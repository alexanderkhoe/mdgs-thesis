package id.mdgs.lvq;

import java.util.Iterator;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.msa.TrainLvq21Msa;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestLvqMsa {

	@Test
	public void testLvqMsa(){
		LvqUtils.resetLabels();
		int Pos = 0 * 4;
		int numclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
		//net.initCodes(trainset, 1, 5);
		net.initCodes(trainset);
		
//		TrainLvq1 train = new TrainLvq1(net, trainset, 0.1);
		TrainLvq21Msa train = new TrainLvq21Msa(net, trainset, 0.1, 0.005);
//		TrainLvq21Msa train = new TrainLvq21Msa(net, trainset, 0.1, 0.1);
		int iteration = 0;
		train.resetCounter();
		do {
//			train.iterationOption1();
			train.iterationOption3();
			iteration++;
		} while (iteration < 100);
		System.out.print(train.msa.toString());	
		
		utils.log(String.format("alpha: %f",train.alpha));
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
