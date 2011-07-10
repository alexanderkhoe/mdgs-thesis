package id.mdgs.batch;

import javax.swing.JFrame;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.evaluation.KFoldCrossValidation;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.fnlvq.TrainFpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.thesis.gui.CodebookMonitor;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.jfree.ui.RefineryUtilities;
import org.junit.Test;


public class TestSkenario8 {

	@Test
	public void test(){
		int id = 4;
		int pos = id * 4;
		int nclass = 12;
		Dataset trainset  = new Dataset(Parameter.DATA[pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[pos + 1]);
		
		trainset.load();
		testset.load();
		
		trainset.join(testset);

		int k = 10;
		KFoldedDataset<Dataset, Entry> kfold = new KFoldedDataset<Dataset, Entry>(trainset, k, 0.1d, true);
		
		//optimal parameter 
		double[][] optimal = { //LVQ1, LVQ21, GLVQ, FPGLVQ
				{0.05, 0.075, 0.075, 0.05}, //300-6class
				{0.075, 0.075, 0.05, 0.05}, //86 -6class
				{0.075, 0.05, 0.1, 0.001},  //24 -6class
				{0.05, 0.1, 0.1, 0.01}, //300-6class
				{0.001, 0.075, 0.05, 0.01}, //86 -6class
				{0.05, 0.05, 0.1, 0.001},  //24 -6class
		};
		
		int[][] optiter = {
				{20, 100, 100, 150},
				{20, 150, 100, 150},
				{20, 150, 150, 150},
				{50, 100, 100, 150},
				{20, 150, 100, 150},
				{20, 150, 100, 150},
		};
		
		//register trainer
		ITrain train1 = new TrainLvq1(null, optimal[id][0]); 			train1.setMaxEpoch(optiter[id][0]);
		ITrain train2 = new TrainLvq21(null, optimal[id][1], 0.005); 	train2.setMaxEpoch(optiter[id][1]);
		ITrain train3 = new TrainGlvq(null, optimal[id][2]); 			train3.setMaxEpoch(optiter[id][2]);
		ITrain train4 = new TrainFpglvq(null, optimal[id][3]); 			train4.setMaxEpoch(optiter[id][3]);
		
		Lvq c1 = new Lvq();		
		Lvq c2 = new Lvq();		
		Glvq c3 = new Glvq();		
		Fpglvq c4 = new Fpglvq();	
		
		//proses init bobot di tester
		FoldedDataset<Dataset, Entry> train = kfold.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> test = kfold.getKFoldedForTest(0);
		
		//------------------------------
		IClassify<?, Entry> net = c1;
		ITrain trainer = train1;
		//------------------------------
		String fname = String.format("%d-fold.%d-class", k, 12);
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor(trainer.getClass().getSimpleName() + " Codebook Monitor", (Dataset) net.getCodebook(), trainer);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		//------------------------------
		
		
		if(net instanceof Lvq) ((Lvq) net).initCodes(train, 1);
		else if(net instanceof Fnlvq) ((Fnlvq) net).initCodes(train, 0.5d, true);
		
		trainer.reset();
		trainer.setNetwork(net);
		trainer.setTraining(train);
		
		ConfusionMatrix cm;
		do {
			trainer.iteration();
			

			double[] err = new double[2];

			//test with training
			cm = TestBatch.doTest(net, train, nclass);
			err[0] = 1 - cm.getAccuracy();
			
			//test with validation set
			cm = TestBatch.doTest(net, test, nclass);
			err[1] = 1 - cm.getAccuracy();
			
			cbm.update(trainer.getCurrEpoch(), err);
		}while(!trainer.shouldStop());
		
		
	}
}
