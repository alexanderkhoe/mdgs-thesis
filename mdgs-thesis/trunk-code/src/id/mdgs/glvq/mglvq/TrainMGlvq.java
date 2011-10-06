/**
 * 
 */
package id.mdgs.glvq.mglvq;

import java.util.Iterator;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 *
 * Notes:
 * 2011.08.12
 * - mahalanobis distance has been derived
 * - update rule implement using derived version
 * - change iteration mechanism using the latest version 
 * 
 * 2011.06.15
 * - No distance derivation yet, and update rule followed by wrong rule 
 */
public class TrainMGlvq extends TrainGlvq {

	public TrainMGlvq(FoldedDataset<Dataset, Entry> foldedDs,
			double learningRate) {
		super(foldedDs, learningRate);
	}

	public TrainMGlvq(MGlvq network, Dataset training, double learningRate) {
		this(network, new FoldedDataset<Dataset, Entry>(training), learningRate);
	}
	
	public TrainMGlvq(MGlvq network, FoldedDataset<Dataset, Entry> foldedDs, double learningRate){
		super(network, foldedDs, learningRate);
	}

	@Override
	protected void adjustWeights(Entry code, Entry input, double alpha,
			double mce, double factor) {
		double sig = lostFunction(mce);
		
		WinnerByMahalanobis wm = ((WinnerByMahalanobis)this.network.findWinner);
		RealMatrix invS = ((WinnerByMahalanobis)this.network.findWinner).mps.invS;
		
//		System.out.println(invS.getRowDimension() + "x" + invS.getColumnDimension());
//		System.out.println(invS.toString());
//		System.out.println(wm.mps.mean.toString());
		//pembilang
		RealMatrix X = new Array2DRowRealMatrix(input.data);
		RealMatrix W = new Array2DRowRealMatrix(code.data);
		RealMatrix M = X.subtract(W);
		
		RealMatrix pembilang = invS.multiply(M); //24 x 1
		
		//mahal distance
		double penyebut = Math.sqrt(MathUtils.mahalanobisDistance(M, invS));
		
//		System.out.println(code.toString());
		for(int i=0;i < code.size();i++){
			double delta = alpha * 4 * sig * (1 - sig) * factor * 
					(pembilang.getEntry(i, 0) / penyebut);
			
			code.data[i] += delta;
//			System.out.print(delta + ",");
		}
//		System.out.println();
		
//		System.exit(0);
	}

