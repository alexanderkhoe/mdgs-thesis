package id.mdgs.evaluation;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.fnlvq.TrainFpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestKHoldOut {
	@Test
	public void test(){
		int pos = 1 * 4;
		Dataset trainset  = new Dataset(Parameter.DATA[pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[pos + 1]);
		
		trainset.load();
		testset.load();
		
		trainset.join(testset);
		
		KHoldOutTest kholdtest = new KHoldOutTest(trainset, 30, 0.5, true, utils.getDefaultPath() + "/resources/Evaluation/khold.log");
		
		//register trainer
		ITrain train1 = new TrainLvq1(null, 0.05); train1.setMaxEpoch(150);
		ITrain train2 = new TrainLvq21(null, 0.05, 0.005); train2.setMaxEpoch(150);
		ITrain train3 = new TrainGlvq(null, 0.05); train3.setMaxEpoch(150);
		ITrain train4 = new TrainFpglvq(null, 0.05); train4.setMaxEpoch(150);
		
		Lvq c1 = new Lvq();		c1.initCodes(trainset, 1, 5);
		Lvq c2 = new Lvq();		c2.initCodes(trainset, 1, 5);
		Glvq c3 = new Glvq();		c3.initCodes(trainset, 1, 5);
		Fpglvq c4 = new Fpglvq();	c4.initCodes(trainset, 1d, false);
		
		kholdtest.registerClassifier(c3, train3);
		kholdtest.registerClassifier(c4, train4);
		
		System.out.print(kholdtest.run());
	}
}
