package id.mdgs.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import id.mdgs.utils.CombinationGenerator;
import id.mdgs.utils.MathUtils;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class IOMatrix {
	private int nSample, nAlgo;
	
	private int[][] rawMatrix;
	private int[] calcMatrix;
	private File flog;
	private String separator;
	
	public IOMatrix(int nSample, int nAlgo, File log){
		this(nSample, nAlgo,log,  "\t");

	}
	
	public IOMatrix(int nSample, int nAlgo, File log, String sep){
		this.nSample = nSample;
		this.nAlgo	 = nAlgo;
		this.separator = sep;
		flog = log;
		
		this.rawMatrix = new int[nSample][nAlgo];
		//1 kolom akhir buat total sum
		this.calcMatrix = new int[(int) Math.pow(2, nAlgo)];
		
		this.reset();
	}

	public void reset(){
		MathUtils.fills(rawMatrix, 0);
		MathUtils.fills(calcMatrix, 0);
	}
	
	//BE AWARE WITH BASE INDEX = 0
	public void feed(int idSample, int idAlgo, boolean result) {
		validate(idSample, idAlgo);
		
		this.rawMatrix[idSample][idAlgo] = result ? 1 : 0;
	}
	

	public void summarized(){
		MathUtils.fills(calcMatrix, 0);
		
		for(int r=0;r < rawMatrix.length;r++){
			int pos = 0;
			int bitmask = 1;
			
			for(int c=rawMatrix[0].length - 1; c >= 0; c--){
				if(rawMatrix[r][c] == 1){
					pos |= bitmask;
				}
				
				bitmask <<= 1;
			}
			
			calcMatrix[pos]++;
		}
	}
	
	private void validateColId(int id){
		if(id >= nAlgo)
			throw new IndexOutOfBoundsException("Column failed");
	}
	
	private void validateRowId(int id){
		if(id >= nSample)
			throw new IndexOutOfBoundsException("Sample failed");
	}
	
	public double getAccuracy(int id){
		validateColId(id);
		
		int TP = 0;
		
		for(int i=0;i < rawMatrix.length;i++){
			if(rawMatrix[i][id] == 1){
				TP++;
			}
		}
		
		return ((double) TP) / nSample;
	}
	
	public double getMcNemarCoef(int idAlgo1, int idAlgo2, int[] mat){
		validateColId(idAlgo1);
		validateColId(idAlgo2);
		
		int N00 = 0, 
			N01 = 0, bitmask01 = 0,
			N10 = 0, bitmask10 = 0,
			N11 = 0; 
		int mask = 0, bitmask = 1;
		
		double X2 = 0;
		
		for(int i=nAlgo-1; i >= 0; i--){
			if(idAlgo1 == i ){
				//bitmask10
				bitmask10 |= bitmask;
				
				mask |= bitmask;
			}
	
			if(idAlgo2 == i){
				//bitmask01
				bitmask01 |= bitmask;
				
				mask |= bitmask;
			}
			
			bitmask <<= 1;
		}
		
		for(int i=0;i < calcMatrix.length;i++){
			if((mask & i) == 0)					N00 += calcMatrix[i];
			else if((mask & i) == bitmask01)	N01 += calcMatrix[i];
			else if((mask & i) == bitmask10)	N10 += calcMatrix[i];
			else if((mask & i) == mask)			N11 += calcMatrix[i];
		}
		
//		System.out.println(printConfusion(N00, N01, N10, N11));
		mat[0] = N00;	mat[1] = N01;
		mat[2] = N10;	mat[3] = N11;
		
		X2 = Math.pow((Math.abs(N01 - N10) - 1), 2) / (N01 + N10); 
		return X2;
	}
	
	public String printConfusion(int[] mat){
		return printConfusion(mat[0],mat[1],mat[2],mat[3]);
	}
	
	private String printConfusion(int n00, int n01, int n10, int n11){
		StringBuilder output = new StringBuilder();
		
		output.append(String.format("-- CONF MATRIX-------\n"));
		output.append(String.format("%5s\t%5s\t%s\n", "", "D2(+)", "D2(-)"));
		output.append(String.format("%5s\t%5d\t%d\n", "D1(+)", n11, n10));
		output.append(String.format("%5s\t%5d\t%d\n", "D1(-)", n01, n00));
		return output.toString();
	}
	
	public String toString(){
		StringBuilder output = new StringBuilder();
		String sep = this.separator;
		
		output.append(String.format("-- INPUT/OUTPUT MATRIX-------\n"));
		output.append(String.format("ROW = Combination, COLUMN = Algorithm\n"));
		for(int c=0;c < nAlgo; c++)
			output.append(String.format("%5s%s","D"+Integer.toString(c),sep));
		
		output.append("\n");
		
		for(int r = 0;r < this.calcMatrix.length; r++){
			int mask = 1; mask <<= (nAlgo-1);
			for(int c=0;c < nAlgo; c++){
				if((r & mask) == mask)
					output.append(String.format("%5d%s", 1,sep));
				else
					output.append(String.format("%5d%s", 0,sep));
				
				mask >>= 1;
			}
			
			output.append(String.format("%5d%s\n", calcMatrix[r],sep));
		}
		
		for(int c=0;c < nAlgo; c++){
			output.append(String.format("%5.2f%s", this.getAccuracy(c) * 100,sep));
		}
		
		output.append("\n\n");
		output.append("McNemar Score\n");	
		CombinationGenerator gen = new CombinationGenerator(nAlgo, 2);
		while(gen.hasMore()){
			int[] idx = gen.getNext();
			int[] mat = new int[4];
			
			double X2 = this.getMcNemarCoef(idx[0], idx[1], mat);
			
			output.append(String.format("X2[D%d,D%d] = %7.4f\n", idx[0], idx[1], X2));
			output.append(printConfusion(mat));
			output.append("\n");
		}
		
		return output.toString();
	}	
	
	public void logAll() throws FileNotFoundException{
		StringBuilder output = new StringBuilder();
		String sep = this.separator;
		
		PrintWriter pw = new PrintWriter(flog);
		
		pw.write(this.toString() + "\n");
		
		pw.write(String.format("-- INPUT/OUTPUT RAW MATRIX-------\n"));
		pw.write(String.format("ROW = Sample, COLUMN = Algorithm\n"));
		pw.write(String.format("%5s%s","",sep));
		for(int c=0;c < nAlgo; c++)
			pw.write(String.format("%5s%s","D"+Integer.toString(c),sep));
		
		pw.write("\n");
		
		int[] sum = new int[this.rawMatrix[0].length];
		MathUtils.fills(sum, 0);
		
		for(int r = 0;r < this.rawMatrix.length; r++){
			pw.write(String.format("%5d%s", r,sep));
			
			for(int c = 0;c < this.rawMatrix[0].length; c++){
				pw.write(String.format("%5d%s", rawMatrix[r][c],sep));
				
				if(rawMatrix[r][c] == 1){
					sum[c]++;
				}
			}
			pw.write("\n");
		}
		
		pw.write(String.format("%5s%s","TOT",sep));
		for(int c = 0;c < this.rawMatrix[0].length; c++){
			pw.write(String.format("%5d%s", sum[c],sep));
		}
		
		pw.flush();
		pw.close();
	}
	
	private void validate(int idSample, int idAlgo) {
		validateColId(idAlgo);
		validateRowId(idSample);
	}
}
