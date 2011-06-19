package id.mdgs.fnlvq;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fglvq.TrainFglvq;
import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.ITrain;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

public class TestFglvq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int Pos = 3 * 4;
		int nclass = 15;
//		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
//		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		Dataset trainset = new Dataset(Parameter.ECG300C15N100_TRAIN);
		Dataset testset  = new Dataset(Parameter.ECG300C15N100_TEST);
		trainset.load();
		testset.load();
		
		Fnlvq net = new Fnlvq();
		//net.initCodes(trainset, 1, 5);
		net.initCodes(trainset, 50);
		
		ITrain train = new TrainFglvq(net, trainset, 0.05);
		train.setMaxEpoch(50);
		
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + "Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		/*start training*/
		do{ 
//			break;
			train.iteration();
			cbm.update(train.getCurrEpoch());
			System.out.format("Epoch:%d, Error:%f\n", train.getCurrEpoch(), train.getError());
		}while(!train.shouldStop());
		
		System.out.println(net.codebook.toString());
		
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

}
