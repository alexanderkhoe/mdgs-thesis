/**
 * 
 */
package id.mdgs.lvq.msa;

import id.mdgs.lvq.TrainLvq1;

import org.encog.mathutil.matrices.Matrix;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Msa {

	public static final double DEFAULT_THRESHOLD = 0.9;
	public String separator = "\t";
	/**
	 * ROW = Category
	 * COLUMN = Output Node
	 */
	private Matrix matrix;
	
	private int[] categoryCounter;
	
	public double[] similarities;
	private double[] prevSimilarities;
	
	private double threshold;
	
	private boolean shouldStop;
	
	private boolean once;
	
	private TrainLvq1 trainer;
	
	public Msa(final TrainLvq1 trainer){
		this(trainer, Msa.DEFAULT_THRESHOLD);
	}
	
	public Msa(final TrainLvq1 trainer, double threshold){
		/*assume each class has single code vector*/
		int numclass = trainer.network.codebook.numEntries;
		 
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
		
	}
	
	//this is Rohmat version
	public void updateSimilarities(int category, double[] similarities){
		validate(category, similarities.length);
		for(int c=0; c < similarities.length; c++){
			this.matrix.add(category, c, similarities[c]);
		}
		
		this.categoryCounter[category] += 1;
	}
	
	//this is Sesepuh version
	public void updateSimilarities(int category, int winner, double winningSimilarity){
		validate(category, winner);
		this.matrix.add(category, winner, winningSimilarity);
		
		this.categoryCounter[category] += 1;
	}

	public boolean shouldStop(){
		return this.shouldStop;
	}
	
	private void keepSimilarities(){
		for(int c=0;c<this.similarities.length;c++)
			this.prevSimilarities[c] = this.similarities[c];
	}
	
	protected void inferSimilarities(){
		inferSimilarities1();
	}
	
	protected void inferSimilarities1(){
		for(int c=0;c<this.matrix.getCols();c++){
			double total = 0.0;
			for(int r=0;r<this.matrix.getRows();r++){
				if(r == c)
					total += this.matrix.get(r, c);
				else
					total -= this.matrix.get(r, c);
			}
			this.similarities[c] = total;
		}		
	}
	
	/**
	 * from Elly, to infer the total similarity
	 * use formula: T = diag - (1 - diag)
	 * 				  = 2*diag - 1;
	 */
	protected void inferSimilarities2(){
		for(int c=0;c<this.matrix.getCols();c++){
			double total = 0.0;
			for(int r=0;r<this.matrix.getRows();r++){
				if(r == c)
					total += this.matrix.get(r, c);
			}
			this.similarities[c] = 2 * total - 1;
		}		
	}
	
	public void averaging(){
		//calc average
		for(int c=0;c<this.matrix.getCols();c++){
			for(int r=0;r<this.matrix.getRows();r++){
				double avg = this.matrix.get(r, c) / this.categoryCounter[r];
				this.matrix.set(r, c, avg);
			}
		}
	}
	
	public void calculateSimilarities(){
		this.averaging();
		
		this.inferSimilarities();
		
		//calculate stop suggestion
		this.shouldStop = true;
		for(int r=0;r<this.similarities.length;r++){
			if(this.similarities[r] < this.threshold){
				this.shouldStop = false;
				break;
			}
		}
		
		//make initial previous similarities = 0
		if(!this.once){
			for(int c=0;c<prevSimilarities.length;c++)
				this.prevSimilarities[c] = -1;
			this.once = true;
		}
		
		//compare with prev similarity, reload if better than current
		for(int c = 0;c < this.similarities.length;c++){
			if (this.prevSimilarities[c] > this.similarities[c]){
				this.trainer.reloadPreviousCode(c);
			}
		}
		
		//keep the similarity
		this.keepSimilarities();
	}
	
	public void validate(int rows, int cols){
		if(rows >= this.matrix.getRows()){
			throw new RuntimeException(
					String.format("Msa: category(%d) out of range(%d)", 
							rows, this.matrix.getRows()));
		}
		
		if(cols >= this.matrix.getCols()){
			throw new RuntimeException(
					String.format("Msa: winner(%d) out of range(%d)", 
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
			output.append(String.format("C#%-3d%s", r, sep));
			
			for(int c = 0;c < this.matrix.getCols();c++){
				output.append(String.format("%7.4f%s", this.matrix.get(r, c), sep));
			}
			output.append(String.format("%5d%s", this.categoryCounter[r], sep));
			
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
	
	public String toString2(){
		StringBuilder output = new StringBuilder();
		String sep = this.separator;
		
		//total similarities
		for(int c = 0;c < this.similarities.length;c++){
			output.append(String.format("%7.4f%s", this.similarities[c], sep));
		}
		output.append("\n");
		
		return output.toString();		
	}	
}
