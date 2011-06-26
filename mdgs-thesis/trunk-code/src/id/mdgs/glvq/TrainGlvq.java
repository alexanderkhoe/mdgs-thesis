/**
 * 
 */
package id.mdgs.glvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.Best;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainGlvq extends TrainLvq1 {

	protected final int CLASS = 0;
	protected final int NCLASS = 1;
	
	protected double xi;
	protected double xiStart;
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 * @param windowWidth
	 */
	public TrainGlvq(Glvq network, Dataset training, double learningRate) {
		this(network, new FoldedDataset<Dataset, Entry>(training), learningRate);
	}
	
	public TrainGlvq(Glvq network, FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		super(network, foldedDs, learningRate);
		
		/*Set findWinner method to Squared Euclid*/
//		network.findWinner = new WinnerBySquaredEuc();
		
		this.xi	= 1d;
		this.xiStart = this.xi;
	}
	
	//bahaya network belum diset, yakinkan pake GLVQ
	public TrainGlvq(FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		super(foldedDs, learningRate);
		
//		/*Set findWinner method to Squared Euclid*/
//		network.findWinner = new WinnerBySquaredEuc();
		
		this.xi	= 1d;
		this.xiStart = this.xi;
	}
	
	
	@Override
	public void reset() {
		super.reset();
		
		this.xi = this.xiStart;
	}

	protected void updateXiParameter() {
		this.xi *= 1.1;// + (epoch/maxEpoch);
	}

	public double lostFunction(double mce){
		return MathUtils.sigmoid(mce * xi);
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
			WinnerInfo wi;
			wi = train(this.network.codebook, sample);
			
			double mce = wi.coef;
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
	
	
	@Override
	protected WinnerInfo train(Dataset codebook, Entry input) {
		WinnerInfo[] wins;
		double mce = 0, fori = 0, finc = 0; 
		
		wins = this.findWinner(codebook, input);
		
		/*misclassification error*/
		double d1 = wins[CLASS].coef;
		double d2 = wins[NCLASS].coef;
		
		mce = (d1 - d2) / (d1 + d2);
		/*it should be [d(ki) + d(rj)]^2, but for simplification, remove the squere*/
		fori= (d2)/ (d1 + d2);
		finc= (d1)/ (d1 + d2);
		
		/*adjust code vector*/
		/*GENUINE*/
		adjustWeights(wins[CLASS].winner, input, this.alpha, mce, fori);
		/*INCORRECT*/
		adjustWeights(wins[NCLASS].winner, input, -this.alpha, mce, finc);
		
		WinnerInfo wi = new WinnerInfo();
		if(wins[CLASS].coef < wins[NCLASS].coef){
			wi.winner 	= wins[CLASS].winner;
			wi.coef		= mce;
		} else {
			wi.winner 	= wins[NCLASS].winner;
			wi.coef		= mce;
		}
		
		return wi;
	}

	protected void adjustWeights(Entry code, Entry input, double alpha, 
			double mce, double factor){
		
		double sig = lostFunction(mce);
		for(int i=0;i < code.size();i++){
			code.data[i] += alpha * 4 * sig * (1 - sig) * factor * 
					(input.data[i] - code.data[i]);
		}
	}

	@Override
	protected void adjustWeights(Entry code, Entry input, double alpha) {
		throw new RuntimeException("Not Supported");
	}
	
	public WinnerInfo[] findWinner(Dataset codebook, Entry input){
		WinnerInfo[] winner;
		int i;

		winner = this.network.findWinner.function(codebook, input, codebook.size());
		
		/*find the best matching to original class and other classes*/
		WinnerInfo[] result = new WinnerInfo[2];
		result[CLASS] = new WinnerInfo(); /*for original class*/
		result[NCLASS] = new WinnerInfo(); /*for other best class*/
		
		//find in same class
		for(i=0;i < winner.length;i++){
			if(winner[i].winner.label == input.label){
				result[CLASS].copy(winner[i]);
				break;
			}
		}
		
		for(i=0;i < winner.length;i++){
			if(winner[i].winner.label != input.label){
				result[NCLASS].copy(winner[i]);
				break;
			}
		}		
		
		return result;
	}
	
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
