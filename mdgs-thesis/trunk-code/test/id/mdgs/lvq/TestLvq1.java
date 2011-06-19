package id.mdgs.lvq;

import java.util.Iterator;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestLvq1 {
	
	@Test
	public void testLvq1(){
		LvqUtils.resetLabels();
		int Pos = 0 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
//		net.initCodes(trainset, 1, 5);
		net.initCodes(trainset);
//		net.initCodesRandom(trainset);
		
		
//		TrainLvq1 train = new TrainLvq1(net, trainset, 0.05);
		TrainLvq1 train = new TrainLvq21(net, trainset, 0.075, 0.005);
//		TrainLvq1 train = new TrainLvq3(net, trainset, 0.075, 0.005, 0.01);
		
		train.setMaxEpoch(150);
		int iteration = 0;
//		train.resetCounter();
		do {
//			train.iterationOption1();
			train.iterationOption3();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		} while (!train.shouldStop());
		
		utils.log(String.format("alpha: %f",train.alpha));
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		
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
