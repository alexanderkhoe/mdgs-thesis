/**
 * 
 */
package id.mdgs.neural.utils;


import id.mdgs.neural.fnlvq.crisp.FnlvqCrisp;
import id.mdgs.neural.fnlvq.crisp.TrainFnlvqCrispMsa;

import org.encog.engine.util.EngineArray;
import org.encog.mathutil.matrices.Matrix;

/**
 * @author I Made Agus Setiawan
 *
 */
public class MSACalculation {

	public static final double DEFAULT_THRESHOLD = 0.9;
	public String separator = "\t";
	/**
	 * ROW = Category
	 * COLUMN = Output Node
	 */
	private Matrix matrix;
	
	private int[] categoryCounter;
	
	private double[] similarities;
	private double[] prevSimilarities;
	
	private boolean ready;
	
	private double threshold;
	
	private boolean shouldStop;
	
	private boolean once;
	
	private TrainFnlvqCrispMsa trainer;
	
	public MSACalculation(final TrainFnlvqCrispMsa trainFnlvqMsa){
		this(trainFnlvqMsa, MSACalculation.DEFAULT_THRESHOLD);
	}
	
	public MSACalculation(final TrainFnlvqCrispMsa trainer, double threshold){
		int numclass = ((FnlvqCrisp) trainer.getNetwork()).getOutputCount();
		
		this.trainer = trainer;
		this.matrix = new Matrix(numclass, numclass);
		this.categoryCounter = new int[numclass];
		this.similarities = new double[numclass];
		this.prevSimilarities = new double[numclass];
		this.setThreshold(threshold);
		
		this.resetAll();
	}
	
	public void setThreshold(double th){
		this.threshold = th;
	}
	
	public void resetAll(){
		reset();
		this.once = false;
	}
	
	public void reset(){
		this.matrix.set(0.0);
		
		for(int c=0;c<this.categoryCounter.length;c++)
			this.categoryCounter[c] = 0;
		
		this.ready = false;
	}
	
	//this is Rohmat version
	public void updateSimilarities(int category, double[] similarities){
		validate(category, similarities.length);
		for(int c=0; c < similarities.length; c++){
			this.matrix.add(category, c, similarities[c]);
		}
		
		this.categoryCounter[category] += 1;
		this.ready = false;
	}
	
	//this is Sesepuh version
	public void updateSimilarities(int category, int winner, double winningSimilarity){
		validate(category, winner);
		this.matrix.add(category, winner, winningSimilarity);
		
		this.categoryCounter[category] += 1;
		this.ready = false;
	}

	public boolean shouldStop(){
		return this.shouldStop;
	}
	
	private void keepSimilarities(double[] sim){
		for(int c=0;c<sim.length;c++)
			this.prevSimilarities[c] = sim[c];
	}
	
//	protected void inferSimilarities(){
//		for(int c=0;c<this.matrix.getCols();c++){
//			double total = 0.0;
//			for(int r=0;r<this.matrix.getRows();r++){
//				if(r == c)
//					total += this.matrix.get(r, c);
//				else
//					total -= this.matrix.get(r, c);
//			}
//			this.similarities[c] = total;
//		}		
//	}
	
	/**
	 * from Elly, to infer the total similarity
	 * use formula: T = diag - (1 - diag)
	 * 				  = 2*diag - 1;
	 */
	protected void inferSimilarities(){
		for(int c=0;c<this.matrix.getCols();c++){
			double total = 0.0;
			for(int r=0;r<this.matrix.getRows();r++){
				if(r == c)
					total += this.matrix.get(r, c);
			}
			this.similarities[c] = 2 * total - 1;
		}		
	}
	
	public void calculateSimilarities(){
		//calc average
		for(int c=0;c<this.matrix.getCols();c++){
			for(int r=0;r<this.matrix.getRows();r++){
				double avg = this.matrix.get(r, c) / this.categoryCounter[r];
				this.matrix.set(r, c, avg);
			}
		}
		
		this.inferSimilarities();
		
		//calculate stop suggestion
		this.shouldStop = true;
		for(int r=0;r<this.similarities.length;r++){
			if(this.similarities[r] < this.threshold){
				this.shouldStop = false;
				break;
			}
		}
		
		this.ready = true;
		
		//make initial previous similarities = 0
		if(!this.once){
			for(int c=0;c<prevSimilarities.length;c++)
				this.prevSimilarities[c] = 0;
			this.once = true;
		}
		
		//compare with prev similarity, reload if better than current
		for(int c = 0;c < this.similarities.length;c++){
			if (this.prevSimilarities[c] > this.similarities[c]){
				this.trainer.reloadPreviousWeights(c);
			}
		}
		
		//keep the similarity
		this.keepSimilarities(this.similarities);
	}
	
	public void validate(int rows, int cols){
		if(rows >= this.matrix.getRows()){
			throw new RuntimeException(
					String.format("MSACalculation: category(%d) out of range(%d)", 
							rows, this.matrix.getRows()));
		}
		
		if(cols >= this.matrix.getCols()){
			throw new RuntimeException(
					String.format("MSACalculation: winner(%d) out of range(%d)", 
							rows, this.matrix.getCols()));
		}
		
//		if(cols >= this.matrix.getCols()){
//			throw new RuntimeException(
//					String.format("MSACalculation: number of similarities value(%d) did not match(%d)", 
//							cols, this.matrix.getCols()));
//		}
	}
	
	public String toString(){
		StringBuilder output = new StringBuilder();
		String sep = this.separator;
		
		output.append(String.format("-- MSA MATRIX-------\n"));
		output.append(String.format("ROW = Category/Class, COLUMN = Output Layer\n"));
		output.append(String.format("%5s%s","",sep));
		
		for(int c = 0;c < this.matrix.getCols(); c++){
			output.append(String.format("%7s%s", "O#" + (new Integer(c)).toString(),sep));
		}
		
		output.append("\n");
		
		for(int r = 0;r < this.matrix.getRows();r++){
			output.append(String.format("C#%-3d%s", r,sep));
			
			for(int c = 0;c < this.matrix.getCols();c++){
				output.append(String.format("%7.4f%s", this.matrix.get(r, c), sep));
			}
			output.append("\n");
		}

		//total similarities
		output.append(String.format("%s%s", "TOTAL",sep));
		for(int c = 0;c < this.similarities.length;c++){
			output.append(String.format("%7.4f%s", this.similarities[c], sep));
		}
		output.append("\n");
		
		return output.toString();		
	}
}
