package libtest;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;

public class testApacheMath {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double data[] = { 0, 1, 2};
		double dS[][] = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
		
		RealMatrix M = new Array2DRowRealMatrix(data);
		RealMatrix MT= M.transpose();
		
		RealMatrix M1 = M.scalarAdd(5);
		RealMatrix M2 = M1.subtract(M);
		RealMatrix MC = M1.copy();
		
		RealMatrix MdS = new RealMatrixImpl(dS);
		RealMatrix M3 = MdS.multiply(M);
	}

}
