/**
 * 
 */
package id.mdgs.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Parameter {
	public static String ECG24_TRAIN = 
		utils.getDefaultPath() + "/resources/ecgdata/statdb8L5_24f_nonoutliers.bwr_300_gabung.txt";
	public static String ECG24_TEST = 
		utils.getDefaultPath() + "/resources/ecgdata/statdb8L5_24f_outliers.bwr_300_gabung_testonly.txt";

	public static String ECG86_TRAIN = 
		utils.getDefaultPath() + "/resources/ecgdata/wletdb8L2_86f_nonoutliers.bwr_300_gabung.txt";
	public static String ECG86_TEST = 
		utils.getDefaultPath() + "/resources/ecgdata/wletdb8L2_86f_outliers.bwr_300_gabung_testonly.txt";
	
	public static String ECG86N100_TRAIN = 
		utils.getDefaultPath() + "/resources/ecgdata/wletdb8L2_86f_outliers.bwr_300_gabung100.txt";
	public static String ECG24N100_TRAIN = 
		utils.getDefaultPath() + "/resources/ecgdata/statdb8L5_24f_nonoutliers.bwr_300_gabung100.txt";

	public static String IRIS_DATA = utils.getDefaultPath() + "/resources/iris.data";
	public static String IRIS_DATA2 = utils.getDefaultPath() + "/resources/iris.unbalance.data";
	
	public static String ECG300C15N100_TRAIN = 
		utils.getDefaultPath() + "/resources/ecgdata/trainset15c.txt";
	public static String ECG300C15N100_TEST = 
		utils.getDefaultPath() + "/resources/ecgdata/testset15c.txt";

	public static String ECG86_2_TRAIN = 
		utils.getDefaultPath() + "/resources/ecgdata/wletdb8L2_86f_nonoutliers.bwr_300_gabung.train.txt";
	public static String ECG86_2_TEST = 
		utils.getDefaultPath() + "/resources/ecgdata/wletdb8L2_86f_nonoutliers.bwr_300_gabung.test.txt";

	
	public static String DATA[] = {
		//0. 300 fit 6 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_kotor.bwr_300_gabung.test.txt",

		//1. 86 fit
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_kotor.bwr_300_gabung.test.txt",
		
		//2. 24 fit
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_kotor.bwr_300_gabung.test.txt",
		
		//3 300 fit 12 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_kotor.bwr_300_gabung.test.txt",

		//4 86 fit 12 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_kotor.bwr_300_gabung.test.txt",

		//5 24 fit 12 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_kotor.bwr_300_gabung.test.txt",

		//7 30 fit PCA 
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_nonoutliers.bwr_300_gabung.train.pca30.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_nonoutliers.bwr_300_gabung.test.pca30.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_outliers.bwr_300_gabung_test_only.pca30.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_outliers.bwr_300_gabung_test_only.pca30.txt",

		//8 30 fit PCA Norm
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_nonoutliers.bwr_300_gabung.train.pca30norm.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_nonoutliers.bwr_300_gabung.test.pca30norm.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_outliers.bwr_300_gabung_test_only.pca30norm.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000.pca/6.300f_2000_outliers.bwr_300_gabung_test_only.pca30norm.txt",
	};	
	
	public static String DATA_EXT[] = {
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.3.24.2000Samples/3.24f_2000_nonoutliers.bwr_300_gabung.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.3.24.2000Samples/3.24f_2000_kotor.bwr_300_gabung.txt",
	};
	
	public static String DATA_IRIS[] = {
		utils.getDefaultPath() + "/resources/trash/iris.train.data",
		utils.getDefaultPath() + "/resources/trash/iris.test.data",
		utils.getDefaultPath() + "/resources/trash/iris.test.data",
		utils.getDefaultPath() + "/resources/trash/iris.test.data"
	};
	
	public static String DATA_UCI[] = {
		utils.getDefaultPath() + "/resources/ucidata/13.6f.ucidb.txt.train.txt",
		utils.getDefaultPath() + "/resources/ucidata/13.6f.ucidb.txt.test.txt",
		utils.getDefaultPath() + "/resources/ucidata/4.6f.ucidb.txt.train.txt",
		utils.getDefaultPath() + "/resources/ucidata/4.6f.ucidb.txt.test.txt",
		utils.getDefaultPath() + "/resources/ucidata/else.6f.ucidb.txt",
	};
	
	public static String DATA_ALL[] = {
		//0. 300 fit 6 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_kotor.bwr_300_gabung.test.txt",

		//1. 86 fit
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_kotor.bwr_300_gabung.test.txt",
		
		//2. 24 fit
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_kotor.bwr_300_gabung.test.txt",
		
		//3 300 fit 12 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.300.2000Samples/12.300f_2000_kotor.bwr_300_gabung.test.txt",

		//4 86 fit 12 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_kotor.bwr_300_gabung.test.txt",

		//5 24 fit 12 class
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_nonoutliers.bwr_300_gabung.test.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_kotor.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.24.2000Samples/12.24f_2000_kotor.bwr_300_gabung.test.txt",
	};
	
	
	public static String DATA_DECOMPOSE[] = {
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.300.2000Samples/6.300f_2000_nonoutliers.bwr_300_gabung.test.txt",
		
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.157.2000Samples/6.157f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.157.2000Samples/6.157f_2000_nonoutliers.bwr_300_gabung.test.txt",

		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.86.2000Samples/6.86f_2000_nonoutliers.bwr_300_gabung.test.txt",
		
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.50.2000Samples/6.50f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.50.2000Samples/6.50f_2000_nonoutliers.bwr_300_gabung.test.txt",		

		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.32.2000Samples/6.32f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.32.2000Samples/6.32f_2000_nonoutliers.bwr_300_gabung.test.txt",
		
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.23.2000Samples/6.23f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.23.2000Samples/6.23f_2000_nonoutliers.bwr_300_gabung.test.txt",
		
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_nonoutliers.bwr_300_gabung.train.txt",
		utils.getDefaultPath() + "/resources/ecgdata/with-header/data.6.24.2000Samples/6.24f_2000_nonoutliers.bwr_300_gabung.test.txt",
	};
}

