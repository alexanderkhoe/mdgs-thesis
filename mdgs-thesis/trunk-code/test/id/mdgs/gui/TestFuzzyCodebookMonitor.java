package id.mdgs.gui;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvqold.Fnlvq;
import id.mdgs.fnlvqold.TrainFnlvq;
import id.mdgs.fwlvq.Fwlvq;
import id.mdgs.fwlvq.TrainFwlvq;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

public class TestFuzzyCodebookMonitor {
	public static void main(String [] args){
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Fwlvq net = new Fwlvq();
		net.initCodes(trainset, 5);
		
		TrainFwlvq train = new TrainFwlvq(net, trainset, 0.001);
		train.setMaxEpoch(100);
		
//		Fnlvq net = new Fnlvq();
//		net.initCodes(trainset, 5);
//		
//		TrainFnlvq train = new TrainFnlvq(net, trainset, 0.005);
//		train.setMaxEpoch(100);
		
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor("Fwlvq Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		/*start training*/
		do{
			train.iteration();
			cbm.update(train.currEpoch);
			System.out.println("Epoch: " + train.currEpoch);
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
		
	}
}
