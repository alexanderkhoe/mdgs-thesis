/**
 * 
 */
package id.mdgs.neural.data;

import org.encog.mathutil.randomize.RangeRandomizer;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FuzzyNode implements Node {
	public double min, mean, max;
	
	public FuzzyNode(){
		this.reset();
	}

	public FuzzyNode(Node node){
		this.copy(node);
	}
	
	public FuzzyNode(final double[] val){
		set(val);
	}
	
	public FuzzyNode(double min, double mean, double max){
		set(min, mean, max);
	}
	
	private void set(double min, double mean, double max){
		this.min = min;
		this.mean = mean;
		this.max = max;			
	}
	
	@Override
	public void reset(){
		set(0D, 1D, 2D);			
	}
	
	public void randomize(double min, double max){
		double d = max - min;
		double d33 = 0.33 * d; 
		set(RangeRandomizer.randomize(min, min + d33),
			RangeRandomizer.randomize(min + d33, max - d33),
			RangeRandomizer.randomize(max-d33, max));
	}		
	
	@Override
	public void set(final double[] val){
		assert(val.length >= 3);
		set(val[0], val[1], val[2]);
	}

	/**
	 * clone create new object with same value 
	 * as current object
	 * 
	 */
	@Override
	public FuzzyNode clone(){
		return new FuzzyNode(this);
	}
	
	@Override
	public String toString(){
		return String.format("[ %8.4f, %8.4f, %8.4f] => w = %8.4f", min,mean,max, max-min);
	}

	/**
	 * copying parameter object value without creating
	 * new object.
	 * 
	 * @param fn
	 */
	@Override
	public void copy(Node node) {
		if(node.getClass() != FuzzyNode.class)
			throw new RuntimeException("Parameter should be FuzzyNode Object");

		FuzzyNode fn = (FuzzyNode) node;
		set(fn.min, fn.mean, fn.max);
	}

	
	@Override
	public double getMaxIntersection(Node node) {
		if(node.getClass() != FuzzyNode.class)
			throw new RuntimeException("Parameter should be FuzzyNode Object");
		
		FuzzyNode fn = (FuzzyNode) node;
		double miu, tp1, tp2;
		
		//never use == to check equality of double in JAVA
		if(Math.abs(this.mean - fn.mean) < 0.00001){
			miu = 1;
		}
		
		else {
			if(this.mean > fn.mean){
				double posisiX = (this.mean - this.min) * fn.max;
				double penyebut= (fn.max - fn.mean);
				posisiX /= penyebut;
				posisiX += this.min;
				posisiX /= (1 + ((this.mean - this.min)/(fn.max - fn.mean)));
				tp1 = (this.max - posisiX)/(this.max - this.mean);
				tp2 = (fn.max - posisiX)/(fn.max - fn.mean);
			}
			
			else {
				double posisiX = (fn.mean - fn.min) * this.max;
				double penyebut= (this.max - this.mean);
				posisiX /= penyebut;
				posisiX += fn.min;
				posisiX /= (1 + ((fn.mean - fn.min)/(this.max - this.mean)));
				tp1 = (this.max - posisiX)/(this.max - this.mean);
				tp2 = (fn.max - posisiX)/(fn.max - fn.mean);
			}			
			
			if(tp1 > 0 && tp1 <= 1){
				miu = tp1;
			} 
			
			else if(tp2 > 0 && tp2 <= 1){
				miu = tp2;
			}
			
			else {
				miu = 0;
			}
		}
		
		return miu;
	}
	
	@Override
	public double getMaxIntersection(double x){
		double miu = 0;
		
		//triangle function
		if(x <= min || x >= max)	miu = 0;
		else if(x <= mean)			miu = (x - min) / (mean - min);
		else						miu = (max - x) / (max - mean);
		
		return miu;
	}
	
}
