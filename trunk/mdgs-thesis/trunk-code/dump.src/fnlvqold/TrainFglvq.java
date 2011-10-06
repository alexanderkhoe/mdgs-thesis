/**
 * 
 */
package id.mdgs.fnlvqold;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.master.ITrain;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

import java.util.Iterator;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFglvq implements ITrain {

	protected final int CLASS = 0;
	protected final int NCLASS = 1;	
	public Fnlvq network;
	protected double alpha;
	protected double xi;
	protected Dataset training;
	protected DatasetProfiler profiler;
	protected double error;
	public int maxEpoch;
	protected int currEpoch;	
	/**
	 * 
	 */
	public TrainFglvq(Fnlvq network, Dataset training, double learningRate) {
		this.network 	= network;
		this.training	= training;
		this.alpha		= learningRate;
		this.xi			= 1d;
		this.profiler	= new DatasetProfiler();
	}

	/**
	 * @param maxEpoch the maxEpoch to set
	 */
	public void setMaxEpoch(int maxEpoch) {
		this.maxEpoch = maxEpoch;
		this.currEpoch = 0;
		profiler.run(this.training);		
	}

	/**
	 * @return the error
	 */
	public double getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(double error) {
		this.error = error;
	}

	public void updateLearningRate(){
		this.alpha *= (1 - (currEpoch/maxEpoch));
	}

	private void updateXiParameter() {
		this.xi = currEpoch;
	}

	public double lostFunction(double mce){
		return MathUtils.sigmoid(mce * xi);
	}
	
	public void iteration(){
		if(currEpoch >= maxEpoch){
			utils.log("Exeed max epoch.");
			return;
		}
		
		iteration3();
	}
	
	private void iteration3(){
		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		
		Entry sample = null;
		double avgLost = 0;
		int N = 0; int True = 0;
		for(int i=0;i<maxData;i++){
			for(PEntry pe: profiler){
				if(i >= pe.size()) continue;
				
				int pos = pe.get(i % pe.size());
				sample = training.get(pos);
				
				double mce;
				mce = train(this.network.codebook, sample);
				avgLost += lostFunction(mce);
				N++;
				if(mce < 0) True++;
			}
		}
		//System.out.print("Benar: " + Benar);
		updateLearningRate();
		updateXiParameter();		
		setError(avgLost/N);
		//setError(1 - ((double)True/N));
		
		this.currEpoch++;
	}
	
	protected double train(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wins;
		double mce = 0, fori = 0, finc = 0; 
		
		wins = this.findWinner(codebook, input);

		//adjust code vector
		//ada 2 kondisi
		//Jika dikenali, nilai coefisien CLASS n NCLASS > 0
		//if(MathUtils.max(wins[CLASS].coef, wins[NCLASS].coef) > 0){
		if(wins[CLASS].coef > 0 && wins[NCLASS].coef > 0){
			/*misclassification error, versi similarity*/
			// miu2 - miu1 / (2 - miu1 - miu2)
			double miu1 = wins[CLASS].coef;
			double miu2 = wins[NCLASS].coef;
			
			mce  = (miu2 - miu1)/ (2 - miu1 - miu2);
//			fori = (2 * (miu2 - 1)) / Math.pow(2 - miu1 - miu2, 2);
//			finc = (2 * (1 - miu1)) / Math.pow(2 - miu1 - miu2, 2);
//			fori = (miu2 - 1) / (2 - miu1 - miu2);
//			finc = (1 - miu1) / (2 - miu1 - miu2);
			fori = ((miu2 - 1)) / Math.pow(2 - miu1 - miu2, 2);
			finc = ((1 - miu1)) / Math.pow(2 - miu1 - miu2, 2);
			
			/*GENUINE*/
			adjustWeights(wins[CLASS].winner, input, this.alpha, mce, fori);
			/*INCORRECT*/
			adjustWeights(wins[NCLASS].winner, input,this.alpha, mce, finc);
			
//			System.out.print(String.format("%10s, %7.4f : %7.4f : %7.4f\n", "Known", mce, fori, finc));
			
			//cek dari mce, (+) dipersempit, karena salah prediksi
//			if(mce > 0) {
//				FEntry cbe = wins[NCLASS].winner;
//				Entry sim  = network.miu.findEntry(cbe.label);
//				
//				for(int j=0;j < cbe.size();j++){
//					double lRange = (cbe.data[j].mean - cbe.data[j].min);
//					double rRange = (cbe.data[j].max - cbe.data[j].mean);
//					
//					cbe.data[j].min = cbe.data[j].mean - lRange * (1 - (1 - sim.data[j]) * alpha);
//					cbe.data[j].max = cbe.data[j].mean + rRange * (1 - (1 - sim.data[j]) * alpha);
//				}
//			}
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
		
		winner = this.network.findWinner(codebook, input, codebook.size());
		
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
	public int getCurrEpoch() {
		return this.currEpoch;
	}

	@Override
	public void reloadPreviousCode(int label) {
		throw new RuntimeException(this.getClass().getSimpleName() + ": reloadPreviousCode() Not Supported");
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
	public int getMaxEpoch() {
		return maxEpoch;
	}	
	
}
