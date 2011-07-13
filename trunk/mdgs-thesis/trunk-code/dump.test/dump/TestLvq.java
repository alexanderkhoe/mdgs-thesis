package dump;

import java.util.Iterator;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.neural.data.NodeNeuralData;
import id.mdgs.neural.data.NodeNeuralDataPair;
import id.mdgs.neural.fnlvq.fuzzy.FnlvqFuzzy;
import id.mdgs.neural.lvq.Lvq1;
import id.mdgs.neural.lvq.TrainLvq;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;
import junit.framework.Assert;

import org.encog.engine.util.EngineArray;
import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodSingle;
import org.encog.util.csv.CSVFormat;
import org.encog.util.logging.DumpMatrix;
import org.junit.Test;


public class TestLvq {
	public static double[][] SAMPLE_IN = {
		{1, 3},
		{3, 4},
		{6, 1},
		{8, 3},
		{9, 1},
		{1, 6}
	};
	
	public static double[][] SAMPLE_OUT = {
		{0}, {0}, {1}, {1}, {1}, {0}
	};
	
	public static double[][] INIT_WEIGHTS = {
		{3, 7}, {1, 4}
	};
	
	@Test
	public void testNetwork(){
		Lvq1 network = new Lvq1(2, 2);
		network.setWeights(new Matrix(INIT_WEIGHTS));
		
		Assert.assertNotNull(network.getWeights());
		
		for(int c= 0;c<network.getOutputCount();c++){
			for(int r=0;r<network.getInputCount();r++){
				Assert.assertEquals(INIT_WEIGHTS[r][c], network.getWeights().get(r, c), 0.001);
			}
		}
	}
	
//	@Test
	public void testTrain(){
		NeuralDataSet dataset = new BasicNeuralDataSet(SAMPLE_IN, SAMPLE_OUT);
		DataSetUtils.changeIdealToBiner(dataset);
		
		Lvq1 network = new Lvq1(2, 2);
		network.setWeights(new Matrix(INIT_WEIGHTS));
		utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		
		TrainLvq train = new TrainLvq(network, dataset, 0.5);
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		} //while (train.getError() > 0.01);
		while (iteration < 1);
		
		ConfusionMatrix cm = new ConfusionMatrix(2);
		
		Iterator<NeuralDataPair> it = dataset.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));			
	}
	
//	@Test
	public void testTrainIris(){
		double[][] init = {
				{5, 7 , 6.3},
				{3, 3.2, 3.3},
				{1.6, 4.7, 6},
				{0.2, 1.4, 2.5}
		};
		int in = 4;
		int out = 1;
		int nClass = 3;
		CSVFormat format = new CSVFormat('.',',');
		NeuralDataSet trainset 	= new CSVNeuralDataSet(
				utils.getDefaultPath() + "/resources/iris.train.data", 
				in, out, false, format);
		NeuralDataSet testset 	= new CSVNeuralDataSet(
				utils.getDefaultPath() + "/resources/iris.test.data", 
				in, out, false, format);
		
		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(testset);
		
		Lvq1 network = new Lvq1(in, nClass);
		network.setWeights(new Matrix(init));
		//network.reset();
		
		TrainLvq train = new TrainLvq(network, trainset, 0.05);
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		}while (iteration < 375);
		
		ConfusionMatrix cm = new ConfusionMatrix(nClass);
		
		Iterator<NeuralDataPair> it = testset.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));	
		
	}
	
