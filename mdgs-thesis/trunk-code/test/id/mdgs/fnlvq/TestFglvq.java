package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvqold.Fglvq;
import id.mdgs.fnlvqold.TrainFglvq;
import id.mdgs.gui.CodebookMonitor;
import id.mdgs.master.ITrain;
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
		int Pos = 0 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
//		Dataset trainset = new Dataset(Parameter.ECG300C15N100_TRAIN);
//		Dataset testset  = new Dataset(Parameter.ECG300C15N100_TEST);
		
		//use unknown beat
//		Dataset unknownset   = new Dataset(Parameter.DATA_EXT[0]);
//		Dataset unknownset   = new Dataset(Parameter.DATA[(4*4) + 1]);
		
		
		trainset.load();
		testset.load();
//		unknownset.load();
		
		//add unknown beat to test set
//		for(Entry e: unknownset){
//			if(e.label == 9){
//				e.label = -1;
//				testset.add(e);
//			}
//		}
		
		Fglvq net = new Fglvq();
		net.initCodes(trainset, 5);
		
		ITrain train = new TrainFglvq(net, trainset, 0.005);
		train.setMaxEpoch(1000);
		
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + "Codebook Monitor", net.codebook, train);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		/*start training*/
		do{ 
//			break;
			train.iteration();
			cbm.update(train.getCurrEpoch(), train.getError());
			System.out.format("Epoch:%d, Error:%f\n", train.getCurrEpoch(), train.getError());
		}while(!train.shouldStop());
		
		//calculate threshold
//		net.calcThreshold(trainset);
		
//		System.out.println(net.codebook.toString());
		
		//testset = trainset;
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
