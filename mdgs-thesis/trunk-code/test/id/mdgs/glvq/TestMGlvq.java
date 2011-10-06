package id.mdgs.glvq;

import java.util.Iterator;

import javax.swing.JFrame;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.glvq.mglvq.MGlvq;
import id.mdgs.glvq.mglvq.TrainMGlvq;
import id.mdgs.glvq.mglvq.WinnerByMahalanobis;
import id.mdgs.gui.CodebookMonitor;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.jfree.ui.RefineryUtilities;
import org.junit.Test;


public class TestMGlvq {
	@Test
	public void testMGlvq(){
//		int Pos = 5 * 4;
//		int nclass = 12;
//		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
//		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
//		
//		trainset.load();
//		testset.load();

		int Pos = 5 * 3;
		int nclass = 12;
		Dataset dset = new Dataset(Parameter.DATA_ALL[Pos + 0]);
		
		dset.load();
		KFoldedDataset<Dataset, Entry> kfdsets 	= new KFoldedDataset<Dataset, Dataset.Entry>(dset, 10, 0.5, false);
		FoldedDataset<Dataset, Entry> trainset 	= kfdsets.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset  	= kfdsets.getKFoldedForTest(0);
		
		MGlvq net = new MGlvq(WinnerByMahalanobis.normalizeData(trainset));
		net.initCodes(trainset, 1);
		
		ITrain train = new TrainMGlvq(net, trainset, 0.001);
		train.setMaxEpoch(50);
		
		/*monitor*/
		CodebookMonitor cbm = new CodebookMonitor(net.getClass().getSimpleName() + " Codebook Monitor", net.codebook, train);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		do{
			train.iteration();
			cbm.update(train.getCurrEpoch(), train.getError());
			System.out.println(train.getError());
		}while(!train.shouldStop());

		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		StringBuilder Failed = new StringBuilder();
		Iterator<Entry> it = trainset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			if(win != target)
				Failed.append(sample.toString() + "\n");
		}
		
		utils.log(cm.toString());		
	}
	
	
//	@Test
	public void testNormalizasi(){
		double[][] d1 = {
				{2,2},
				{2,5},
				{6,5},
				{7,3},
				{4,7},
				{6,4},
				{5,3},
				{4,6},
				{2,5},
				{1,3}
				};
		
		Dataset set = new Dataset();
		set.load(d1);
		
//		for(Entry e: set){
//			System.out.println(e.toString());
//		}
//		
//		MParam[] mp = TrainMGlvq.normalizeData(set);
//		for(Entry e: set){
//			System.out.println(e.toString());
//		}
//		
//		for(int i=0;i < mp.length;i++){
//			for(int j=0;j < mp[i].mean.size();j++){
//				System.out.println(j + "-Mean: " + mp[i].mean.data[j] + 
//						", Std: " + mp[i].std.data[j]);
//			}
//		}
	}
	
//	@Test
	public void testGlvqMahalanobis(){
		int Pos = 5 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		/*limit data = 100 max*/
//		Dataset trainset2 = new Dataset();
//		trainset2.copyInfo(trainset);
//		Dataset testset2 = new Dataset();
//		testset2.copyInfo(testset);
//		int max = 100;
//		int[] counter = new int[100];
//		for(int i=0;i < 100;i++) counter[i] = 0;
//		
//		for(int i=0;i < trainset.size();i++){
//			Entry e = trainset.get(i);
//			if(counter[e.label] < max){
//				trainset2.add(e);
//				counter[e.label]++;
//			}
//		}
//		
//		for(int i=0;i < 100;i++) counter[i] = 0;
//		for(int i=0;i < testset.size();i++){
//			Entry e = testset.get(i);
//			if(counter[e.label] < max){
//				testset2.add(e);
//				counter[e.label]++;
//			}
//		}
//		
//		trainset = trainset2;
//		testset  = testset2;
		
//		MGlvq net = new MGlvq(TrainMGlvq.normalizeData(trainset));
//		net.initCodes(trainset);
//		//net.initCodesRandom(trainset);
//		
//		TrainMGlvq train = new TrainMGlvq(net, trainset, 0.005);
//		train.setMaxEpoch(150);
//		
//		/*monitor*/
//		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + " Codebook Monitor", net.codebook);
//		cbm.pack();
//		RefineryUtilities.centerFrameOnScreen(cbm);
//		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		cbm.setVisible(true);
//		
//		do{
//			train.iteration();
//			cbm.update(train.getCurrEpoch());
//			System.out.println(train.getError());
//		}while(!train.shouldStop());
//
//		ConfusionMatrix cm = new ConfusionMatrix(nclass);
//		//testset = trainset;
//		StringBuilder Failed = new StringBuilder();
//		Iterator<Entry> it = testset.iterator();
//		while(it.hasNext()){
//			Entry sample = it.next(); 
//			
//			int win = net.classify(sample);
//			int target = sample.label;
//			
//			cm.feed(win, target);
//			if(win != target)
//				Failed.append(sample.toString() + "\n");
//		}
//		
//		//utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
//		utils.log(cm.toString());
//		utils.log("Test Result :");
//		utils.log(String.format("True : %d", cm.getTruePrediction()));
//		utils.log(String.format("Total: %d", cm.getTotal()));
//		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
	
//	@Test
	public void testMGlvq2(){
		int Pos = 5 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		Dataset extset   = new Dataset(Parameter.DATA_EXT[0]);
		
		trainset.load();
		testset.load();
		extset.load();
		
		//load extset to testset
		for(Entry e: extset){
			//if(e.label == 13){
				e.label = -1;
				testset.add(e);
			//}
		}
		
		//split data
//		Dataset trainset1 = new Dataset();
//		trainset1.copyInfo(trainset);
//		Dataset trainset2 = new Dataset();
//		trainset2.copyInfo(trainset);
//		
//		DatasetProfiler dp = new DatasetProfiler();
//		dp.run(trainset);
//		
//		for(PEntry pe: dp){
//			int max = pe.size()/2;
//			
//			for(int i=0; i < pe.size();i++){
//				Entry e = trainset.get(pe.get(i));
//				if(i < max)
//					trainset1.add(e);
//				else
//					trainset2.add(e);
//			}
//		}
		
//		MGlvq2 net = new MGlvq2(TrainMGlvq.normalizeData(trainset));
//		net.initCodes(trainset);
//
//		TrainMGlvq train = new TrainMGlvq(net, trainset, 0.005);
//		train.setMaxEpoch(150);
//		
//		/*monitor*/
//		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + " Codebook Monitor", net.codebook);
//		cbm.pack();
//		RefineryUtilities.centerFrameOnScreen(cbm);
//		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		cbm.setVisible(true);
//		
//		do{
//			train.iteration();
//			cbm.update(train.getCurrEpoch());
//			System.out.println(train.getError());
//		}while(!train.shouldStop());
//
//		//calculate threshold
//		net.calcThreshold(trainset);
//		System.out.println(net.toString());
//		
//		ConfusionMatrix cm = new ConfusionMatrix(nclass);
//		Iterator<Entry> it = testset.iterator();
//		while(it.hasNext()){
//			Entry sample = it.next(); 
//			
//			int win = net.classify(sample);
//			int target = sample.label;
//			
//			cm.feed(win, target);
//		}
//		
//		utils.log(cm.toString());
//		utils.log("Test Result :");
//		utils.log(String.format("True : %d", cm.getTruePrediction()));
//		utils.log(String.format("Total: %d", cm.getTotal()));
//		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}	
}
