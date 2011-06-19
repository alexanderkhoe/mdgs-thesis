/**
 * 
 */
package id.mdgs.utils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.encog.mathutil.matrices.Matrix;

/**
 * @author I Made Agus Setiawan
 *
 */
public class MathUtils {

	/*euclid distance*/
	public static double euclideDistance(double[] v1, double[] v2){
		return Math.sqrt(squaredEuclideDistance(v1, v2));
	}

	public static double squaredEuclideDistance(double[] v1, double[] v2){
		assert(v1.length == v2.length);
		
		double difference = 0;
		
		for(int x = 0;x < v1.length;x++){
			double diff = v1[x] - v2[x];
			difference += diff * diff; 
		}
		
		return difference;
	}


	/*mahalanobis distance*/
	public static double mahalanobisDistance(double[] v1, double[] v2, RealMatrix invS){
		assert(v1.length == v2.length);
		
        double[][] diff = new double[v1.length][1];
        for (int j = 0; j < 1; j++) {
            for (int i = 0; i < v1.length; i++) {
                diff[i][j] = v1[i] - v2[i];
            }
        }
		RealMatrix M 	= new Array2DRowRealMatrix(diff); 
		RealMatrix MT 	= M.transpose();
		
		RealMatrix temp1 = MT.multiply(invS).multiply(M);
		assert(temp1.getRowDimension() == 1 && temp1.getColumnDimension() == 1);
		
		return temp1.getTrace();
	}
	
	
	
	public static double sigmoid(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}

	/**
	 * normalize value from range min-max to 0-1
	 * @param min
	 * @param max
	 * @param val
	 * @return
	 */
	public static double norm(double min, double max, double val){
		double nval = (val + (0 - min)) / (max - min);
		return 1.0 - nval;//(nval / (1 + nval));
	}

	
	/*math utils*/
	/**
	 * return ramdom number between min (inclusive) and max (inclusive)
	 */
	public static int randomInt(int min, int max){
		assert(max > min);
		
		final double range = (max + 1) - min;
		return (int) ((range * Math.random()) + min);
	}
	
	public static double randomDouble(double min, double max){
		assert(max > min);
		
		final double range = max - min;
		return ((range * Math.random()) + min);
	}	
	
	/**
	 * 
	 * @param num
	 * @param max
	 * @return
	 */
	public static int[] randPerm(int num, int max){
		int[] out = new int[num];
		int[] table = new int[max];
		
		for(int i = 0;i < max;i++) table[i] = i;
		for(int i = 0;i < num;i++) {
			int j = i + randomInt(0, (max-1) - i);
			int old = table[i];
			table[i]= table[j];
			table[j]= old;
		}
		for(int i = 0; i < num;i++){
			out[i] = table[i];
		}
		
		return out;
	}

	/*max*/
	public static int maxIndex(double[] data){
		assert(data.length > 0);
		
		int max = 0;
		
		for(int i=1;i<data.length;i++){
			if(data[max] < data[i])
				max = i;
		}
		
		return max;
	}
	
	public static double max(double[] data){
		assert(data.length > 0);
		
		return data[maxIndex(data)]; 
	}
	
	public static double max(double d1, double d2){
		if(d1 > d2) return d1;
		else return d2;
	}
	
	/*min*/
	public static int minIndex(double[] data){
		assert(data.length > 0);
		
		int min = 0;
		
		for(int i=1;i<data.length;i++){
			if(data[min] > data[i])
				min = i;
		}
		
		return min;
	}
	
	public static double min(double[] data){
		assert(data.length > 0);
		
		return data[minIndex(data)]; 
	}
	
	/*mean*/
	public static double mean(double[] data){
		assert(data.length > 0);
		
		double mean = 0;
		
		for(int i=1;i<data.length;i++){
			mean += data[i];
		}
		
		return mean/data.length;		
	}
	
	private final static double EPSILON = 0.00001;
	public static boolean equals(double actual, double expected){
		return actual == expected ? true : Math.abs(actual - expected) < EPSILON;
	}
	
	public static boolean equals(double actual, double expected, double eps){
		return actual == expected ? true : Math.abs(actual - expected) < eps;
	}
}
