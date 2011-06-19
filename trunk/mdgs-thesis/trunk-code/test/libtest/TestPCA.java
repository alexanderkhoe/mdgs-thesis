package libtest;

import org.junit.Test;

import javastat.multivariate.PCA;


public class TestPCA {

	@Test
	public void testPca(){
		double[][] data = {
			{2.5 ,0.5 ,2.2 ,1.9 ,3.1 ,2.3 ,2   ,1   ,1.5 ,1.1},
			{2.4 ,0.7 ,2.9 ,2.2 ,3.0 ,2.7 ,1.6 ,1.1 ,1.6 ,0.9}
		};
		double[][] datanorm = {
				{0.69  ,-1.31 ,0.39  ,0.09  ,1.29  ,0.49  ,0.19  ,-0.81 ,-0.31 ,-0.71},
				{0.49 ,-1.21 ,0.99 ,0.29 ,1.09 ,0.79 ,-0.31 ,-0.81 ,-0.31 ,-1.01}
		};
		
		double[][] testscores = { {36, 62, 31, 76, 46, 12, 39, 30, 22, 9, 32,
            40, 64, 36, 24, 50, 42, 2, 56, 59, 28, 19,
            36, 54, 14},
           {58, 54, 42, 78, 56, 42, 46, 51, 32, 40, 49,
            62, 75, 38, 46, 50, 42, 35, 53, 72, 50, 46,
            56, 57, 35},
           {43, 50, 41, 69, 52, 38, 51, 54, 43, 47, 54,
            51, 70, 58, 44, 54, 52, 32, 42, 70, 50, 49,
            56, 59, 38},
           {36, 46, 40, 66, 56, 38, 54, 52, 28, 30, 37,
            40, 66, 62, 55, 52, 38, 22, 40, 66, 42, 40,
            54, 62, 29},
           {37, 52, 29, 81, 40, 28, 41, 32, 22, 24, 52,
            49, 63, 62, 49, 51, 50, 16, 32, 62, 63, 30,
            52, 58, 20} };
		
//		PCA pca = new PCA();
//		double[][] components = pca.principalComponents(testscores);
		
		PCA pca2 = new PCA(1.0,"covariance",testscores);
		double[][] components = pca2.principalComponents;
		//components = testscores;
		
		for(int i=0;i < components.length;i++){
			for(int j=0;j < components[0].length;j++){
				System.out.format("%7.4f ",components[i][j]);
			}
			System.out.println();
		}
	}
}
