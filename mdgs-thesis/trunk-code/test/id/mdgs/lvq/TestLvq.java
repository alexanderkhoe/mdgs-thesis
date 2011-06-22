package id.mdgs.lvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestLvq {
	
	@Test
	public void testLvq(){
		int Pos = 0 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
//		net.initCodes(trainset, 1, 5);
		net.initCodes(trainset);
//		net.initCodes(trainset, 0d, 1d);
		
		
//		ITrain train = new TrainLvq1(net, trainset, 0.05);
		ITrain train = new TrainLvq21(net, trainset, 0.05, 0.005);
//		ITrain train = new TrainLvq3(net, trainset, 0.05, 0.005, 0.5);
		
		train.setMaxEpoch(150);
		do {
			train.iteration();
			System.out.println("Iteration: " + train.getCurrEpoch() + ", Error:" + train.getError());
		} while (!train.shouldStop());
		
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
