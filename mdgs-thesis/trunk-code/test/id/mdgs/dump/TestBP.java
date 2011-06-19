package id.mdgs.dump;


import java.util.Iterator;
import java.util.List;

import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.utils;

import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.engine.util.EngineArray;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.strategy.RequiredImprovementStrategy;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.neural.prune.NetworkPattern;
import org.encog.util.logging.DumpMatrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBP {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testBP(){
		utils.header("Testing ECG data using BackPropagation");
		DataSetUtils.createData24N100(0.5);
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecg24N100Part1);
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecg24N100Part2);
		
		NeuralDataSet trainset = DataSetUtils.ecg24N100Part1;
		NeuralDataSet testset = DataSetUtils.ecg24N100Part2;
		
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,false,24));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,24));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,12));
		network.getStructure().finalizeStructure();
		network.reset();

		// train the neural network
		final Train train = new Backpropagation(network, trainset, 0, 0.8);
		train.addStrategy(new SmartLearningRate());
		int epoch = 1;

		do {
			train.iteration();
			System.out
					.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01);
		//while (epoch < 100);
		
		utils.log("Test Result");
		Iterator<NeuralDataPair> it = testset.iterator();
		int tp = 0, total = 0;
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();

			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			int target = DataSetUtils.getTarget(ideal);
			if (win == target) tp++;
			total++;
			
			utils.log(String.format("win=%d, target=%d, actual=%.4f, ideal=%.4f, %s", 
					win, target, output.getData()[win], ideal.getData()[target], 
					DumpMatrix.dumpArray(output.getData())));
		}		
		utils.log(String.format("True : %d", tp));
		utils.log(String.format("Total: %d", total));
		utils.log(String.format("Accuracy = %.4f",100.0*tp/total));
		
	}
	
	
//	@Test
	public void testBPIris(){
		utils.header("Testing IRIS data using BackPropagation");
//		NeuralDataSet trainset = DataSetUtils.createDataIris();
		DataSetUtils.changeIdealToBiner(trainset);
		
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,false,4));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,16));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,3));
		network.getStructure().finalizeStructure();
		network.reset();

		// train the neural network
		final Train train = new Backpropagation(network, trainset, 0.7, 0.7);
		//train.addStrategy(new RequiredImprovementStrategy(100));
		train.addStrategy(new SmartLearningRate());
		int epoch = 1;

		do {
			train.iteration();
			System.out
					.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01);
		//while (epoch < 100);
		
		utils.log("Test Result");
		Iterator<NeuralDataPair> it = trainset.iterator();
		int tp = 0, total = 0;
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();

			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			int target = DataSetUtils.getTarget(ideal);
			if (win == target) tp++;
			total++;
			
			utils.log(String.format("win=%d, target=%d, actual=%.4f, ideal=%.4f, %s", 
					win, target, output.getData()[win], ideal.getData()[target], 
					DumpMatrix.dumpArray(output.getData())));
		}		
		utils.log(String.format("True : %d", tp));
		utils.log(String.format("Total: %d", total));
		utils.log(String.format("Accuracy = %.4f",100.0*tp/total));
	}
}
