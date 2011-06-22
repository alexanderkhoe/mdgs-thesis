/**
 * 
 */
package id.mdgs.fnlvqold;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.FCodeBook.FWinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFglvqFuzzy extends TrainFnlvqFuzzy {
	protected final int CLASS = 0;
	protected final int NCLASS = 1;
	protected double xi;
	
	public TrainFglvqFuzzy(FnlvqF network, Dataset training, double learningRate) {
		this(network, training, learningRate, 5);
		
	}

	public TrainFglvqFuzzy(FnlvqF network, Dataset training,
			double learningRate, int num) {
		super(network, training, learningRate, num);
		this.xi 	= 1d;
	}

	private void updateXiParameter() {
		this.xi *= 1.1;
	}

	public double lostFunction(double mce){
		return MathUtils.sigmoid(mce * xi);
	}
	
	@Override
	public void iteration() {
		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		
		FEntry sample = null;
		int N = 0; int True = 0;
		FWinnerInfo wi;
		double mce;
		for(int i=0;i<maxData;i++){
			for(PEntry pe: profiler){
				if(i >= pe.size()) continue;
				
				int pos = pe.get(i % pe.size());
				sample = fuzzyset.get(pos);
				
				mce = train2(this.network.codebook, sample);
				
				N++;
				if(mce < 0) True++;
			}
		}
//		System.out.println("E: " + utils.timer.stop());
		//System.out.print("Benar: " + Benar);
		updateLearningRate();
		updateXiParameter();
		//setError(avgLost/N);
		setError(1 - ((double)True/N));
		
		this.currEpoch++;
	}
	
	
	protected double train2(FCodeBook codebook, FEntry input) {
		FWinnerInfo[] wins;
		double mce = 0, fori = 0, finc = 0; 
		
		wins = this.findWinner(codebook, input);
		/*misclassification error*/
		mce = (wins[CLASS].coef - wins[NCLASS].coef)/
			  (wins[CLASS].coef + wins[NCLASS].coef);
		/*it should be [d(ki) + d(rj)]^2, but for simplification, remove the squere*/
		fori= (wins[NCLASS].coef)/
		  	  (wins[CLASS].coef + wins[NCLASS].coef);
		finc= (wins[CLASS].coef)/
		  	  (wins[CLASS].coef + wins[NCLASS].coef);
		
		
		/*adjust code vector*/
		/*GENUINE*/
		adjustWeights(wins[CLASS].winner, input, this.alpha, mce, fori);
		/*INCORRECT*/
		adjustWeights(wins[NCLASS].winner, input, -this.alpha, mce, finc);
		
		return mce;
	}
	
	protected void adjustWeights(FEntry code, FEntry input, double alpha, 
			double mce, double factor){
		
		double sig = lostFunction(mce);
		for(int i=0;i < code.size();i++){
			/*update center*/
			double lRange = code.data[i].mean - code.data[i].min; 
			double rRange = code.data[i].max - code.data[i].mean;
			
			code.data[i].mean += alpha * 4 * sig * (1 - sig) * factor * 
					(input.data[i].mean - code.data[i].mean);
			
			code.data[i].min = code.data[i].mean - lRange;
			code.data[i].max = code.data[i].mean + rRange;
		}
	}
	
	protected FWinnerInfo[] findWinner(FCodeBook codebook, FEntry input){
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
	protected FWinnerInfo train(FCodeBook codebook, FEntry input) {
		return null;
	}
	@Override
	protected void adjustWeights(FCodeBook codebook, Entry mius, FEntry code,
			FEntry input, double alpha, boolean unknown) {
	}




}