	@Override
	public WinnerInfo[] findWinner(Dataset codebook, Entry input) {
		WinnerInfo[] winner;
		int i;
		winner = new WinnerInfo[codebook.size()];
		for(i=0;i < winner.length;i++){
			winner[i] = new WinnerInfo();
		}
		
		Iterator<Entry> eIt = codebook.iterator();
		while(eIt.hasNext()) {
			double difference = 0;
			Entry code = eIt.next();
			
			difference = ((WinnerByMahalanobis) this.network.findWinner).getDistance(input, code);
//			System.out.println(difference);
			for(i=0; (i < winner.length) && (difference > winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = difference;
				winner[i].winner = code;
			}
		}
		
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

	

	
	
//	protected final int CLASS = 0;
//	protected final int NCLASS = 1;
//	public int maxEpoch;
//	public int currEpoch;
//	protected double alpha;
//	protected double alphaStart;
//	protected double xi;
//	protected Dataset training;
//	public MGlvq network;
//	protected DatasetProfiler profiler;
//	protected double error;
//
//	protected FoldedDataset<Dataset, Entry> foldedDs;
//	/**/
//	public static class Best {
//		public Dataset codebook;
//		public double  coef;
//		public int epoch;
//		public Best(Dataset ds) {
//			codebook = new Dataset(ds);
//			coef = Double.MAX_VALUE;
//			epoch = 0;
//		}
//		public void evaluate(Dataset codes, double err, int epoch){
//			if(coef > err){
//				coef = err;
//				codebook.set(codes);
//				this.epoch = epoch;
//			}
//		}
//	}
//	public Best bestCodebook;
//	
//	/**
//	 * 
//	 * @param network
//	 * @param training
//	 * @param learningRate
//	 */
//	public TrainMGlvq(MGlvq network, Dataset training, double learningRate) {
//		this.network 	= network;
//		this.training	= training;
//		this.alphaStart = learningRate;
//		this.alpha		= learningRate;
//		this.xi			= 1d;
//		this.profiler	= new DatasetProfiler();
//		this.maxEpoch	= 100;
//		
//		this.bestCodebook = new Best(network.codebook);
//	}
//	
//	/**
//	 * @return the error
//	 */
//	public double getError() {
//		return error;
//	}
//
//	/**
//	 * @param error the error to set
//	 */
//	public void setError(double error) {
//		this.error = error;
//	}
//
//	public void updateLearningRate(){
//		this.alpha = alphaStart * (1 - (currEpoch/maxEpoch));
////		this.alpha *= (1 - (getCurrIteration()/maxEpoch));
////		this.alpha *= 0.99d;
//	}
//
//	protected void updateXiParameter() {
//		this.xi *= 1.1;// + (epoch/maxEpoch);
//	}
//
//	public double lostFunction(double mce){
//		return MathUtils.sigmoid(mce * xi);
//	}
//	/**
//	 * @return the maxEpoch
//	 */
//	public int getMaxEpoch() {
//		return maxEpoch;
//	}
//
//	/**
//	 * @param maxEpoch the maxEpoch to set
//	 */
//	public void setMaxEpoch(int maxEpoch) {
//		this.maxEpoch = maxEpoch;
//		this.setCurrIteration(0);
//		profiler.run(this.training);
//	}
//	
//	/**
//	 * @return the currIteration
//	 */
//	public int getCurrIteration() {
//		return currEpoch;
//	}
//
//	/**
//	 * @param currIteration the currIteration to set
//	 */
//	public void setCurrIteration(int currIteration) {
//		this.currEpoch = currIteration;
//	}
//
//	/**
//	 * @return the training
//	 */
////	public Dataset getTraining() {
////		return training;
////	}
//
//	public boolean shouldStop(){
//		return currEpoch >= maxEpoch;
//	}
//	
//	public void iteration(int maxEpoch){
//		setMaxEpoch(maxEpoch);
//		
//		for(int epoch=0;epoch < maxEpoch;epoch++){
//			
//			this.iteration();
//			
//			System.out.println("Iteration: " + epoch + ", Error:" + this.getError());
//		}
//	}
//	
//
//	@Override
//	public void iteration() {
//		if(getCurrEpoch() >= getMaxEpoch()){
//			utils.log("Exeed max epoch.");
//			return;
//		}
//		
//		int TP = 0, N = 0;
//		double avgError = 0;
//		for(Entry sample: getTraining()){
//			WinnerInfo wi;
//			wi = train(this.network.codebook, sample);
//			
//			double mce = wi.coef;
//			avgError += lostFunction(mce);
//			
//			if(mce < 0) TP++;
//			N++;
//		}
//	
//		updateLearningRate();
//		updateXiParameter();
//		setError(avgError/N);
//		currEpoch++;
//		
//		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
//	}
//	
//	public void iterationOption3(){
//		//find max
//		int maxData = 0;
//		for(PEntry pe: profiler){
//			if (maxData < pe.size()){
//				maxData = pe.size();
//			}
//		}
//		
//		Entry sample = null;
//		double avgLost = 0;
//		int N = 0; int True = 0;
//		for(int i=0;i<maxData;i++){
//			for(PEntry pe: profiler){
//				if(i >= pe.size()) continue;
//				
//				int pos = pe.get(i % pe.size());
//				sample = training.get(pos);
//				
//				WinnerInfo win;
//				win = train(this.network.codebook, sample);
//				
//				double mce = win.coef;
//				avgLost += lostFunction(mce);
//				N++;
//				if(mce < 0) True++;
//			}
//		}
//		updateLearningRate();
//		updateXiParameter();		
//		setError(avgLost/N);
////		setError(1 - ((double)True/N));
//		
//		this.currEpoch++;
//		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
//	}
//	
//	
//	protected WinnerInfo train(Dataset codebook, Entry input) {
//		WinnerInfo[] wins;
//		double mce = 0, fori = 0, finc = 0; 
//		
//		wins = this.findWinner(codebook, input);
//		/*misclassification error*/
//		mce = (wins[CLASS].coef - wins[NCLASS].coef)/
//			  (wins[CLASS].coef + wins[NCLASS].coef);
//		/*it should be [d(ki) + d(rj)]^2, but for simplification, remove the squere*/
//		fori= (wins[NCLASS].coef)/
//		      (wins[CLASS].coef + wins[NCLASS].coef);
//		  	  //Math.pow((wins[CLASS].coef + wins[NCLASS].coef), 2);
//		finc= (wins[CLASS].coef)/
//			  (wins[CLASS].coef + wins[NCLASS].coef);
//			  //Math.pow((wins[CLASS].coef + wins[NCLASS].coef), 2);
//		
//		
//		/*adjust code vector*/
//		/*GENUINE*/
//		adjustWeights(wins[CLASS].winner, input, this.alpha, mce, fori);
//		/*INCORRECT*/
//		adjustWeights(wins[NCLASS].winner, input, -this.alpha, mce, finc);
//		
//		WinnerInfo wi = new WinnerInfo();
//		if(wins[CLASS].coef < wins[NCLASS].coef){
//			wi.winner 	= wins[CLASS].winner;
//			wi.coef		= mce;
//		} else {
//			wi.winner 	= wins[NCLASS].winner;
//			wi.coef		= mce;
//		}
//		
//		return wi;
//	}
//
//	protected void adjustWeights(Entry code, Entry input, double alpha, 
//			double mce, double factor){
////		double sig = lostFunction(mce*currEpoch);
////		for(int i=0;i < code.size();i++){
////			code.data[i] += alpha * 1 * sig * (1 - sig) * factor * 
////					(input.data[i] - code.data[i]);
////		}
//		double sig = lostFunction(mce);
//		for(int i=0;i < code.size();i++){
//			code.data[i] += alpha * 4 * sig * (1 - sig) * factor * 
//					(input.data[i] - code.data[i]);
//		}
//	}
//	
//
//	
//	public WinnerInfo[] findWinner(Dataset codebook, Entry input){
//		WinnerInfo[] winner;
//		int i;
//		winner = new WinnerInfo[codebook.size()];
//		for(i=0;i < winner.length;i++){
//			winner[i] = new WinnerInfo();
//		}
//		
//		Iterator<Entry> eIt = codebook.iterator();
//		while(eIt.hasNext()) {
//			double difference = 0;
//			Entry code = eIt.next();
//			
//			difference = this.network.getDistance(input, code);
//			
//			for(i=0; (i < winner.length) && (difference > winner[i].coef); i++);
//			
//			if(i < winner.length){
//				for(int j=winner.length - 1;j > i;j--){
//					winner[j].copy(winner[j-1]);
//				}
//				winner[i].coef 	 = difference;
//				winner[i].winner = code;
//			}
//		}
//		
////		winner = this.network.findWinner(codebook, input);
//		
//		/*find the best matching to original class and other classes*/
//		WinnerInfo[] result = new WinnerInfo[2];
//		result[CLASS] = new WinnerInfo(); /*for original class*/
//		result[NCLASS] = new WinnerInfo(); /*for other best class*/
//		
//		//find in same class
//		for(i=0;i < winner.length;i++){
//			if(winner[i].winner.label == input.label){
//				result[CLASS].copy(winner[i]);
//				break;
//			}
//		}
//		
//		for(i=0;i < winner.length;i++){
//			if(winner[i].winner.label != input.label){
//				result[NCLASS].copy(winner[i]);
//				break;
//			}
//		}		
//		
//		return result;
//	}
//
//	@Override
//	public int getCurrEpoch() {
//		return this.currEpoch;
//	}
//
//	@Override
//	public void reloadPreviousCode(int label) {
//		throw new RuntimeException(this.getClass().getSimpleName() + ": reloadPreviousCode() Not Supported");
//	}
//
//	@Override
//	public int getNumberOfClass() {
//		return this.network.codebook.numEntries;
//	}
//	

//
//	@Override
//	public void setNetwork(IClassify<?, ?> net) {
//	}
//
//	@Override
//	public void reset() {
//	}
//
//	@Override
//	public void setTraining(FoldedDataset<?, ?> foldedDs) {
//		
//	}
//
//	@Override
//	public FoldedDataset<Dataset, Entry> getTraining() {
//		return this.foldedDs;
//	}
//
//	@Override
//	public String information() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
