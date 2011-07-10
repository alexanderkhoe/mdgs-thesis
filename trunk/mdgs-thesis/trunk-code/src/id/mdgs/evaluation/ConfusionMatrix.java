/**
 * 
 */
package id.mdgs.evaluation;

import id.mdgs.utils.MathUtils;

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
		
		output.append(String.format("TOT%sTPR%s\n",sep,sep));
		
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
			
//			double porsi  = 1.0/this.nclass;//(double)total/this.total;
			double tprate = getPerformance(r).getTPRate()*100;
//			double gtprate= porsi * tprate;
//			
//			if( r < this.nclass && !Double.isNaN(gtprate))
//				avgTPRate += gtprate;
			
			/*total data per kelas*/
			output.append(String.format("%5d%s", total, sep));
			
			/*porsi data tiap kelas per overall data*/
//			output.append(String.format("%5.4f%s", porsi, sep));
			
			/*accuracy per class*/
			output.append(String.format("%6.2f%%%s", tprate, sep));
			
//			output.append(String.format("%6.2f%%%s", gtprate, sep));
			output.append("\n");
		}
		
		
		int MAX = 7;
		StringBuilder[] sb = new StringBuilder[MAX];
		for(int i=0;i < MAX;i++) sb[i] = new StringBuilder(); 
		
		sb[0].append(String.format("TOT%s", sep));
		sb[1].append(String.format("Rec%s", sep));
		sb[2].append(String.format("Pre%s", sep));
		sb[3].append(String.format("FMe%s", sep));
		sb[4].append(String.format("Sen%s", sep));
		sb[5].append(String.format("Spe%s", sep));
		sb[6].append(String.format("Gme%s", sep));
		
		total = 0;
		double[] avg = new double[MAX];
		MathUtils.fills(avg, 0);
		
		for(int c = 0;c < this.matrix[0].length;c++){
			if(c >= this.nclass) continue;
			
			PerformanceMeasure pm = getPerformance(c); 
			double[] avgc = new double[MAX];
			MathUtils.fills(avgc, 0);
			
			avgc[0] = (pm.tp + pm.fp); 
			avgc[1] = pm.getRecall();
			avgc[2] = pm.getPrecision();
			avgc[3] = pm.getFMeasure();
			avgc[4] = pm.getRecall();
			avgc[5] = 1 - (pm.fp / (pm.fp + pm.tn));
			avgc[6] = Math.sqrt(avgc[4] * avgc[5]);
			
			for(int i=0;i < MAX;i++) if(Double.isNaN(avgc[i])) avgc[i] = 0;
			for(int i=0;i < MAX;i++) avg[i] += avgc[i];
			
			sb[0].append(String.format("%5d%s", (int)avgc[0], sep));
			sb[1].append(String.format("%5.4f%s",avgc[1], sep));
			sb[2].append(String.format("%5.4f%s",avgc[2], sep));
			sb[3].append(String.format("%5.4f%s",avgc[3], sep));
			sb[4].append(String.format("%5.4f%s",avgc[4], sep));
			sb[5].append(String.format("%5.4f%s",avgc[5], sep));
			sb[6].append(String.format("%5.4f%s",avgc[6], sep));
			
		}
		output.append(sb[0].toString() + "\n\n");
		
		for(int i=1;i < MAX;i++) avg[i] /= this.nclass;
		
		
		String[] label = {"", "Recall,TPRate", "Precision", "FMeasure", 
				"Sensitifity,Recall,TPRate", "Specificity,(1 - FPRate)", "G-Means"};
		
		for(int i=1;i < MAX;i++){
			sb[i].append(String.format("|\t%5.4f\t<-%s %s\n", avg[i], "Average", label[i]));
			output.append(sb[i].toString());
		}
		
		output.append(String.format("\nTotal True: %d\n", getTruePrediction()));
		output.append(String.format("Total Data: %d\n", getTotal()));
		output.append(String.format("Total Accuracy: %f\n", getAccuracy()));
		
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
