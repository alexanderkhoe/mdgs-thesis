/**
 * 
 */
package id.mdgs.lvq;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.Dataset.EntryIterator;
import id.mdgs.lvq.DatasetProfiler.PEntry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq1 {
	protected final double ALPHA_DECAY_FACTOR = 0.99D;
	protected double alpha;
	protected double alphaStart;
	protected Dataset training;
	public Lvq network;
	protected double error = 0;
	protected WinnerFunction findWinner;
	/*for supporting option 3*/
	protected DatasetProfiler profiler;
	protected int index;
	public int maxEpoch;
	public int currEpoch;
	
	/**/
	public static class Best {
		public Dataset codebook;
		public double  coef;
		public int epoch;
		public Best(Dataset ds) {
			codebook = new Dataset(ds);
			coef = Double.MAX_VALUE;
			epoch = 0;
		}
		public void evaluate(Dataset codes, double err, int epoch){
			if(coef > err){
				coef = err;
				codebook.set(codes);
				this.epoch = epoch;
			}
		}
	}
	public Best bestCodebook;
	/**
	 * 
	 */
	public TrainLvq1(Lvq network, Dataset training, double learningRate){
		this.network 	= network;
		this.training	= training;
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;		
		this.profiler	= new DatasetProfiler();
		this.index		= 0;
		this.findWinner = new WinnerByEuc();
		
		this.bestCodebook = new Best(network.codebook);
	}
	
	/**
	 * @param maxEpoch the maxEpoch to set
	 */
	public void setMaxEpoch(int maxEpoch) {
		this.maxEpoch = maxEpoch;
		this.currEpoch = 0;
	}

	public boolean shouldStop(){
		return currEpoch >= maxEpoch;
	}
	
	public Dataset getTraining(){
		return this.training;
	}
	
	
	public void updateLearningRate(){
//		this.alpha *= this.ALPHA_DECAY_FACTOR;
		this.alpha = this.alphaStart * (1 - ((double)currEpoch/maxEpoch));
	}
	
	public void setError(double error) {
		this.error = error;
	}

	public double getError() {
		return error;
	}

	/**
	 * @return the findWinner
	 */
	public WinnerFunction getFindWinner() {
		return findWinner;
	}

	/**
	 * @param findWinner the findWinner to set
	 */
	public void setFindWinner(WinnerFunction findWinner) {
		this.findWinner = findWinner;
	}

	/*OPSI 1*/
	public void iterationOption1(){
		double worstDist = Double.MIN_VALUE;
		
		int numTrue = 0, N = 0;
		for(Entry e: getTraining()){
			WinnerInfo wi;
			wi = train(this.network.codebook, e);

//			if(worstDist < wi.coef)
//				worstDist = wi.coef;
			if(wi.winner.label == e.label) numTrue++;
			N++;
		}

		setError(1 - ((double)numTrue/N));
		updateLearningRate();
		currEpoch++;
		
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}
	
	/*OPSI 3*/
	public void resetCounter(){
		this.index = 0;
	}
	protected void prepareOption3(){
		if(profiler.size() > 0){
			profiler.reset();
		}
		
		profiler.run(getTraining());
	}

	public void iterationOption3(){
		iterationOption32();
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}
	
	private void iterationOption32(){
		if(profiler.size() <= 0){
			prepareOption3();
		}

//		if(this.alpha < 0){
//			utils.log("alpha <= 0. can not continue...");
//			return;
//		}
			
		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		
		int numTrue = 0;
		int numOfData = 0;
		Entry sample = null;
		
		for(int i=0;i<maxData;i++){
			index = i;		
			for(PEntry pe: profiler){
				if(index >= pe.size()) continue;
				
				int pos = pe.get(index % pe.size());
				sample = getTraining().get(pos);

				WinnerInfo wi;
				wi = train(this.network.codebook, sample);
				
				if(wi.winner.label == sample.label)
					numTrue++;
				
				numOfData++;
			}
		}
		
		updateLearningRate();
		setError(1 - ((double)numTrue/numOfData));
		currEpoch++;
	}
	
	protected WinnerInfo train(Dataset codebook, Entry input) {
		WinnerInfo wi;
		
//		wi = LvqUtils.findWinner(codebook, input);
		wi = this.findWinner.function(codebook, input);
		
		this.adjustWeights(wi.winner, input, this.alpha);
		
		return wi;
	}	
	
	protected void adjustWeights(Entry code, Entry input, double alpha) {
		//move toward input vector
		if(code.label == input.label){
			for(int i=0;i < code.size();i++){
				code.data[i] += alpha * (input.data[i] - code.data[i]);
			}
		}
		
		//move away from input vector
		else {
			for(int i=0;i < code.size();i++){
				code.data[i] -= alpha * (input.data[i] - code.data[i]);
			}
		}
	}	
	

	
	/*reserved for msa*/
	public void reloadPreviousCode(int label){
		throw new RuntimeException("method not supported");
	}
	
	
	/*OPSI 2*/
	public void iterationOption2(int num){
		double worstDist = Double.MIN_VALUE;
		Entry sample = null;
		int numError = 0;
		int numOfData = 0;
		EntryIterator eIt = (EntryIterator) getTraining().iterator();
		for(int i = 0; i < num; i++){
			sample = eIt.next();
			if(sample == null){
				eIt.rewind();
				sample = eIt.next();
				if(sample == null){
					throw new RuntimeException("TrainLvq1: Failed reading data");
				}
			}
			
			WinnerInfo wi;
			wi = train(this.network.codebook, sample);
			
			if(wi.winner.label != sample.label)
				numError++;

			numOfData++;
			
			updateLearningRate();
		}
		
		setError((double)numError/numOfData);
	}
	
	private void iterationOption31(){
		if(profiler.size() <= 0){
			prepareOption3();
		}

		int numError = 0;
		int numOfData = 0;
		Entry sample = null;
		
		for(PEntry pe: profiler){
			int pos = pe.get(index % pe.size());
			sample = getTraining().get(pos);
			
			WinnerInfo wi;
			wi = train(this.network.codebook, sample);
			
			if(wi.winner.label != sample.label)
				numError++;
			
			numOfData++;
		}

		this.index++;
		
		setError((double)numError/numOfData);
		
		updateLearningRate();
	}
	
	public void iterationOption1Save(){
		double worstDist = Double.MIN_VALUE;
		BufferedWriter writer = utils.openWriter(utils.getDefaultPath() + "/resources/trash/trainsave.txt", false);
		
		
		for(Entry e: getTraining()){
			WinnerInfo wi;
			wi = train(this.network.codebook, e);
			
			if(worstDist < wi.coef)
				worstDist = wi.coef;
			
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<this.network.codebook.size();i++){
				Entry code = this.network.codebook.get(i);
				sb.append(String.format("%d\t%f\t%f\n",code.label,code.data[0],code.data[149]));
			}
			
			try {
				writer.write(sb.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}

		utils.closeWriter(writer);
		setError(worstDist);
		updateLearningRate();
	}
	
	public void iterationOption1Variant(){
		int numError = 0;
		
		/*get randomize index*/
		int numOfData = getTraining().size();
		int[] poss = MathUtils.randPerm(numOfData, numOfData);
		
		for(int i=0;i < numOfData;i++){
			Entry e = getTraining().get(poss[i]);

			WinnerInfo wi;
			wi = train(this.network.codebook, e);
			
			if(wi.winner.label != e.label)
				numError++;
		}

		setError((double)numError/numOfData);
		updateLearningRate();
		currEpoch++;
	}
}
