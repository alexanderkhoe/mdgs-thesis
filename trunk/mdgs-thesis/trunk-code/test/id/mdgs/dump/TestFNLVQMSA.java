package id.mdgs.dump;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQ;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.utils;

import org.encog.engine.util.EngineArray;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.util.logging.DumpMatrix;
import org.junit.Test;


public class TestFNLVQMSA {

//	@Test
	public void testIris() throws IOException{
		NeuralDataSet dataset = DataSetUtils.createDataIris();
		NeuralDataSet trainset = new BasicNeuralDataSet(); 
		NeuralDataSet initset  = new BasicNeuralDataSet();
		DataSetUtils.splitTrainSet(dataset, trainset, initset, DataSetUtils.DATA_GROUP_SIZE);
		DataSetUtils.changeIdealToBiner(trainset);
		DataSetUtils.changeIdealToBiner(initset);
		
		Map<Integer,List<Integer>> stat = DataSetUtils.getClassTable(initset);
		int in = 4;
		int out= stat.size();
		FNLVQ network = new FNLVQ(in, out);
		network.setWeights(new WeightMatrix(in, out, 
				DataSetUtils.calculateInitWeights(initset)));
		
		TrainFnlvqMsa train = new TrainFnlvqMsa(network, trainset, 0.01);
		train.addStrategy(new SmartLearningRate());
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			//Loging.log(train.getMSA().toString());
			//if(iteration > 500) break;
			iteration++;
		} //while (train.getError() > 0.01);
		while (iteration < 10000);
		
		Loging.stop();
		
//	
//		
		utils.log("Vector Reference");
		utils.log(network.getWeights().toString());
		
		ConfusionMatrix cm = new ConfusionMatrix(out);
		
		utils.log("Test Result");
		Iterator<NeuralDataPair> it = trainset.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();

			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			
			cm.feed(win, target);
			
//			NeuralData output = ((FNLVQ)train.getNetwork()).getOutput();
//			utils.log(String.format("win=%d, target=%d, actual=%.4f, ideal=%.4f, %s", 
//					win, target, output.getData()[win], ideal.getData()[target], 
//					DumpMatrix.dumpArray(output.getData())));
		}

		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		utils.log(cm.toString());
	}
	
	@Test
	public void testF300C15N100() throws IOException{
		utils.header("Feature 300 Class 15 N 100");
		
		DataSetUtils.createDataECG300C15N100();
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecgECG300C15N100Part1);
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecgECG300C15N100Part2);
		
		NeuralDataSet trainset = DataSetUtils.ecgECG300C15N100Part1;
		NeuralDataSet initset = DataSetUtils.ecgECG300C15N100Part2;

		Map<Integer,List<Integer>> stat = DataSetUtils.getClassTable(initset);
		int in = 300;
		int out= stat.size();
		FNLVQ network = new FNLVQ(in, out);
		network.setWeights(new WeightMatrix(in, out, 
				DataSetUtils.calculateInitWeights(initset)));
		
		TrainFnlvqMsa train = new TrainFnlvqMsa(network, trainset, 0.01);
		train.addStrategy(new SmartLearningRate());
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		for(iteration = 0;iteration<=100;iteration++)
		{
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
		}
		
		Loging.stop();
		
		utils.log("Vector Reference");
		utils.log(network.getWeights().toString());
		utils.log(train.getMSA().toString());
		
		utils.log("Testing...");
		stat = DataSetUtils.getClassTable(DataSetUtils.ecgECG300C15N100Test);
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecgECG300C15N100Test);
		ConfusionMatrix cm = new ConfusionMatrix(out);
		
		int counter = 0;
		NeuralDataPair pair = BasicNeuralDataPair.createPair(in, out);
		for(Integer cat: stat.keySet()){
			counter = 0;
			List<Integer> list = stat.get(cat);
			for(Integer pos: list){
				DataSetUtils.ecgECG300C15N100Test.getRecord(pos, pair);
				NeuralData input = pair.getInput();
				NeuralData ideal = pair.getIdeal();
				
				int win = network.classify(input);
				int target = DataSetUtils.getTarget(ideal);
				
				cm.feed(win, target);
				
				if(++counter > 100) break;
			}
		}

		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		utils.log(cm.toString());
	}
	
}
