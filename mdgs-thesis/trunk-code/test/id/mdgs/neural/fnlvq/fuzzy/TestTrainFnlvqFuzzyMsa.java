package id.mdgs.neural.fnlvq.fuzzy;

import java.io.IOException;
import java.util.Iterator;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.NodeNeuralData;
import id.mdgs.neural.data.NodeNeuralDataPair;
import id.mdgs.neural.data.NodeNeuralDataSet;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;
import junit.framework.Assert;

import org.encog.engine.util.EngineArray;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.util.csv.CSVFormat;
import org.junit.Test;


public class TestTrainFnlvqFuzzyMsa {

	public static double[][] SAMPLE_IN = {
		{5.10,3.50,1.40,0.20},
		{7.00,3.20,4.70,1.40},
		{6.40,3.20,4.50,1.50},
		{6.90,3.10,4.90,1.50},
		{6.30,3.30,6.00,2.50},
		{5.80,2.70,5.10,1.90},
		{7.10,3.00,5.90,2.10},
		{6.30,2.90,5.60,1.80},
		{6.50,3.00,5.80,2.20},
		{7.60,3.00,6.60,2.10},
		{4.90,2.50,4.50,1.70},
		{7.30,2.90,6.30,1.80},
		{6.70,2.50,5.80,1.80},
		{7.20,3.60,6.10,2.50},
		{6.50,3.20,5.10,2.00}
	};
	public static double[][] SAMPLE_OUT = {
		{0},{1},{1},{1},{2},{2},{2},{2},{2},{2},{2},{2},{2},{2},{2} };
	
	public static double[][] RESULT = {
		{4.100,5.100,6.100},	//=0
		{2.500,3.500,4.500},
		{0.400,1.400,2.400},
		{-0.800,0.200,1.200},
		{6.300,6.767,7.100},	//=1
		{3.000,3.167,3.300},
		{4.400,4.700,5.000},
		{1.300,1.467,1.600},	
		{5.800,6.400,7.100},	//=2
		{2.700,2.980,3.300},
		{5.100,5.680,6.000},	
		{1.800,2.100,2.500},
		{4.900,6.700,7.600},	//=2
		{2.500,2.950,3.600},
		{4.500,5.733,6.600},
		{1.700,1.983,2.500}
	};
	
	public static int[][] RESULT_OUT = {
		{0}, {1}, {2}, {2} };
	
//	@Test
	public void testFnlvqFuzzy(){
		FnlvqFuzzy network = new FnlvqFuzzy(4, 3);
		
		Assert.assertNotNull(network.getWeights());
		Assert.assertNotNull(network.getMembershipDegrees());
		Assert.assertNotNull(network.getOutput());
		double a = (double)5/2;
		Assert.assertEquals(2.5,a, 0.1);
	}
	
//	@Test
	public void testTrainFnlvqFuzzyMsa(){
		NeuralDataSet dataset = new BasicNeuralDataSet(SAMPLE_IN, SAMPLE_OUT);
		
		FnlvqFuzzy network = new FnlvqFuzzy(4, 3);
		TrainFnlvqFuzzy1Msa train = new TrainFnlvqFuzzy1Msa(network, dataset, 0.01, 5);
		
		Assert.assertEquals(4, train.getFuzzyTraining().getRecordCount());
		
		int counter = 0;
		for(NodeNeuralDataPair pair: train.getFuzzyTraining()){
			for(int i=0;i<pair.getInputArray().length;i++){
				FuzzyNode fn = (FuzzyNode) pair.getInputArray()[i];
				double[] res = RESULT[counter*pair.getInputArray().length+i];
				
				Assert.assertEquals(res[0], fn.min, 0.001);
				Assert.assertEquals(res[1], fn.mean, 0.001);
				Assert.assertEquals(res[2], fn.max, 0.001);
			}
			int id = DataSetUtils.getTarget(pair.getIdeal());
			Assert.assertEquals(RESULT_OUT[counter][0], id);
			counter++;
		}
		
		//init weights

		for(int c=0;c<network.getWeights().getCols();c++){
			for(int r=0;r<network.getWeights().getRows();r++){
				FuzzyNode fn = (FuzzyNode) network.getWeights().get(r, c);
				int s;
				if(c == 0) 		s=0;
				else if (c == 1)s=4;
				else s=8;
				double[] res = RESULT[s+r];
				
				Assert.assertEquals(res[0], fn.min, 0.001);
				Assert.assertEquals(res[1], fn.mean, 0.001);
				Assert.assertEquals(res[2], fn.max, 0.001);
			}
		}
	}
	
