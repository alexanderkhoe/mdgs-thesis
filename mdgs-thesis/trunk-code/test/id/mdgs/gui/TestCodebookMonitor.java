package id.mdgs.gui;

import java.util.Iterator;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.lvq.glvq.TrainGlvq;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import javax.swing.JFrame;

import org.encog.neural.networks.training.Train;
import org.jfree.ui.RefineryUtilities;

public class TestCodebookMonitor {
	public static void main(String [] args){
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
		net.initCodes(trainset);
//		net.initCodesRandom(trainset);
		
		TrainGlvq train = new TrainGlvq(net, trainset, 0.005);
		train.setMaxEpoch(200);
		
//		TrainLvq21 train = new TrainLvq21(net, trainset, 0.001, 0.005);
//		train.setMaxEpoch(200);
		
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + "Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		/*start training*/
		do{
			train.iterationOption3();
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
