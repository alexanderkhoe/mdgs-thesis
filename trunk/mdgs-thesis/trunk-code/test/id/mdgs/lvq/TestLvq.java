package id.mdgs.lvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestLvq {
	
	@Test
	public void testLvq2(){
		int Pos = 1 * 3;
		int nclass = 12;
		Dataset dataset = new Dataset(Parameter.DATA_ALL[Pos + 0]);
		dataset.load();
		
		KFoldedDataset<Dataset, Entry> kfold = new KFoldedDataset<Dataset, Dataset.Entry>(dataset, 2, 0.5, false);
		FoldedDataset<Dataset, Entry> trainset = kfold.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset = kfold.getKFoldedForTest(0);
		
		Lvq net = new Lvq();
		net.initCodes(trainset, 1, 5);
		
		TrainLvq21 train = new TrainLvq21(net, trainset, 0.1, 0.005);
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
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
	
//	@Test
	public void testLvq(){
		int Pos = 2 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		FoldedDataset<Dataset, Entry> fdset = new FoldedDataset<Dataset, Dataset.Entry>(trainset, true);
		Lvq net = new Lvq();
		net.initStaticCodes(fdset, 1, 5);
//		net.initCodes(trainset);
//		net.initCodes(trainset, 0d, 1d);
		System.out.println(net.codebook.toString());
		System.exit(0);
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
