/**
 * 
 */
package id.mdgs.fnlvqold;

import java.awt.Graphics;

import junit.framework.Assert;

import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralData;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.FuzzyNode;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvq {

	protected final int CLASS = 0;
	protected final int NCLASS = 1;	
	public Fnlvq network;
	protected double alpha;
	public double alphaStart;
	protected double xi;
	protected Dataset training;
	protected DatasetProfiler profiler;
	protected double error;
	public int maxEpoch;
	public int currEpoch;	
	
	/**/
	protected double xMin, xMax;
	/**
	 * 
	 */
	public TrainFnlvq(Fnlvq network, Dataset training, double learningRate) {
		this.network 	= network;
		this.training	= training;
		this.alphaStart	= learningRate;
		this.alpha		= learningRate;
		this.profiler	= new DatasetProfiler();
		init();
	}

	public void init(){
		xMin = Double.POSITIVE_INFINITY; 
		xMax = Double.NEGATIVE_INFINITY;
		for(Entry e: training){			
			for(int i = 0;i < e.size();i++){
				if(xMin > e.data[i]) xMin = e.data[i];
				if(xMax < e.data[i]) xMax = e.data[i];
			}
		}
	}
	
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
		this.alpha = alphaStart * (1 - (currEpoch/maxEpoch));
	}
	
	public boolean shouldStop() {
		return currEpoch >= maxEpoch;
	}

	
	public void iteration(){
		if(currEpoch >= maxEpoch){
			utils.log("Exeed max epoch.");
			return;
		}
		
		iteration3();
	}
	
	protected void iteration3(){
		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		//utils.timer.start();
		Entry sample = null;
		int N = 0; int True = 0;
		FWinnerInfo wi;
		for(int i=0;i<maxData;i++){
			for(PEntry pe: profiler){
				if(i >= pe.size()) continue;
				
				int pos = pe.get(i % pe.size());
				sample = training.get(pos);
				
				wi = train(this.network.codebook, sample);
				
				N++;
				
				if(!MathUtils.equals(wi.coef, 0) && wi.winner.label == sample.label) True++;
			}
		}
		//System.out.println("E: " + utils.timer.stop());
		//System.out.print("Benar: " + Benar);
		updateLearningRate();
		//setError(avgLost/N);
		setError(1 - ((double)True/N));
		
		this.currEpoch++;
	}	
	
	protected FWinnerInfo train(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wins;
		
		wins = this.network.findWinner(codebook, input, codebook.size());
		
		/*adjust code vector*/
		adjustWeights(codebook, wins[0].winner, input, this.alpha, 
				MathUtils.equals(wins[0].coef, 0));
		
		return wins[0];
	}	
	
	/*update rule sama dengan yang wavelet*/
	protected void adjustWeights(FCodeBook codebook, FEntry code, 
			Entry input, double alpha, boolean unknown) {
		if (unknown){
			double DELTA = 1.01d;
			for(int i=0; i < codebook.size();i++){
				FEntry cbe = codebook.get(i);
				for(int j=0;j < cbe.size();j++){
					cbe.data[j].min = cbe.data[j].mean - DELTA * (cbe.data[j].mean - cbe.data[j].min);
					cbe.data[j].max = cbe.data[j].mean + DELTA * (cbe.data[j].max - cbe.data[j].mean);
					
//					Assert.assertTrue("unknown winner, " + j + "-" + i + " " +
//							cbe.data[j].max + " " + cbe.data[j].min,
//							cbe.data[j].max > cbe.data[j].min);
				}
			}
//			System.out.println("masuk");
		}
		
		//equals
		else if (code.label == input.label){
			//shift winning cluster toward the target
			Entry cmiu = this.network.miu.findEntry(code.label);
			for(int j=0;j < code.size();j++){
				FuzzyNode fn = code.data[j];
				double miu 	 = cmiu.data[j];
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
				
				/*limit perlebaran sebatas range nilai min data dan max data*/
//				if(code.data[j].min < lLimit)
//					code.data[j].min = lLimit;
//				if(code.data[j].max > uLimit)
//					code.data[j].max = uLimit;
				
//				Assert.assertTrue("true winner, " + j +
//						code.data[j].max + code.data[j].min,
//						code.data[j].max > code.data[j].min);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			Entry cmiu = this.network.miu.findEntry(code.label);
			for(int j=0;j < code.size();j++){
				FuzzyNode fn = code.data[j];
				double miu 	 = cmiu.data[j];
				double in 	 = input.data[j];
				
				double oldMean = fn.mean;
				fn.mean = fn.mean - alpha * (1 - miu) * (in - fn.mean);
				double jarak = fn.mean - oldMean;
				fn.min  = fn.min + jarak;
				fn.max  = fn.max + jarak;
				 			
				double sempit = fn.mean - fn.min;
				fn.min = fn.mean - sempit * (1 - (1 - miu) * alpha);
				fn.max = fn.mean + sempit * (1 - (1 - miu) * alpha);
				
//				Assert.assertTrue("false winner, " + j ,code.data[j].max > code.data[j].min);
			}
		}			
	}
	
	protected void adjustWeightsOld(FCodeBook codebook, FEntry code, 
			Entry input, double alpha, boolean unknown) {

//		double EXTF = 0.5d;
//		double lLimit = xMin - (EXTF * (xMax - xMin));
//		double uLimit = xMax + (EXTF * (xMax - xMin));
//		
		//unknown class (-1)
		if (unknown){
			double DELTA = 1.001d;
			for(int i=0; i < codebook.size();i++){
				FEntry cbe = codebook.get(i);
				for(int j=0;j < cbe.size();j++){
					cbe.data[j].min = cbe.data[j].mean - DELTA * (cbe.data[j].mean - cbe.data[j].min);
					cbe.data[j].max = cbe.data[j].mean + DELTA * (cbe.data[j].max - cbe.data[j].mean);
					
					/*limit perlebaran sebatas 1.5 * range nilai min data dan max data*/
//					if(cbe.data[j].min < lLimit)
//						cbe.data[j].min = lLimit;
//					if(cbe.data[j].max > uLimit)
//						cbe.data[j].max = uLimit;
					
//					Assert.assertTrue("unknown winner, " + j + "-" + i + " " +
//							cbe.data[j].max + " " + cbe.data[j].min,
//							cbe.data[j].max > cbe.data[j].min);
				}
			}
//			System.out.println("masuk");
		}
		
		//equals
		else if (code.label == input.label){
			//shift winning cluster toward the target
			Entry cmiu = this.network.miu.findEntry(code.label);
			for(int j=0;j < code.size();j++){
				FuzzyNode fn = code.data[j];
				double miu 	 = cmiu.data[j];
				double in 	 = input.data[j];
				
				final double lRange = fn.mean - fn.min;
				final double rRange = fn.max - fn.mean;
				fn.mean = fn.mean + alpha * (1 - miu) * (in - fn.mean);
				fn.min  = fn.mean - lRange;
				fn.max  = fn.mean + rRange;
				 			
				//increase fuzziness (extend fuzziness)
				double BETA = 0.05d;
				fn.min = fn.min - (BETA) * lRange;
				fn.max = fn.max + (BETA) * rRange;
				
				/*limit perlebaran sebatas range nilai min data dan max data*/
//				if(code.data[j].min < lLimit)
//					code.data[j].min = lLimit;
//				if(code.data[j].max > uLimit)
//					code.data[j].max = uLimit;
				
//				Assert.assertTrue("true winner, " + j +
//						code.data[j].max + code.data[j].min,
//						code.data[j].max > code.data[j].min);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			Entry cmiu = this.network.miu.findEntry(code.label);
			for(int j=0;j < code.size();j++){
				FuzzyNode fn = code.data[j];
				double miu 	 = cmiu.data[j];
				double in 	 = input.data[j];
				
				final double lRange = fn.mean - fn.min;
				final double rRange = fn.max - fn.mean;
				fn.mean = fn.mean - alpha * (1 - miu) * (in - fn.mean);
				fn.min  = fn.mean - lRange;
				fn.max  = fn.mean + rRange;
				 			
				//decrease fuzziness (narrow fuzziness)
				double GAMMA = 0.05d;
				fn.min = fn.min + (GAMMA) * lRange;
				fn.max = fn.max - (GAMMA) * rRange;
				
//				Assert.assertTrue("false winner, " + j ,code.data[j].max > code.data[j].min);
			}
		}
	}
	
	public void reloadPreviousCode(int label){
		throw new RuntimeException("not provided");
	}

	public int getCurrEpoch() {
		return this.currEpoch;
	}

}
