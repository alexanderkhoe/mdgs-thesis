package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.FuzzyNode;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.evaluation.Best;
import id.mdgs.evaluation.Best.FBest;
import id.mdgs.fwlvq.Fwlvq;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

public class TrainCFnlvq implements ITrain {

	public CFnlvq network;
	protected double alpha;
	protected double alphaStart;
	protected double error;
	public int maxEpoch;
	protected int currEpoch;
	
	public FBest bestCodebook;
	protected FoldedDataset<Dataset, Entry> foldedDs;
	
	public TrainCFnlvq(CFnlvq network, Dataset training, double learningRate) {
		this(network, new FoldedDataset<Dataset, Entry>(training), learningRate);
	}
	
	public TrainCFnlvq(CFnlvq network, FoldedDataset<Dataset, Entry> foldedDs, double learningRate) {
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;
		this.foldedDs	= foldedDs;
		
		setNetwork(network);
	}
	
	public TrainCFnlvq(FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;
		this.foldedDs	= foldedDs;
	}
	
	@Override
	public void updateLearningRate() {
		this.alpha = alphaStart * (1 - (currEpoch/maxEpoch));
		
	}

	@Override
	public void setMaxEpoch(int maxEpoch) {
		this.maxEpoch = maxEpoch;
		
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
	public void setError(double error) {
		this.error = error;
		
	}

	@Override
	public double getError() {
		return this.error;
	}

	@Override
	public void reloadPreviousCode(int label) {
		// TODO Auto-generated method stub
	}

	@Override
	public void iteration() {
		if(getCurrEpoch() >= getMaxEpoch()){
			utils.log("Exeed max epoch.");
			return;
		}
		
		int TP = 0, N = 0;
		double avgError = 0;
		for(Entry sample: getTraining()){
			double mce;
			mce = train(this.network.codebook, sample);
			avgError += lostFunction(mce);
			
			if(mce < 0) TP++;
			N++;
		}
	
		updateLearningRate();
		setError(avgError/N);
		currEpoch++;
		
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
		
	}

	protected FWinnerInfo train(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wins;
		
		wins = this.network.findWinner.function(codebook, input, codebook.size());
		
		/*adjust code vector*/
		adjustWeights(codebook, wins[0].winner, input, this.alpha, 
				MathUtils.equals(wins[0].coef, ((CFnlvq)this.network).wavelet(0)));
		
		return wins[0];
	}
	
	protected void adjustWeights(FCodeBook codebook, FEntry code, Entry input,
			double alpha, boolean unknown) {
		
		if (unknown){
			double DELTA = 1.01d;
			for(int i=0; i < codebook.size();i++){
				FEntry cbe = codebook.get(i);
				for(int j=0;j < cbe.size();j++){
					cbe.data[j].min = cbe.data[j].mean - DELTA * (cbe.data[j].mean - cbe.data[j].min);
					cbe.data[j].max = cbe.data[j].mean + DELTA * (cbe.data[j].max - cbe.data[j].mean);
				}
			}
		}
		
		//equals
		else if (code.label == input.label){
			//shift winning cluster toward the target
			for(int j=0;j < code.size();j++){
				FuzzyNode fn = code.data[j];
				double miu 	 = code.miu[j];
				double in 	 = input.data[j];
				
				double oldMean = fn.mean;
				fn.mean = fn.mean + alpha * (1 - miu) * (in - fn.mean);
				double jarak = fn.mean - oldMean;
				fn.min  = fn.min + jarak;
				fn.max  = fn.max + jarak;
				 			
				//increase fuzziness (extend fuzziness)
				double lebar = fn.mean - fn.min;
				fn.min = fn.mean - lebar * (1 + 0.5 * (1 - miu) * alpha);
				fn.max = fn.mean + lebar * (1 + 0.5 * (1 - miu) * alpha);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			for(int j=0;j < code.size();j++){
				FuzzyNode fn = code.data[j];
				double miu 	 = code.miu[j];
				double in 	 = input.data[j];
				
				double oldMean = fn.mean;
				fn.mean = fn.mean - alpha * (1 - miu) * (in - fn.mean);
				double jarak = fn.mean - oldMean;
				fn.min  = fn.min + jarak;
				fn.max  = fn.max + jarak;
				 			
				double sempit = fn.mean - fn.min;
				fn.min = fn.mean - sempit * (1 - (1 - miu) * alpha);
				fn.max = fn.mean + sempit * (1 - (1 - miu) * alpha);
			}
		}		
	}
	
	
	@Override
	public boolean shouldStop() {
		return this.currEpoch >= this.maxEpoch;
	}

	@Override
	public int getNumberOfClass() {
		return this.network.codebook.numEntries;
	}

	@Override
	public void setNetwork(IClassify<?, ?> net) {
		this.network = (CFnlvq) net;
		this.bestCodebook = new Best.FBest(((CFnlvq) net).codebook);
	}

	@Override
	public FoldedDataset<Dataset, Entry> getTraining() {
		return this.foldedDs;
	}

	@Override
	public void reset() {
		this.alpha 	= this.alphaStart;
		this.currEpoch = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setTraining(FoldedDataset<?, ?> foldedDs) {
		this.foldedDs 	= (FoldedDataset<Dataset, Entry>) foldedDs;
		
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
