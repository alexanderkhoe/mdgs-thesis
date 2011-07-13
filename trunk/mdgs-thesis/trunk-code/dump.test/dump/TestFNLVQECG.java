package dump;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQ;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQTrain;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.utils;

import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.util.csv.CSVFormat;
import org.encog.util.logging.DumpMatrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFNLVQECG {

//	@Test
	public void testNumData(){
		DataSetUtils.createData24();
		DataSetUtils.createData86();
		
		utils.header("Data Statistik");
		utils.log("ecg24Train");
		Map<Integer,List<Integer>> stat;
		stat = DataSetUtils.getClassTable(DataSetUtils.ecg24Train);
		for(Integer cat: stat.keySet()){
			utils.log(String.format("Class #%-2d : %d", cat, stat.get(cat).size()));
		}
		utils.log("ecg24Test");
		stat = DataSetUtils.getClassTable(DataSetUtils.ecg24Test);
		for(Integer cat: stat.keySet()){
			utils.log(String.format("Class #%-2d : %d", cat, stat.get(cat).size()));
		}
		utils.log("ecg86Train");
		stat = DataSetUtils.getClassTable(DataSetUtils.ecg86Train);
		for(Integer cat: stat.keySet()){
			utils.log(String.format("Class #%-2d : %d", cat, stat.get(cat).size()));
		}
		utils.log("ecg86Test");
		stat = DataSetUtils.getClassTable(DataSetUtils.ecg86Test);
		for(Integer cat: stat.keySet()){
			utils.log(String.format("Class #%-2d : %d", cat, stat.get(cat).size()));
		}
		
		DataSetUtils.createDataECG300C15N100();
		utils.log("ecgECG300C15N100Train");
		stat = DataSetUtils.getClassTable(DataSetUtils.ecgECG300C15N100Train);
		for(Integer cat: stat.keySet()){
			utils.log(String.format("Class #%-2d : %d", cat, stat.get(cat).size()));
		}
		
	}
	
//	@Test
	public void testCekData(){
		DataSetUtils.createData24();
		
		utils.header("Check weather read data succeed.");
		Iterator<NeuralDataPair> it = DataSetUtils.init24Train.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			utils.log(String.format("Fitures: %d", input.getData().length));
			utils.log(String.format("Data   : %s", DumpMatrix.dumpArray(input.getData())));
			utils.log(String.format("Target : %d", DataSetUtils.getTarget(ideal)));
			break;
		}
	}
	
//	@Test
	public void test24N100() throws IOException{
		utils.header("Feature 24 N 100");
		
		DataSetUtils.createData24N100();
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecg24N100Part1);
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecg24N100Part2);
		
		NeuralDataSet trainset = DataSetUtils.ecg24N100Part1;
		NeuralDataSet initset = DataSetUtils.ecg24N100Part2;

		Map<Integer,List<Integer>> stat = DataSetUtils.getClassTable(initset);
		int in = 24;
		int out= stat.size();
		FNLVQ network = new FNLVQ(in, out);
		network.setWeights(new WeightMatrix(in, out, 
				DataSetUtils.calculateInitWeights(initset)));
		
		FNLVQTrain train = new FNLVQTrain(network, trainset, 0.01);
		//train.addStrategy(new SmartLearningRate());
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		for(iteration = 0;iteration<=500;iteration++)
		{
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
		}
		
		Loging.stop();
		
		utils.log("Vector Reference");
		utils.log(network.getWeights().toString());
		
		utils.log("Testing...");
		DataSetUtils.createData24();
		stat = DataSetUtils.getClassTable(DataSetUtils.ecg24Test);
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecg24Test);
		ConfusionMatrix cm = new ConfusionMatrix(out);
		
		int counter = 0;
		NeuralDataPair pair = BasicNeuralDataPair.createPair(in, out);
		for(Integer cat: stat.keySet()){
			counter = 0;
			List<Integer> list = stat.get(cat);
			for(Integer pos: list){
				DataSetUtils.ecg24Test.getRecord(pos, pair);
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
		
		FNLVQTrain train = new FNLVQTrain(network, trainset, 0.01);
		train.addStrategy(new SmartLearningRate());
		
		utils.log("Training...");
		utils.log(String.format("start learning rate: %.4f", train.getLearningRate()));
		
		Loging.start(Loging.createFileWriter(Loging.LOG_FILE));
		
		int iteration = 0;
		for(iteration = 0;iteration<=500;iteration++)
		{
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
		}
		
		Loging.stop();
		
		utils.log("Vector Reference");
		utils.log(network.getWeights().toString());
		
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
