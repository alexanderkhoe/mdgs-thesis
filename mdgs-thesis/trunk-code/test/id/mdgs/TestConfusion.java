package id.mdgs;

import id.mdgs.dataset.Dataset;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.util.EngineArray;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.util.logging.DumpMatrix;
import org.junit.Test;


public class TestConfusion {
	@Test
	public void testBP(){
		utils.header("Testing ECG data using BackPropagation");
		int Pos = 1 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();

		NeuralDataSet enTrainset = DataSetUtils.myDataset2EncogDataset(trainset);
		NeuralDataSet enTestset = DataSetUtils.myDataset2EncogDataset(testset);
		
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,false,24));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,24));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,12));
		network.getStructure().finalizeStructure();
		network.reset();

		// train the neural network
		final Train train = new Backpropagation(network, enTrainset, 0, 0.8);
		train.addStrategy(new SmartLearningRate());
		int epoch = 1;

		do {
			train.iteration();
			System.out
					.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01);
		//while (epoch < 100);
		
		//CREATE EVALUATOR
		ConfusionMatrix cm = new ConfusionMatrix(12);
		
		utils.log("Test Result");
		Iterator<NeuralDataPair> it = enTestset.iterator();
		int tp = 0, total = 0;
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();

			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			int target = DataSetUtils.getTarget(ideal);

			cm.feed(win, target);
			
			utils.log(String.format("win=%d, target=%d, actual=%.4f, ideal=%.4f, %s", 
					win, target, output.getData()[win], ideal.getData()[target], 
					DumpMatrix.dumpArray(output.getData())));
		}		
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		utils.log(cm.toString());
	}
}
