/**
 * 
 */
package id.mdgs.fwlvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.FuzzyNode;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.fnlvqold.Fnlvq;
import id.mdgs.fnlvqold.TrainFnlvq;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFwlvq extends TrainFnlvq {
	/**/
	public static class Best {
		public FCodeBook codebook;
		public double  coef;
		public int epoch;
		public Best(FCodeBook ds) {
			codebook = new FCodeBook(ds);
			coef = Double.MAX_VALUE;
			epoch = 0;
		}
		public void evaluate(FCodeBook codes, double err, int epoch){
			if(coef > err){
				coef = err;
				codebook.set(codes);
				this.epoch = epoch;
			}
		}
	}
	public Best bestCodebook;
	
	/**/
	public TrainFwlvq(Fnlvq network, Dataset training, double learningRate) {
		super(network, training, learningRate);
		
		this.bestCodebook = new Best(network.codebook);
	}

	public boolean shouldStop(){
		return currEpoch >= maxEpoch;
	}
	
	public void iteration1(){
		/*iterasi 1*/
		int N = 0; int True = 0;
		for(Entry sample: training){
			FWinnerInfo wi;
			wi = train(this.network.codebook, sample);
			N++;
			if(!MathUtils.equals(wi.coef, ((Fwlvq)this.network).wavelet(0)) && wi.winner.label == sample.label) True++;
		}
		
		updateLearningRate();
		setError(1 - ((double)True/N));
		this.currEpoch++;
		
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}
	
	@Override
	public void iteration3() {
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
				
				if(!MathUtils.equals(wi.coef, ((Fwlvq)this.network).wavelet(0)) && wi.winner.label == sample.label) True++;
			}
		}
		
		updateLearningRate();
		setError(1 - ((double)True/N));
		this.currEpoch++;
		
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}
	
	@Override
	protected FWinnerInfo train(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wins;
		
		wins = this.network.findWinner(codebook, input, codebook.size());
		
		/*adjust code vector*/
		adjustWeights(codebook, wins[0].winner, input, this.alpha, 
				MathUtils.equals(wins[0].coef, ((Fwlvq)this.network).wavelet(0)));
		
		return wins[0];
	}

	/* (non-Javadoc)
	 * @see id.mdgs.fnlvq.TrainFnlvq#adjustWeights(id.mdgs.fnlvq.FCodeBook, id.mdgs.fnlvq.FCodeBook.FEntry, id.mdgs.lvq.Dataset.Entry, double, boolean)
	 */
	@Override
	protected void adjustWeights(FCodeBook codebook, FEntry code, Entry input,
			double alpha, boolean unknown) {
		
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

//	protected double wavelet(double bobot){
//		return Math.pow(2.0*Math.PI/Math.sqrt(3),-1.0/4) * (1.0-bobot) *
//			   Math.exp((bobot*bobot)/2);
//	}
//	
//	protected FWinnerInfo[] findWinner(FCodeBook codebook, Entry sample, int num){
//		FWinnerInfo[] winner;
//		int i;
//		winner = new FWinnerInfo[num];
//		for(i=0;i < winner.length;i++){
//			winner[i] = new FWinnerInfo();
//			//sort descending, set coef to minimum
//			winner[i].coef = Double.MAX_VALUE;
//		}
//		
//		Iterator<FEntry> eIt = codebook.iterator();
//		Iterator<Entry>  mIt = this.network.miu.iterator();
//		
//		double totScore = 0;
//		while(eIt.hasNext()) {
//			double score = 0;
//			FEntry code = eIt.next();
//			Entry sim = mIt.next();
//			
//			for(int j=0;j < code.size();j++){
//				sim.data[j] = code.data[j].getMaxIntersection(sample.data[j]);
//			}
//			
//			score = MathUtils.min(sim.data);
//			totScore += score;
//			
//			if(MathUtils.equals(totScore, 0)) score = 0;
//			else score = score / totScore;
//			
//			score = wavelet(score);
//			
//			/*sort asc*/
//			for(i=0; (i < winner.length) && (score > winner[i].coef); i++);
//			
//			if(i < winner.length){
//				for(int j=winner.length - 1;j > i;j--){
//					winner[j].copy(winner[j-1]);
//				}
//				winner[i].coef 	 = score;
//				winner[i].winner = code;
//			}
//		}
//		
//		return winner;		
//	}

	/* (non-Javadoc)
	 * @see id.mdgs.fnlvq.TrainFnlvq#updateLearningRate()
	 */
	@Override
	public void updateLearningRate() {
		//this.alpha *= 0.99d;
		this.alpha = alphaStart * (1 - (currEpoch/maxEpoch));
	}

	
}
