/**
 * 
 */
package id.mdgs.fnlvq;

import id.mdgs.fnlvq.FCodeBook.FEntry;
import id.mdgs.fnlvq.FCodeBook.FWinnerInfo;
import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.DatasetProfiler;
import id.mdgs.lvq.DatasetProfiler.PEntry;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

import org.junit.Assert;


/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvqFuzzy {

	public FnlvqF network;
	public FCodeBook fuzzyset;
	protected double alpha;
	public double alphaStart;
	protected double xi;
	protected Dataset training;
	protected DatasetProfiler profiler;
	protected double error;
	public int maxEpoch;
	public int currEpoch;	
	protected double xMin, xMax;
	
	public TrainFnlvqFuzzy(FnlvqF network, Dataset training, double learningRate) {
		this(network, training, learningRate, 5);
	}
	public TrainFnlvqFuzzy(FnlvqF network, Dataset training, double learningRate, int num) {
		this.network 	= network;
		this.training	= training;
		this.alphaStart	= learningRate;
		this.alpha		= learningRate;
		this.profiler	= new DatasetProfiler();
		init();
		fuzzyset = generateFuzzyData(training, num);
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
		profiler.run(fuzzyset);		
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
	
	public boolean shouldStop(){
		return currEpoch >= maxEpoch;
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
		
		FEntry sample = null;
		int N = 0; int True = 0;
		FWinnerInfo wi;
		for(int i=0;i<maxData;i++){
			for(PEntry pe: profiler){
//				if(i >= pe.size()) continue;
				
				int pos = pe.get(i % pe.size());
				sample = fuzzyset.get(pos);
				
				wi = train(this.network.codebook, sample);
				
				N++;
				
				if(!MathUtils.equals(wi.coef, 0) && 
						wi.winner.label == sample.label) True++;
			}
		}
//		System.out.println("E: " + utils.timer.stop());
		//System.out.print("Benar: " + Benar);
		updateLearningRate();
		//setError(avgLost/N);
		setError(1 - ((double)True/N));
		
		this.currEpoch++;
	}
	
	protected FWinnerInfo train(FCodeBook codebook, FEntry input) {
		FWinnerInfo[] wins;
		
		wins = this.network.findWinner(codebook, input, codebook.size());
//		utils.timer.start();
		/*adjust code vector*/
		adjustWeights(codebook, wins[0].miu, wins[0].winner, input, this.alpha, 
				MathUtils.equals(wins[0].coef, 0));
//		System.out.println("Time: " + utils.timer.stop());
		return wins[0];
	}	
	
	protected void adjustWeights(FCodeBook codebook, Entry mius, FEntry code, 
			FEntry input, double alpha, boolean unknown) {

//		double EXTF = 0.5d;
//		double lLimit = xMin - (EXTF * (xMax - xMin));
//		double uLimit = xMax + (EXTF * (xMax - xMin));
		
		//unknown class (-1)
		if (unknown){
			double DELTA = 1.003d;
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
			for(int j=0;j < code.size();j++){
				FuzzyNode fn 	= code.data[j];
				FuzzyNode data 	= input.data[j];
				double miu 	 	= mius.data[j];
				
				
				final double lRange = fn.mean - fn.min;
				final double rRange = fn.max - fn.mean;
				fn.mean = fn.mean + alpha * (1 - miu) * (data.mean - fn.mean);
				fn.min  = fn.mean - lRange;
				fn.max  = fn.mean + rRange;
				 			
				//increase fuzziness (extend fuzziness)
				fn.min = fn.mean - (1+0.25*(1 - miu)*this.alpha) * lRange;
				fn.max = fn.mean + (1+0.25*(1 - miu)*this.alpha) * rRange;
				
//				double BETA = 0.05d;
//				fn.min = fn.min - (BETA) * lRange;
//				fn.max = fn.max + (BETA) * rRange;				
				/*limit perlebaran sebatas range nilai min data dan max data*/
//				if(code.data[j].min < lLimit)
//					code.data[j].min = lLimit;
//				if(code.data[j].max > uLimit)
//					code.data[j].max = uLimit;
				
				Assert.assertTrue("true winner, " + j +
						code.data[j].max + code.data[j].min,
						code.data[j].max > code.data[j].min);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			for(int j=0;j < code.size();j++){
				FuzzyNode fn 	= code.data[j];
				FuzzyNode data 	= input.data[j];
				double miu 	 	= mius.data[j];
				
				final double lRange = fn.mean - fn.min;
				final double rRange = fn.max - fn.mean;
				fn.mean = fn.mean - alpha * (1 - miu) * (data.mean - fn.mean);
				fn.min  = fn.mean - lRange;
				fn.max  = fn.mean + rRange;

				//decrease fuzziness (narrow fuzziness)
				fn.min = fn.mean - (1 - (miu*this.alpha)) * lRange;
				fn.max = fn.mean + (1 - (miu*this.alpha)) * rRange;
				
//				double GAMMA = 0.05d;
//				fn.min = fn.min + (GAMMA) * lRange;
//				fn.max = fn.max - (GAMMA) * rRange;
				
				Assert.assertTrue("false winner, " + j +
						code.data[j].max + code.data[j].min,
						code.data[j].max > code.data[j].min);
			}
		}
	}	
	
	
	
	/**
	 * Fuzzify crisp data into number of group, and find it's min, mean and max
	 * if number of record = 1, the number gonna be the center and 
	 * 		min and max will be expand from center 1 unit
	 * if number of records between 2 until numOfGrouping, it will find
	 * 		min, mean and max. In addition, min and max added with 0.1 unit
	 * else it will calculate as ussual, except for the last group 
	 * 		fe: 16 records with 5 each then there are 4 groups
	 * 		The 3rd group will contain 5 records.
	 * 		AND the 4th group will contain 1 record and will 		
	 * 
	 * @param set
	 * @param num number of grouping
	 * @return
	 */
	public static FCodeBook generateFuzzyData(Dataset set, int num){
		DatasetProfiler dp = new DatasetProfiler();
	    FCodeBook fset = new FCodeBook();
	    FEntry fe = null;
	    fset.numFeatures = set.numFeatures;
	    
	    dp.run(set);
	    
	    for(PEntry pe: dp){
	    	if(pe.size() >= num){
	    		int NF = (int) Math.floor(pe.size() / num);
	    		
	    		for(int x=0;x < NF;x++){
	    			fe = new FEntry(fset.numFeatures);
	    			fe.label = pe.label;
    				for(int j=0;j < fe.size();j++){
    					fe.data[j].set(Double.POSITIVE_INFINITY, 0, 
    							Double.NEGATIVE_INFINITY); 
    				}
    				
    				int start, end;
	    			start = x*num;
	    			end   = (x+1)*num;
	    			if(x == NF-1) end = num;
	    			
					//fill in data
	    			for(int y = start;y < end;y++){
	    				Entry sample = set.get(pe.get(y));
	    				
			    		for(int j=0;j < sample.size();j++){
			    			double inval = sample.data[j];
			    			FuzzyNode node = fe.data[j];

			    			if(inval < node.min)	node.min = inval;
			    			if(inval > node.max)	node.max = inval;
			    			node.mean += inval;
			    		}
	    			}
	    			
	    			for(int j=0;j < fe.size();j++){
		    			fe.data[j].mean /= (end - start);
	    			}
	    			
	    			//add data
	    			fset.add(fe);
	    		}
	    	}
	    	
	    	else {
				if(pe.size() >= 2){
	    			//create fuzzy data
	    			fe = new FEntry(fset.numFeatures);
	    			fe.label = pe.label;
    				for(int j=0;j < fe.size();j++){
    					fe.data[j].set(Double.POSITIVE_INFINITY, 0, 
    							Double.NEGATIVE_INFINITY); 
    				}
	    			
    				//fill in data
	    			for(int y = 0;y < pe.size();y++){
	    				Entry sample = set.get(pe.get(y));
	    				
			    		for(int j=0;j < sample.size();j++){
			    			double inval = sample.data[j];
			    			FuzzyNode node = fe.data[j];

			    			if(inval < node.min)	node.min = inval;
			    			if(inval > node.max)	node.max = inval;
			    			node.mean += inval;
			    		}
	    			}
	    			
	    			for(int j=0;j < fe.size();j++){
		    			fe.data[j].mean /= pe.size();
		    			fe.data[j].min -= 0.1;
		    			fe.data[j].max += 0.1;
	    			}
	    			
	    			fset.add(fe);
				}

				else {
		    		//define data as center and expand fuzzy by 0.1
					fe = new FEntry(fset.numFeatures);
					fe.label = pe.label;
    				for(int j=0;j < fe.size();j++){
    					fe.data[j].set(Double.POSITIVE_INFINITY, 0, 
    							Double.NEGATIVE_INFINITY); 
    				}
    				
    				Entry sample = set.get(pe.get(0));
    				for(int j=0;j < sample.size();j++){
		    			double inval = sample.data[j];
		    			FuzzyNode node = fe.data[j];
		    			node.min  = inval - 1;
		    			node.mean = inval; 
		    			node.max  = inval + 1;
		    		}
		    		
    				fset.add(fe);
		    	} 
			}
		}

		return fset;
	}
}
