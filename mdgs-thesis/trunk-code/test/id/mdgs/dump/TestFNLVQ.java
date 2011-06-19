package id.mdgs.dump;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQ;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQTrain;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.utils;

import org.encog.engine.data.EngineData;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodSingle;
import org.encog.util.csv.CSVFormat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFNLVQ {

	public static String IRIS_DATA = utils.getDefaultPath() + "/resources/iris.data";
	public static String LOG_FILE = utils.getDefaultPath() + "/resources/trash/log.txt";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

//	@Test
	public void testFNLVQ() throws IOException{
		//load data
		NeuralDataSet dataset = new CSVNeuralDataSet(IRIS_DATA, 4, 1, 
				false, CSVFormat.ENGLISH);		
		NeuralDataSet trainset = new BasicNeuralDataSet();
		NeuralDataSet initset = new BasicNeuralDataSet();
		DataSetUtils.splitTrainSet(dataset, trainset, initset, 0.1);
		NeuralDataSet trainset2 = new BasicNeuralDataSet();
		NeuralDataSet testset2 = new BasicNeuralDataSet();
		DataSetUtils.splitTrainSet(trainset, trainset2, testset2, 0.4);
		
		//create network
		FNLVQ network = new FNLVQ(4, 3);
		network.setWeights(new WeightMatrix(4, 3, 
				DataSetUtils.calculateInitWeights(4, 3, initset)));
		
		FNLVQTrain train = 
			new FNLVQTrain(network, trainset2, 0.05);
				
		int iteration = 0;
		
		Loging.start(Loging.createFileWriter(LOG_FILE));
		
		for(iteration = 0;iteration<=500;iteration++)
		{
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
		}
		
		System.out.print(network.getWeights().toString());
		
		utils.log("Testing...");
		Iterator<NeuralDataPair> it = testset2.iterator();
		int tp = 0, total = 0;
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();
			
			int win = network.classify(input);
			int target = DataSetUtils.getTarget(ideal);
			if (win == target) tp++;
			utils.log(String.format("Pattern #%3d winner: %d, ideal: %d", 
						total, win, target));
			total++;
		}
		utils.log(String.format("True : %d", tp));
		utils.log(String.format("Total: %d", total));
		utils.log(String.format("Accuracy: %.4f",100.0*tp/total));
		
		Loging.stop();
	}
	
	public void showUnique(List<List<Integer>> lists){
		for(List<Integer> list: lists)
			System.out.println(list);
	}
	
}