	@Test
	public void testRealData() throws IOException{
		int in = 300;
		int out = 1;
		int nClass = 15;
		CSVFormat format = new CSVFormat('.',',');
		NeuralDataSet trainset 	= new CSVNeuralDataSet(Parameter.ECG300C15N100_TRAIN, 
				in, out, false, format);
		NeuralDataSet testset 	= new CSVNeuralDataSet(Parameter.ECG300C15N100_TEST, 
				in, out, false, format);
		
		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(testset);
		
		NodeNeuralDataSet fuzzytestset = TrainFnlvqFuzzy1Msa.generateFuzzyData(testset, 5);

		FnlvqFuzzy network = new FnlvqFuzzy(in, nClass);
		TrainFnlvqFuzzy1Msa train = new TrainFnlvqFuzzy1Msa(network, trainset, 0.01, 5);
//		train.setFuzzinessFactorByConstant(0.01, 0.00025);
		//train.addStrategy(new SmartLearningRate());
		//train.setFuzzinessFactorByVariable();
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
			Loging.log(train.getMsa().toString());
		} //while (train.getError() > 0.01);
		while (iteration < 100);
		Loging.log("Vector Reference");
		Loging.log(network.getWeights().toString());
		Loging.stop();
		

		
		ConfusionMatrix cm = new ConfusionMatrix(15);
		
		Iterator<NodeNeuralDataPair> it = fuzzytestset.iterator();
		while(it.hasNext()){
			NodeNeuralDataPair pair = it.next(); 
			NodeNeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			if(output.getData()[win] < 0.5){
				win = -1;
			}
			
			int target = DataSetUtils.getTarget(ideal);
			
//			int win = network.classify(input);
//			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(cm.toString());
		utils.log("Test Result : Data F300");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));		
	}
	
//	@Test
	public void testRealData2() throws IOException{
		int in = 86;
		int out = 1;
		int nClass = 12;
		CSVFormat format = new CSVFormat('.','\t');
		NeuralDataSet dataset 	= new CSVNeuralDataSet(Parameter.ECG86_TRAIN, 
				in, out, false, format);

		NeuralDataSet trainset = new BasicNeuralDataSet();
		NeuralDataSet testset = new BasicNeuralDataSet();
		
		DataSetUtils.splitTrainSet(dataset, trainset, testset, 0.5);
		
		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(testset);
		
		NodeNeuralDataSet fuzzytestset = TrainFnlvqFuzzy2Msa.generateFuzzyData(testset, 5);

		FnlvqFuzzy network = new FnlvqFuzzy(in, nClass);
		TrainFnlvqFuzzy1Msa train = new TrainFnlvqFuzzy2Msa(network, trainset, 0.01, 5);
//		train.setFuzzinessFactorByConstant(0.01, 0.00025);
		//train.addStrategy(new SmartLearningRate());
//		train.setFuzzinessFactorByVariable();
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
			Loging.log(train.getMsa().toString());
		} //while (train.getError() > 0.01);
		while (iteration < 50);
		Loging.log("Vector Reference");
		Loging.log(network.getWeights().toString());
		Loging.stop();
		

		
		ConfusionMatrix cm = new ConfusionMatrix(15);
		
		Iterator<NodeNeuralDataPair> it = fuzzytestset.iterator();
		while(it.hasNext()){
			NodeNeuralDataPair pair = it.next(); 
			NodeNeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			if(output.getData()[win] < 0.5){
				win = -1;
			}
			
			int target = DataSetUtils.getTarget(ideal);
			
//			int win = network.classify(input);
//			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(cm.toString());
		utils.log("Test Result : Data F300");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));		
	}	

//	@Test
	public void testRealData3() throws IOException{
		int in = 86;
		int out = 1;
		int nClass = 12;
		CSVFormat format = new CSVFormat('.','\t');
		NeuralDataSet trainset 	= new CSVNeuralDataSet(Parameter.ECG86_2_TRAIN, 
				in, out, false, format);
		NeuralDataSet testset 	= new CSVNeuralDataSet(Parameter.ECG86_2_TEST, 
				in, out, false, format);

		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(testset);
		
		NodeNeuralDataSet fuzzytestset = TrainFnlvqFuzzy2Msa.generateFuzzyData(testset, 5);

		FnlvqFuzzy network = new FnlvqFuzzy(in, nClass);
		TrainFnlvqFuzzy1Msa train = new TrainFnlvqFuzzy2Msa(network, trainset, 0.01, 5);
//		train.setFuzzinessFactorByConstant(0.01, 0.00025);
		//train.addStrategy(new SmartLearningRate());
//		train.setFuzzinessFactorByVariable();
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
			Loging.log(train.getMsa().toString());
		} //while (train.getError() > 0.01);
		while (iteration < 50);
		Loging.log("Vector Reference");
		Loging.log(network.getWeights().toString());
		Loging.stop();
		

		
		ConfusionMatrix cm = new ConfusionMatrix(15);
		
		Iterator<NodeNeuralDataPair> it = fuzzytestset.iterator();
		while(it.hasNext()){
			NodeNeuralDataPair pair = it.next(); 
			NodeNeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			if(output.getData()[win] < 0.5){
				win = -1;
			}
			
			int target = DataSetUtils.getTarget(ideal);
			
//			int win = network.classify(input);
//			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(cm.toString());
		utils.log("Test Result : Data F300");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));		
	}	
	
}
