package id.mdgs.fnlvq;

import id.mdgs.dataset.DataNormalization;
import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.ZScoreNormalization;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvqold.Fglvq;
import id.mdgs.fnlvqold.TrainFglvq;
import id.mdgs.master.ITrain;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

public class TestFpglvq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int Pos = 4 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
//		Dataset trainset = new Dataset(Parameter.ECG300C15N100_TRAIN);
//		Dataset testset  = new Dataset(Parameter.ECG300C15N100_TEST);
		
		//use unknown beat
//		Dataset unknownset   = new Dataset(Parameter.DATA_EXT[0]);
		Dataset unknownset   = new Dataset(Parameter.DATA[(4*4) + 1]);
		unknownset.load();
		
//		Dataset trainset = new Dataset(Parameter.DATA_UCI[Pos + 2]);
//		Dataset testset  = new Dataset(Parameter.DATA_UCI[Pos + 3]);
		trainset.load();
		testset.load();
		
//		DataNormalization norm = new DataNormalization(trainset);
//		norm.normalize(trainset);
//		norm.normalize(testset);
		
//		nclass = DataSetUtils.selectClassOnly(trainset, testset, 10);
//		unknownset.load();
		
		//add unknown beat to test set
//		for(Entry e: unknownset){
//			if(e.label == 9){
//				e.label = -1;
//				testset.add(e);
//			}
//		}
		
		Fpglvq net = new Fpglvq();
//		net.initCodes(trainset, 5, false);
		net.initCodes(trainset, 1000, false);
		
		ITrain train = new TrainFpglvq(net, trainset, 0.075);
		train.setMaxEpoch(150);
		
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