//	@Test
	public void testTrainIrisSOM(){
		int in = 4;
		int out = 1;
		int nClass = 3;
		CSVFormat format = new CSVFormat('.',',');
		NeuralDataSet dataset 	= new CSVNeuralDataSet(Parameter.IRIS_DATA, 
				in, out, false, format);
		
		NeuralDataSet trainset = new BasicNeuralDataSet();
		NeuralDataSet testset = new BasicNeuralDataSet();
		
		DataSetUtils.splitTrainSet(dataset, trainset, testset, 0.5);
		
		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(testset);
		
		SOM network = new SOM(in, nClass);
		network.reset();
		
		BasicTrainSOM train = new BasicTrainSOM(network, 0.7, trainset, new NeighborhoodSingle());
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		}while (iteration < 100);
		
		ConfusionMatrix cm = new ConfusionMatrix(nClass);
		
		Iterator<NeuralDataPair> it = testset.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));	
		
	}
	
	
	@Test
	public void testTrainEcg(){
		double[][] init = {
				{-0.15684,0.12125,-0.24872,-0.24554,0.030899,0.13454,-0.028492,0.094032,0.23378,-0.187,1.3544,0.0014466},
				{-0.15902,0.0048954,-0.24995,-0.2386,0.11776,0.20236,-0.038392,0.12251,0.21094,-0.16618,1.3391,-0.11786},
				{-0.14078,-0.10207,-0.35285,-0.2631,0.24779,0.067629,-0.09355,0.10229,0.23178,-0.17808,1.3493,-0.079761},
				{-0.13128,-0.12873,-0.43254,-0.24385,0.26825,0.062986,-0.10353,0.096494,0.23547,-0.17209,1.3503,-0.094245},
				{-0.15868,-0.039684,-0.23149,-0.25665,0.15953,0.16958,-0.051668,0.12824,0.21708,-0.17363,1.3438,-0.11475},
				{-0.1562,0.098961,-0.26698,-0.2395,0.058247,0.15482,-0.033439,0.092542,0.22002,-0.17456,1.3432,-0.032974},
				{-0.14754,0.14074,-0.33222,-0.22278,0.026939,0.12657,-0.034963,0.071879,0.23063,-0.17621,1.3484,0.008851},
				{-0.16429,0.056371,-0.19475,-0.25277,0.078178,0.18394,-0.029357,0.11723,0.2154,-0.17611,1.3429,-0.072259},
				{-0.14673,-0.070881,-0.33571,-0.24524,0.20332,0.13653,-0.072129,0.1126,0.22021,-0.16844,1.3429,-0.11439},
				{-0.12896,-0.13766,-0.41538,-0.25706,0.28229,0.018256,-0.11303,0.090037,0.24473,-0.18005,1.3553,-0.063726},
				{-0.1528,-0.072216,-0.31654,-0.25007,0.20005,0.16727,-0.064543,0.12632,0.21443,-0.16898,1.3432,-0.1348},
				{-0.15509,0.03834,-0.2206,-0.24737,0.10063,0.12908,-0.045664,0.097968,0.22345,-0.17418,1.3403,-0.049174},
				{-0.15873,0.14778,-0.32388,-0.23269,0.0053708,0.1925,-0.021312,0.10629,0.22859,-0.18699,1.3606,-0.017834},
				{-0.15679,0.25649,-0.18708,-0.25006,-0.059358,0.3064,0.11058,0.089235,0.22453,-0.17442,1.3668,-0.050894},
				{-0.16217,0.34546,-0.2324,-0.25126,-0.082554,0.1677,0.12476,0.092673,0.20523,-0.17315,1.3684,0.018136},
				{-0.18071,0.36455,-0.39337,-0.2758,-0.10765,0.22049,0.040288,0.13201,0.21065,-0.16447,1.3563,-0.043127},
				{-0.21714,0.46721,-0.28148,-0.2851,-0.13095,0.29259,0.015235,0.13778,0.20257,-0.13558,1.3754,-0.014026},
				{-0.19349,0.46111,-0.15583,-0.24887,-0.1186,0.1335,-0.043657,0.10804,0.21154,-0.086549,1.3868,0.0015366},
				{-0.17983,0.52419,-0.19164,-0.26491,-0.13812,0.14261,-0.12228,0.1043,0.21062,-0.013012,1.4052,-0.0039286},
				{-0.21046,0.59714,-0.30704,-0.27311,-0.0935,0.01771,-0.089672,0.091156,0.20457,0.055862,1.3973,-0.034867},
				{-0.20615,0.49271,0.085034,-0.29091,-0.065317,-0.020697,-0.13246,0.099818,0.18182,0.12583,1.4013,-0.027434},
				{-0.22419,0.43236,-0.12471,-0.3048,-0.058475,-0.14019,-0.20444,0.085576,0.15385,0.081149,1.4314,0.00052202},
				{-0.21955,0.48151,-0.16419,-0.35667,-0.02589,-0.11546,-0.15762,0.10583,0.14569,0.038951,1.4462,-0.030775},
				{-0.23512,0.59673,-0.22799,-0.38152,0.001113,-0.17738,-0.14603,0.096033,0.13599,0.13813,1.4657,0.038105},
				{-0.2048,0.46806,-0.12982,-0.38185,0.047011,-0.25949,-0.21164,0.018069,0.14404,0.19458,1.468,0.041483},
				{-0.20065,0.40138,-0.057889,-0.36604,0.06674,-0.16196,-0.20992,0.019268,0.12429,0.23735,1.445,0.00042106},
				{-0.17342,0.44121,0.035026,-0.26206,0.083447,-0.19559,-0.17648,0.081489,0.10622,0.26664,1.4515,0.024881},
				{-0.19988,0.37774,0.1527,-0.2061,0.094518,-0.17834,-0.23823,0.087555,0.076601,0.21735,1.4629,-0.0021614},
				{-0.16858,0.12336,0.20784,-0.11495,0.1407,-0.088284,-0.21256,0.055967,0.10233,0.17837,1.4671,-0.017151},
				{-0.1386,-0.012138,0.24217,-0.050436,0.11394,-0.035425,-0.24424,0.052794,0.11939,0.081743,1.4743,0.024441},
				{-0.073285,-0.016488,-0.061396,-0.0096647,0.097349,-0.11238,-0.24949,0.060356,0.10968,-0.028799,1.4657,0.019692},
				{-0.0074188,-0.036159,0.32242,-0.010478,0.12687,-0.048578,-0.17009,0.049291,0.10261,-0.10398,1.473,-0.011235},
				{-0.012235,-0.083925,0.2656,-0.026902,0.11958,-0.11695,-0.13883,0.049068,0.10544,-0.15347,1.4768,0.04064},
				{-0.058923,-0.099579,-0.14878,-0.027802,0.12831,-0.10955,-0.11169,0.047743,0.074102,-0.20084,1.4859,0.0085638},
				{-0.0036373,0.030904,-0.016319,-0.026502,0.1468,0.02137,-0.022725,0.044774,0.037809,-0.19566,1.5004,-0.029156},
				{-0.092564,-0.0014927,-0.14565,-0.030393,0.15469,-0.10075,-0.068203,0.0070481,0.025044,-0.20213,1.4989,0.040405},
				{-0.21093,-0.011688,-0.39653,-0.037999,0.14361,-0.06144,-0.174,-0.016307,0.03017,-0.20424,1.49,0.042572},
				{-0.21116,-0.021982,-0.29615,-0.065536,0.14376,-0.078857,-0.1583,0.048317,-0.019526,-0.18974,1.4918,-0.0049512},
				{-0.2488,0.15511,-0.15434,-0.084967,0.19304,-0.12163,-0.19138,0.081775,-0.031353,-0.16906,1.5059,0.022251},
				{-0.2594,-0.04086,-0.012584,-0.089788,0.17563,-0.092169,-0.31411,0.06127,-0.041381,-0.159,1.5221,0.049534},
				{-0.2448,0.0080673,-0.097557,-0.10548,0.15833,-0.0072577,-0.29583,0.063716,-0.079418,-0.15896,1.5145,-0.081474},
				{-0.2408,-0.1862,-0.10309,-0.11101,0.21213,-0.078065,-0.26506,0.06114,-0.10663,-0.091894,1.4844,0.016785},
				{-0.2359,-0.13983,-0.26682,-0.26091,0.1181,-0.08566,-0.31248,0.091235,-0.16969,0.17158,1.5015,0.58271},
				{-0.36929,0.64519,-0.39105,-0.28706,0.44732,-0.1001,-0.13927,0.089931,-0.28318,0.59797,1.5318,0.10088},
				{-0.61263,1.146,0.47581,-0.81867,-0.13632,-0.012937,0.42945,0.18931,-0.3395,0.37825,1.5438,0.16287},
				{-0.17907,1.9542,1.1715,-3.0061,2.5215,-0.088792,0.88118,0.19682,-0.35002,0.37689,1.686,1.6165},
				{1.7612,2.4409,1.468,-4.5289,3.9506,0.66975,2.3327,2.5574,1.2012,0.60803,1.8345,3.3108},
				{1.9876,2.2261,2.4368,-4.7992,3.3318,1.0605,2.6083,2.3731,0.83923,0.53655,1.8597,3.7282},
				{0.13726,1.7403,0.081968,-3.7597,2.5042,-0.31941,1.2191,1.8493,0.13644,0.24388,1.7036,2.2707},
				{-0.42364,1.0256,-1.9246,-2.0034,1.5053,-0.5244,0.47927,1.4086,-0.012079,-0.033962,1.3427,0.38767},
				{-0.33676,-0.34469,-2.2509,-0.48217,0.13179,-0.48625,0.030009,1.946,-0.076125,-0.19993,1.1824,-0.42224},
				{-0.32299,-1.0624,-2.4392,0.074379,-1.6189,-0.53203,-0.26132,2.0936,-0.052247,-0.2336,1.1742,-0.41127},
				{-0.31997,-1.0782,-2.5648,0.36514,-3.1451,-0.32486,-0.28377,1.5826,-0.02407,-0.25776,1.2009,-0.36139},
				{-0.35693,-1.0383,-1.8952,0.53201,-3.8109,-0.58291,-0.30556,0.17234,-0.046574,-0.28924,1.1574,-0.27753},
				{-0.33174,-1,-0.85322,0.7033,-4.1925,-0.44038,-0.397,0.16795,-0.051585,-0.30368,1.0762,-0.33311},
				{-0.33237,-0.71797,-0.5069,0.82731,-4.8656,-0.31046,-0.35338,0.2866,-0.045395,-0.31637,1.121,-0.37871},
				{-0.34261,-0.69865,-0.33058,0.86428,-4.4364,-0.12398,-0.33687,0.22115,-0.010459,-0.30587,1.0638,-0.35242},
				{-0.34334,-0.64689,-0.45773,0.9475,-2.8158,-0.10049,-0.37379,0.12949,0.034453,-0.29866,1.0771,-0.35834},
				{-0.36553,-0.55324,-0.29082,1.1011,-1.7768,0.007436,-0.33667,0.039507,0.00084757,-0.29078,1.1362,-0.36108},
				{-0.34861,-0.72514,-0.38469,1.1116,-0.947,0.003013,-0.34725,-0.026555,0.014199,-0.26356,1.3077,-0.34233},
				{-0.33275,-0.85754,-0.44632,1.0752,-0.43009,-0.072908,-0.40688,-0.05846,0.044553,-0.26511,1.4916,-0.33962},
				{-0.33247,-0.81408,-0.46652,1.0959,0.15744,-0.028657,-0.34826,-0.14739,0.048431,-0.24138,1.6369,-0.3897},
				{-0.31825,-0.76231,-0.55167,1.1942,0.53508,-0.030744,-0.35972,-0.19193,0.090119,-0.20928,1.7026,-0.36139},
				{-0.32306,-0.73022,-0.6255,1.3153,1.0366,-0.046881,-0.39444,-0.16929,0.12639,-0.17032,1.7606,-0.39853},
				{-0.32511,-0.67938,-0.67855,1.4618,1.5001,0.020684,-0.38535,-0.18946,0.16031,-0.10568,1.8178,-0.45379},
				{-0.33725,-0.6759,-0.80771,1.686,1.602,0.020036,-0.42408,-0.2124,0.13308,-0.047516,1.8881,-0.41645},
				{-0.34748,-0.51263,-0.77291,1.9076,1.5441,0.067438,-0.48952,-0.23631,0.14777,0.036258,1.9289,-0.43437},
				{-0.36832,-0.52726,-0.91345,2.1208,1.5441,0.17388,-0.48564,-0.24475,0.16998,0.13846,1.9912,-0.48784},
				{-0.37547,-0.37606,-0.8185,2.3329,1.6074,0.21292,-0.50885,-0.30122,0.039936,0.20858,2.0476,-0.45502},
				{-0.402,-0.40378,-0.82007,2.4418,1.6794,0.25095,-0.58973,-0.35661,0.014944,0.25168,2.0957,-0.44546},
				{-0.42326,-0.27733,-0.73238,2.4598,1.8576,0.45469,-0.51858,-0.42254,-0.055458,0.25683,2.1339,-0.46539},
				{-0.43276,-0.10588,-0.6056,2.489,2.08,0.42998,-0.3984,-0.51459,-0.12252,0.2141,2.1917,-0.38359},
				{-0.44617,-0.049395,-0.53386,2.3394,2.3196,0.42577,-0.37024,-0.60887,-0.17883,0.13295,2.2294,-0.30253},
				{-0.43377,0.13881,-0.29905,2.0327,2.5278,0.50276,-0.22988,-0.75287,-0.30521,0.031627,2.2649,-0.26491},
				{-0.35285,0.20722,-0.21071,1.6009,2.6775,0.53742,-0.17441,-0.90371,-0.29807,-0.06966,2.2777,-0.10434},
				{-0.2463,0.25899,-0.18725,1.1389,2.7419,0.44079,-0.19564,-1.0327,-0.37047,-0.15411,2.3132,-0.055234},
				{-0.18215,0.25273,-0.048032,0.72793,2.7602,0.34559,-0.16539,-1.1457,-0.42842,-0.18695,2.3206,0.046199},
				{-0.14537,0.46356,-0.051958,0.46839,2.6277,0.30986,-0.17242,-1.2376,-0.43287,-0.20409,2.2856,0.16692},
				{-0.13424,0.29079,-0.047051,0.26377,2.4357,0.13179,-0.18395,-1.3219,-0.43507,-0.23374,2.2712,0.19321},
				{-0.13373,0.35858,-0.063801,0.12053,2.1602,0.094729,-0.17341,-1.3151,-0.42065,-0.22731,2.2823,0.17735},
				{-0.1275,0.29933,-0.081766,0.04696,1.8185,0.035294,-0.15721,-1.2363,-0.44204,-0.24252,2.2818,0.20302},
				{-0.12995,0.25686,-0.061846,0.021429,1.4485,-0.019181,-0.21979,-1.0905,-0.4546,-0.26606,2.275,0.13451},
				{-0.13437,0.1966,-0.11377,0.013852,1.1121,0.028468,-0.18964,-0.92944,-0.45364,-0.27018,2.2252,0.010813},
				{-0.11802,0.26328,-0.16177,0.016477,0.722,-0.04131,-0.18013,-0.75573,-0.42516,-0.27469,2.2112,-0.044796},
				{-0.09813,0.17446,-0.13458,0.019494,0.49669,-0.084753,-0.24302,-0.69602,-0.46627,-0.23931,2.1204,-0.081012},
				{-0.1055,0.25543,-0.15337,0.016742,0.52551,-0.089412,-0.21475,-0.69172,-0.43859,-0.2608,2.1683,-0.06761}
		};
		int in = 86;
		int out = 1;
		int nClass = 12;
		CSVFormat format = new CSVFormat('.','\t');
		NeuralDataSet trainset 	= new CSVNeuralDataSet(
				utils.getDefaultPath() + "/resources/ecgdata/wlet.train86.txt", 
				in, out, false, format);
		NeuralDataSet testset 	= new CSVNeuralDataSet(
				utils.getDefaultPath() + "/resources/ecgdata/wlet.test86.txt", 
				in, out, false, format);

		Lvq1 network = new Lvq1(in, nClass);
		//network.reset();
		network.setWeights(new Matrix(init));
		
		TrainLvq train = new TrainLvq(network, trainset, 0.05);
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		}while (iteration < 480);
		//40 * jml codebook vector  
		
		ConfusionMatrix cm = new ConfusionMatrix(nClass);
		
		Iterator<NeuralDataPair> it = testset.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));			
	}
	
	
}
