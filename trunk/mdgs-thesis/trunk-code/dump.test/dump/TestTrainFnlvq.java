package dump;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.fnlvq.crisp.FnlvqCrisp;
import id.mdgs.neural.fnlvq.crisp.TrainFnlvqCrisp;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.util.csv.CSVFormat;
import org.junit.Test;


public class TestTrainFnlvq {
	
	@Test
	public void testIris(){
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
		
		//Map<Integer,List<Integer>> stat = DataSetUtils.getClassTable(initset);
//		int in = 4;
//		int out= stat.size();
		FnlvqCrisp network = new FnlvqCrisp(in, nClass);
		network.getWeights().randomize(0d, 2d);
//		network.setWeights(new WeightMatrix(in, out, 
//				DataSetUtils.calculateInitWeights(initset)));
		
		TrainFnlvqCrisp train = new TrainFnlvqCrisp(network, trainset, 0.01);
//		train.addStrategy(new SmartLearningRate());
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
//		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		} //while (train.getError() > 0.01);
		while (iteration < 1000);
//		Loging.stop();
		
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
		
		utils.log("Vector Reference");
		utils.log(network.getWeights().toString());
		
		utils.log(cm.toString());
		utils.log("Test Result : Data IRIS");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
	
//	@Test
	public void testEcg(){
		utils.header("Test TrainFnlvq() with EcgF300C15N100"); 
		NeuralDataSet dataset = new CSVNeuralDataSet(Parameter.ECG300C15N100_TRAIN, 300, 1, 
				false, CSVFormat.ENGLISH);
		NeuralDataSet testset = new CSVNeuralDataSet(Parameter.ECG300C15N100_TEST, 300, 1, 
				false, CSVFormat.ENGLISH);		
		NeuralDataSet trainset = new BasicNeuralDataSet(); 
		NeuralDataSet initset  = new BasicNeuralDataSet();
		DataSetUtils.splitTrainSet(dataset, trainset, initset, 5);
		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(initset);
		
		int in = trainset.getInputSize();
		int out= trainset.getIdealSize();
		FnlvqCrisp network = new FnlvqCrisp(in, out);
		network.setWeights(new WeightMatrix(in, out, 
				DataSetUtils.calculateInitWeights(initset)));
		
		TrainFnlvqCrisp train = new TrainFnlvqCrisp(network, trainset, 0.01);
		train.addStrategy(new SmartLearningRate());
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
//		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		} //while (train.getError() > 0.01);
		while (iteration < 500);
//		Loging.stop();
		
		utils.log("Vector Reference");
		utils.log(network.getWeights().toString());
		
		ConfusionMatrix cm = new ConfusionMatrix(out);
		Iterator<NeuralDataPair> it = testset.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();

			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
		}
		utils.log(cm.toString());
		utils.log("Test Result : Data ECGF300C15N100");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}	
}
