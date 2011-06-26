/**
 * 
 */
package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.evaluation.Best;
import id.mdgs.evaluation.Best.FBest;
import id.mdgs.fnlvqold.Fnlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFpglvq implements ITrain {
	protected final int CLASS = 0;
	protected final int NCLASS = 1;	
	
	public Fpglvq network;
	protected double alpha;
	protected double alphaStart;
	protected double xi;
	protected double xiStart;
//	protected Dataset training;
	protected DatasetProfiler profiler;
	protected double error;
	public int maxEpoch;
	protected int currEpoch;
	
	public FBest bestCodebook;
	protected FoldedDataset<Dataset, Entry> foldedDs;
	/**
	 * 
	 */
	public TrainFpglvq(Fpglvq network, Dataset training, double learningRate) {
		this(network, new FoldedDataset<Dataset, Entry>(training), learningRate);
	}
	
	public TrainFpglvq(Fpglvq network, FoldedDataset<Dataset, Entry> foldedDs, double learningRate) {
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;
		this.xi			= 1d;
		this.foldedDs	= foldedDs;
		
		setNetwork(network);
//		this.network 	= network;
//		this.bestCodebook = new Best.FBest(network.codebook);
	}
	
	public TrainFpglvq(FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		this.alpha		= learningRate;
		this.alphaStart	= learningRate;
		this.xi			= 1d;
		this.xiStart	= this.xi;
		this.foldedDs	= foldedDs;
	}

	@Override
	public void setNetwork(IClassify<?, ?> net) {
		this.network = (Fpglvq) net;
		this.bestCodebook = new Best.FBest(((Fpglvq) net).codebook);
	}

	@Override
	public void setTraining(FoldedDataset<?, ?> foldedDs) {
		this.foldedDs 	= (FoldedDataset<Dataset, Entry>) foldedDs;
	}
	
	@Override
	public void updateLearningRate() {
		this.alpha *= (1 - (currEpoch/maxEpoch));
	}

	@Override
	public void setMaxEpoch(int maxEpoch) {
		this.maxEpoch = maxEpoch;
		this.currEpoch = 0;
	}

	@Override
	public int getMaxEpoch() {
		return maxEpoch;
	}

	@Override
	public int getCurrEpoch() {
		return currEpoch;
	}

	@Override
	public void setError(double error) {
		this.error = error;
	}

	@Override
	public double getError() {
		return error;
	}

	private void updateXiParameter() {
		this.xi = currEpoch;
	}
	
	public double lostFunction(double mce){
		return MathUtils.sigmoid(mce * xi);
	}
	
	public FoldedDataset<Dataset, Entry> getTraining(){
		return this.foldedDs;
	}
	
	@Override
	public boolean shouldStop() {
		return currEpoch >= maxEpoch;
	}

	@Override
	public int getNumberOfClass() {
		return this.network.codebook.numEntries;
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
		updateXiParameter();
		setError(avgError/N);
		currEpoch++;
		
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}
	
	protected double train(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wins;
		double mce = 0, fori = 0, finc = 0; 
		
		wins = this.findWinner(codebook, input);
		double miu1 = wins[CLASS].coef;
		double miu2 = wins[NCLASS].coef;
		
		//adjust code vector
		//ada 2 kondisi
		//Jika dikenali, nilai coefisien CLASS n NCLASS > 0
		if(miu1 > 0 || miu2 > 0){
			/*misclassification error, versi similarity*/
			// miu2 - miu1 / (2 - miu1 - miu2)

			mce  = (miu2 - miu1)/ (2 - miu1 - miu2);
//			fori = (2 * (miu2 - 1)) / Math.pow(2 - miu1 - miu2, 2);
//			finc = (2 * (1 - miu1)) / Math.pow(2 - miu1 - miu2, 2);
//			fori = (miu2 - 1) / (2 - miu1 - miu2);
//			finc = (1 - miu1) / (2 - miu1 - miu2);
			fori = -((1 - miu2)) / Math.pow(2 - miu1 - miu2, 2);
			finc = ((1 - miu1)) / Math.pow(2 - miu1 - miu2, 2);
			
			/*GENUINE*/
			adjustWeights(wins[CLASS].winner, input, this.alpha, mce, fori);
			/*INCORRECT*/
			adjustWeights(wins[NCLASS].winner, input,this.alpha, mce, finc);
			
//			System.out.print(String.format("%10s, %7.4f : %7.4f : %7.4f\n", "Known", mce, fori, finc));
			
			//cek dari mce, (+) dipersempit, karena salah prediksi
			if(mce > 0) {
				FEntry cbe = wins[NCLASS].winner;
					
				for(int j=0;j < cbe.size();j++){
					double lRange = (cbe.data[j].mean - cbe.data[j].min);
					double rRange = (cbe.data[j].max - cbe.data[j].mean);
					
					cbe.data[j].min = cbe.data[j].mean - lRange * (1 - 0.00005 * alpha);//((1 - cbe.miu[j]));
					cbe.data[j].max = cbe.data[j].mean + rRange * (1 - 0.00005 * alpha);//((1 - cbe.miu[j]));
				}
			} else {
				//dilebarin
				FEntry cbe = wins[CLASS].winner;
				
				for(int j=0;j < cbe.size();j++){
					double lRange = (cbe.data[j].mean - cbe.data[j].min);
					double rRange = (cbe.data[j].max - cbe.data[j].mean);
					
					cbe.data[j].min = cbe.data[j].mean - lRange * (1 + 0.00005 * alpha);//((1 - cbe.miu[j]));
					cbe.data[j].max = cbe.data[j].mean + rRange * (1 + 0.00005 * alpha);//((1 - cbe.miu[j]));
				}
			}
		}
		
		//else jika nilai coefisien = 0
		else {
			//lebarin sgitiga fuzzy semua codebook entry
			double DELTA = 0.1d;
			for(int i=0; i < codebook.size();i++){
				FEntry cbe = codebook.get(i);
				for(int j=0;j < cbe.size();j++){
					double lRange = (cbe.data[j].mean - cbe.data[j].min);
					double rRange = (cbe.data[j].max - cbe.data[j].mean);
					
//					cbe.data[j].min = cbe.data[j].mean - lRange * (1 + this.alpha * DELTA);//- lRange * this.alpha * DELTA;
//					cbe.data[j].max = cbe.data[j].mean + rRange * (1 + this.alpha * DELTA);//+ rRange * this.alpha * DELTA;
					cbe.data[j].min = cbe.data[j].mean - lRange - lRange * this.alpha * DELTA;
					cbe.data[j].max = cbe.data[j].mean + rRange + rRange * this.alpha * DELTA;
				}
			}
			
//			System.out.print(String.format("%10s, -\n", "Unknown"));
		}
		
		return mce;
	}
	
	protected void adjustWeights(FEntry code, Entry input, double alpha, 
			double mce, double factor){
		
		double sig = lostFunction(mce);
		for(int i=0;i < code.size();i++){
			/*update center*/
			double lRange = code.data[i].mean - code.data[i].min; 
			double rRange = code.data[i].max - code.data[i].mean;
			
			double tmp1, tmp2;
			if(input.data[i] <= code.data[i].mean){
				if(input.data[i] > code.data[i].min){
					tmp1 = -1 * (input.data[i] - code.data[i].min);
					tmp2 = code.data[i].mean - code.data[i].min;
				} else {
					tmp1 = 0;
					tmp2 = 1;
				}
			} else {
				if(input.data[i] < code.data[i].max){
					tmp1 = code.data[i].max - input.data[i];
					tmp2 = code.data[i].max - code.data[i].mean;
				} else {
					tmp1 = 0;
					tmp2 = 1;
				}
			}

			double deltaW = alpha * sig * (1 - sig) * factor * tmp1 / (tmp2 * tmp2);
			code.data[i].mean -= deltaW; 
			
			code.data[i].min = code.data[i].mean - lRange;
			code.data[i].max = code.data[i].mean + rRange;
		}
	}
	
	public FWinnerInfo[] findWinner(FCodeBook codebook, Entry input){
		FWinnerInfo[] winner;
		
		winner = this.network.findWinner.function(codebook, input, codebook.size());
		
		/*find the best matching to original class and other classes*/
		FWinnerInfo[] result = new FWinnerInfo[2];
		result[CLASS] = new FWinnerInfo(); /*for original class*/
		result[NCLASS] = new FWinnerInfo(); /*for other best class*/
		
		//find in same class
		for(int i=0;i < winner.length;i++){
			if(winner[i].winner.label == input.label){
				result[CLASS].copy(winner[i]);
				break;
			}
		}
		
		for(int i=0;i < winner.length;i++){
			if(winner[i].winner.label != input.label){
				result[NCLASS].copy(winner[i]);
				break;
			}
		}		
		
		return result;
	}
	
	@Override
	public void reloadPreviousCode(int label) {
		throw new RuntimeException("Not Supported");
	}

	@Override
	public void reset() {
		this.alpha 	= this.alphaStart;
		this.xi		= this.xiStart;
		this.currEpoch = 0;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getClass().getSimpleName() + " -> ");
		sb.append("alpha: " + alphaStart + ","); 
		sb.append("epoch: " + maxEpoch);
		
		if(foldedDs != null)
			sb.append(", NdataTrain: " + foldedDs.size());
		
		return sb.toString();
	}
}
