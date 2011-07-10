package id.mdgs.glvq;

import id.mdgs.dataset.DataNormalization;
import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.ZScoreNormalization;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Lvq;
import id.mdgs.master.ITrain;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

public class TestGlvq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int Pos = 4 * 4;
		int nclass = 12;
		Dataset dset1 = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset dset2  = new Dataset(Parameter.DATA[Pos + 1]);
//		Dataset trainset = new Dataset(Parameter.ECG300C15N100_TRAIN);
//		Dataset testset  = new Dataset(Parameter.ECG300C15N100_TEST);
//		int Pos = 0 * 2;
//		int nclass = 4;
//		Dataset trainset = new Dataset(Parameter.DATA_UCI[Pos + 2]);
//		Dataset testset  = new Dataset(Parameter.DATA_UCI[Pos + 3]);
		
//		Dataset trainset = new Dataset(Parameter.DATA_IRIS[Pos + 0]);
//		Dataset testset  = new Dataset(Parameter.DATA_IRIS[Pos + 1]);
		
//		trainset.load();
//		testset.load();
		
//		Dataset dataset = new Dataset(utils.getDefaultPath() + "/resources/mobil.txt");
//		dataset.load();

		dset1.load();
		dset2.load();
		dset1.join(dset2);
		
		KFoldedDataset<Dataset, Entry> ds = new KFoldedDataset<Dataset, Dataset.Entry>(dset1, 2, 0.5, true);
		FoldedDataset<Dataset, Entry> trainset = ds.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset	= ds.getKFoldedForTest(0);
		
//		DataNormalization norm = new ZScoreNormalization(trainset);
//		norm.normalize(trainset);
//		norm.normalize(testset);
		
		Glvq net = new Glvq();
//		net.initCodes(trainset, 1, 5);
		net.initCodes(trainset, 1);
		
		ITrain train = new TrainGlvq(net, trainset, 0.05);
		train.setMaxEpoch(150);
//		((TrainGlvq)train).getTraining().makeRoundRobin(1);
		
		/*monitor*/
		CodebookMonitor cbm = new CodebookMonitor("Glvq Codebook Monitor", net.codebook, train);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		do{
			train.iteration();
			cbm.update(train.getCurrEpoch(), train.getError());
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
