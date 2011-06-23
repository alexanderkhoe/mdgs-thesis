package id.mdgs.dataset;

import org.junit.Test;

import id.mdgs.utils.Parameter;


public class TestDataNormalization {
	@Test
	public void testDataNorm(){
		int Pos = 0 * 4;
		int nclass = 13;
		Dataset trainset = new Dataset(Parameter.DATA_UCI[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA_UCI[Pos + 1]);
		trainset.load();
		testset.load();
		
		DataNormalization norm = new ZScoreNormalization(trainset);
		System.out.println(((ZScoreNormalization)norm).std.toString()); 
		System.out.println(trainset.get(12).toString());
		norm.normalize(trainset);
		System.out.println(trainset.get(12).toString());
//		norm.normalize(testset);
		
		
	}
}
