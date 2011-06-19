package id.mdgs.backprop;

import java.util.Iterator;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

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
import org.junit.Test;


public class TestBackProp {
	@Test
	public void testBP(){
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();

		NeuralDataSet enTrainset = DataSetUtils.myDataset2EncogDataset(trainset);
		NeuralDataSet enTestset = DataSetUtils.myDataset2EncogDataset(testset);
		
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,false,trainset.numFeatures));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,24));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,nclass));
		network.getStructure().finalizeStructure();
		network.reset();

		// train the neural network
		final Train train = new Backpropagation(network, enTrainset, 0.075, 0.8);
		train.addStrategy(new SmartLearningRate());
		int epoch = 1;

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
//		} while(train.getError() > 0.01);
		} while (epoch < 500);
		
		utils.log("Test Result");
		Iterator<NeuralDataPair> it = enTestset.iterator();
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		
		while(it.hasNext()){
			NeuralDataPair pair = it.next(); 
			NeuralData input = pair.getInput();
			NeuralData ideal = pair.getIdeal();

			NeuralData output = network.compute(input);
			int win = EngineArray.maxIndex(output.getData());
			int target = DataSetUtils.getTarget(ideal);
			cm.feed(win, target);
			
		}
		
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
}
