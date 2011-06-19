/**
 * 
 */
package id.mdgs.evaluation;

import org.encog.engine.util.EngineArray;

/**
 * Row is True Class
 * Column is Predicted Class
 * 
 * @author I Made Agus Setiawan
 *
 */
public class ConfusionMatrix {

	private int nclass;
	
	private int[][] matrix;
	private int tp;
	private int total;
	
	private String separator;
	
	public ConfusionMatrix(int nclass){
		this(nclass, "\t");
	}
	
	public ConfusionMatrix(int nclass, String sep){
		this.nclass = nclass;
		this.separator = sep;
		
		//add one class for unknown 
		this.matrix = new int[nclass + 1][nclass + 1];
		
		this.reset();
	}

	public void reset(){
		this.initMatrix(0);
		this.tp = 0;		
		this.total = 0;
	}
	
	private void initMatrix(int val) {
		for(int r=0;r<this.matrix.length;r++){
			for(int c=0;c<this.matrix[0].length;c++){
				this.matrix[r][c] = val;
			}
		}
	}
	
	//BE AWARE WITH BASE INDEX = 0
	public void feed(int output, int target) {
		validate(output, target);
		if(target == -1) target = this.nclass;
		if(output == -1) output = this.nclass;
		
		this.matrix[target][output] += 1;
		
		if(output == target) tp++;
		total++;
	}
	
	//UTILIZE PerformanceMeasure from JavaML
	public PerformanceMeasure getPerformance(int kelas){
		PerformanceMeasure out = new PerformanceMeasure();
		
		if(kelas == -1) kelas = this.nclass;
		
		for(int r=0;r<this.matrix.length;r++){
			for(int c=0;c<this.matrix[0].length;c++){
				//TP
				if(kelas == r && kelas == c){
					out.tp += this.matrix[r][c];
				} 
				
				//FN
				else if(kelas == r){
					out.fn += this.matrix[r][c];
				} 
				
				//FP
				else if(kelas == c){
					out.fp += this.matrix[r][c];
				} 
				
				//TN
				else {
					out.tn += this.matrix[r][c];
				}
			}
		}
		
		return out;
	}

	
	public double getAccuracy(){
		return (double)this.tp / (double)this.total;		
	}
	
	public int getTruePrediction(){
		return this.tp;
	}
	
	public int getTotal(){
		return this.total;
	}
		
	public String toString(){
		StringBuilder output = new StringBuilder();
		String sep = this.separator;
		
		output.append(String.format("-- CONFUSION MATRIX-------\n"));
		output.append(String.format("ROW = True, COLUMN = Prediction\n"));
		output.append(String.format("%5s%s","",sep));
		
		for(int c = 0;c < this.matrix[0].length; c++){
			if(c == this.nclass){
				output.append(String.format("%-5s%s", "C#N/A",sep));
			}
			
			else {
				output.append(String.format("%5s%s", "C#" + (new Integer(c)).toString(),sep));
			}
		}
		
		output.append(String.format("TOT%sACC\n",sep,sep));
		int total;
		for(int r = 0;r < this.matrix.length;r++){
			if(r == this.nclass){
				output.append(String.format("%-5s%s", "C#N/A",sep));
			}
			
			else {
				output.append(String.format("C#%-3d%s", r,sep));
			}
			
			total = 0;
			for(int c = 0;c < this.matrix[r].length;c++){
				output.append(String.format("%5d%s", this.matrix[r][c], sep));
				total += this.matrix[r][c];
			}
			
			/*total data per kelas*/
			output.append(String.format("%5d%s", total, sep));
			
			/*accuracy per class*/
			output.append(String.format("%6.2f%%%s", getPerformance(r).getTPRate()*100, sep));
			output.append("\n");
		}
		
		output.append(String.format("\nTotal True: %d\n", getTruePrediction()));
		output.append(String.format("Total Data: %d\n", getTotal()));
		return output.toString();
	}	
	
	
	private void validate(int output, int target) {
		if(target >= this.nclass){
			throw new RuntimeException(String.format("ConfusionMatrix: target(%d) out of range(%d) - base 0",
					target, this.nclass));
		}
		if(output >= this.nclass){
			throw new RuntimeException(String.format("ConfusionMatrix: output(%d) out of range(%d) - base 0",
					output, this.nclass));
		}
	}
}
