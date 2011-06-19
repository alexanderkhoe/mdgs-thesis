package id.mdgs.utils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;


public class TestCommonMath {
	
	@Test
	public void testRealMatrix(){
		double[][] d1 = {{1, 1, 1}};
		RealMatrix M = new Array2DRowRealMatrix(d1);
		RealMatrix MT = M.transpose();
		
		RealMatrix temp = M.multiply(MT);
		System.out.println(temp.toString());
	}
}
