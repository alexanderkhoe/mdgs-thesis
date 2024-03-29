/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.Dataset.EntryIterator;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.evaluation.Best;
import id.mdgs.evaluation.Best.CBest;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.master.WinnerFunction;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq1 implements ITrain {
	protected final double ALPHA_DECAY_FACTOR = 0.99D;
	protected double alpha;
	protected double alphaStart;
	public Lvq network;
	protected double error = 0;
	protected DatasetProfiler profiler;
	public int maxEpoch;
	public int currEpoch;
	
	public CBest bestCodebook;
	protected FoldedDataset<Dataset, Entry> foldedDs;
	
	/**
	 * 
	 */
	public TrainLvq1(Lvq network, Dataset training, double learningRate){
		this(network, new FoldedDataset<Dataset, Entry>(training), learningRate);
	}
	
	public TrainLvq1(Lvq network, FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;		
		this.foldedDs	= foldedDs;

		setNetwork(network);
//		this.network 	= network;
//		this.bestCodebook = new Best.CBest(network.codebook);
	}
	
	public TrainLvq1(FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;		
		this.foldedDs	= foldedDs;
	}
	
	@Override
	public void setNetwork(IClassify<?, ?> net) {
		this.network = (Lvq) net;
		this.bestCodebook = new Best.CBest(((Lvq) net).codebook);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setTraining(FoldedDataset<?, ?> foldedDs) {
		this.foldedDs	= (FoldedDataset<Dataset, Entry>) foldedDs;
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
	
	public FoldedDataset<Dataset, Entry> getTraining(){
		return this.foldedDs;
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
	 * iterasi sudah menggunakan sistem epoch
	 * data optomatis di saling silang pada folded dataset
	 */
	public void iteration() {
		if(getCurrEpoch() >= getMaxEpoch()){
			utils.log("Exeed max epoch.");
			return;
		}
		
		int TP = 0, N = 0;
		for(Entry sample: getTraining()){
			WinnerInfo wi;
			wi = train(this.network.codebook, sample);
			
			if(wi.winner.label == sample.label)
				TP++;
			
			N++;
		}
	
		updateLearningRate();
		setError(1 - ((double)TP/N));
		currEpoch++;
		
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}
	
	public void iteration(int num){
		setMaxEpoch(num);
		
		for(int i=0; i < num;i++){
			this.iteration();
		}
	}
	
	
	protected WinnerInfo train(Dataset codebook, Entry input) {
		WinnerInfo wi;
		
//		wi = LvqUtils.findWinner(codebook, input);
		wi = this.network.findWinner.function(codebook, input);
		
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
	
	@Override
	public int getMaxEpoch() {
		return this.maxEpoch;
	}

	@Override
	public int getCurrEpoch() {
		return this.currEpoch;
	}

	@Override
	public int getNumberOfClass() {
		return this.network.codebook.numEntries;
	}

	@Override
	public void reset() {
		this.currEpoch 	= 0;
		this.alpha		= this.alphaStart;
	}

	@Override
	public String information() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getClass().getSimpleName() + " -> ");
		sb.append("alpha: " + alphaStart + ","); 
		sb.append("epoch: " + maxEpoch);
		
		if(foldedDs != null)
			sb.append(", NdataTrain: " + foldedDs.size());
		
		return sb.toString();
	}
}
