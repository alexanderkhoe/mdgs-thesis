package id.mdgs.lvq;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.glvq.TrainGlvq;
import id.mdgs.lvq.glvq.TrainGlvqMsa;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;
import org.junit.Test;

public class TestGlvq {
//	@Test
	public void testGLvq() throws IOException{
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
//		net.initCodes(trainset, 1, 5);
		net.initCodes(trainset);
		//net.initCodesRandom(trainset);
		
		TrainGlvq train = new TrainGlvq(net, trainset, 0.005);
		train.setMaxEpoch(100);
		
//		StringBuilder[] codebook = new StringBuilder[nclass];
//		for(int i=0;i<nclass;i++) {
//			codebook[i] = new StringBuilder();
//			codebook[i].append(net.codebook.get(i).toString() + "\n");
//		}
		
		/*monitor*/
		CodebookMonitor cbm = new CodebookMonitor("Glvq Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		do{
			train.iteration();
			
//			for(int i=0;i<nclass;i++){
//				codebook[i].append(net.codebook.get(i).toString() + "\n");
//			}
			cbm.update(train.getCurrEpoch());
		}while(!train.shouldStop());

//		for(int i=0;i<nclass;i++){
//			codebook[i].append(net.codebook.get(i).toString());
//			BufferedWriter bw = utils.openWriter(utils.getDefaultPath() + "/resources/code"+ i +".txt",
//					false);
//			bw.write(codebook[i].toString());
//			bw.flush();
//			bw.close();
//		}
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		//testset = trainset;
		StringBuilder Failed = new StringBuilder();
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			if(win != target)
				Failed.append(sample.toString() + "\n");
		}
		
		//utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		
		/*save*/
//		BufferedWriter bw = utils.openWriter(utils.getDefaultPath() + "/resources/failed.txt",
//				false);
//		bw.write(Failed.toString());
//		bw.flush();
//		bw.close();
//		
//		net.codebook.save(utils.getDefaultPath() + "/resources/checkfailed.txt");
		try {
			cbm.wait(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
//	@Test
	public void testGlvqMsa() {
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 2]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
//		net.initCodes(trainset, 1, 5);
		net.initCodes(trainset);
//		net.initCodesRandom(trainset);
		
		ITrain train = new TrainGlvqMsa(net, trainset, 0.005);
		train.setMaxEpoch(100);
		
		/*monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + " Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		do{
			train.iteration();
			cbm.update(train.getCurrEpoch());
			System.out.println(((TrainGlvqMsa) train).msa.toString2());
		}while(!train.shouldStop());
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		StringBuilder Failed = new StringBuilder();
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			if(win != target)
				Failed.append(sample.toString() + "\n");
		}
		
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		System.console().readLine();
	}
	
	@Test
	public void testGLvqTest() throws IOException{
		LvqUtils.resetLabels();
		int Pos = 5 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
//		net.initCodes(trainset, 1, 5);
		net.initCodes(trainset);
		//net.initCodesRandom(trainset);
		
		TrainGlvq train = new TrainGlvq(net, trainset, 0.005);
		train.setMaxEpoch(100);
		
		/*monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + " Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		do{
			train.iteration();
			cbm.update(train.getCurrEpoch());
		}while(!train.shouldStop());

		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		//testset = trainset;
		StringBuilder Failed = new StringBuilder();
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			if(win != target)
				Failed.append(sample.toString() + "\n");
		}
		
		//utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		
	}
}
