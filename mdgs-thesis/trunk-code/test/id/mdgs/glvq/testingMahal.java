package id.mdgs.glvq;

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

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

public class testingMahal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int Pos = 5 * 4;
		int nclass = 12;
		Dataset dset1 = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset dset2  = new Dataset(Parameter.DATA[Pos + 1]);
		
		dset1.load();
		dset2.load();
		dset1.join(dset2);
		
		KFoldedDataset<Dataset, Entry> ds = new KFoldedDataset<Dataset, Dataset.Entry>(dset1, 2, 0.5, true);
		FoldedDataset<Dataset, Entry> trainset = ds.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset	= ds.getKFoldedForTest(0);
		
		Dataset train4test = trainset.getDeepDataset();
//		Dataset trainset = dset1;
//		Dataset testset  = dset2;
		
//		System.out.println(trainset.get(0).toString());
//		System.out.println(train4test.get(0).toString());
//		System.exit(0);
		
		MGlvq net = new MGlvq(WinnerByMahalanobis.normalizeData(trainset));
		net.initCodes(trainset, 1);

		ITrain train = new TrainMGlvq(net, trainset, 0.0001);
		train.setMaxEpoch(10);
		
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
		Iterator<Entry> it = train4test.iterator();
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

}
