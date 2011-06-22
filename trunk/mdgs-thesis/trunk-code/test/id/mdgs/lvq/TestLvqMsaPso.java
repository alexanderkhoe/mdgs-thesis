package id.mdgs.lvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.msa.TrainLvqMsaPso;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;

public class TestLvqMsaPso {
	
	@Test
	public void testLvqMsaPso(){
		LvqUtils.resetLabels();
		int Pos = 0 * 4;
		int numclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
		net.initCodes(trainset);
		TrainLvqMsaPso train = new TrainLvqMsaPso(net, trainset, 10, 0.1, 0.005);
		
		int iteration = 0;
		do {
			train.iteration();
			iteration++;
			//System.out.println("Iteration: " + iteration );
		} while (iteration < 10);
		
		
//		TrainLvq21Msa train2 = new TrainLvq21Msa(net, trainset, 0.1, 0.005);
//		iteration = 0;
//		do {
//			train2.iterationOption3();
//			iteration++;
//		} while (iteration < 3);
//		
//		System.out.print(train2.msa.toString());	
		
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			
		}
		//utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));			
	}
	
}
