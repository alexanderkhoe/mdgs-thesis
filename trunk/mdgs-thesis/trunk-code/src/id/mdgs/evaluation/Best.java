package id.mdgs.evaluation;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class Best {
	public static class CBest {
		public Dataset codebook;
		public double  coef;
		public int epoch;
		
		public CBest(Dataset ds) {
			codebook = new Dataset(ds);
			coef = Double.MAX_VALUE;
			epoch = 0;
		}
		public void evaluate(Dataset codes, double err, int epoch){
			if(coef > err){
				coef = err;
				codebook.set(codes);
				this.epoch = epoch;
			}
		}
	}
	
	public static class FBest {
		public FCodeBook codebook;
		public double  coef;
		public int epoch;
		
		public FBest(FCodeBook ds) {
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
}