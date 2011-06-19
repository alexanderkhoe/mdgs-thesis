package id.mdgs.utils;

import org.junit.Test;


public class TestUtils {

	@Test
	public void testRandPerm(){
		int[] perm;
			
		for(int x = 0; x < 20; x++){
			perm = MathUtils.randPerm(10, 10);			
			for(int i = 0; i < perm.length; i++)
				System.out.print(perm[i] + " ");
			
			System.out.println();
		}
	}
	
	@Test
	public void testRandomDouble(){
		for(int i=0;i<50;i++){
			System.out.println(MathUtils.randomDouble(-10.270000,10.743000));
		}
	}
	
	@Test
	public void testMathUtilsNorm(){
		System.out.println("testMathUtilsNorm");
		for(double i=-1;i<=1;i+=0.1){
			System.out.println(MathUtils.norm(-1, 1, i));
		}
		
	}
}
