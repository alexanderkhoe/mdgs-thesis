package id.mdgs.evaluation;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
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


public class TestMcNemar {

	@Test
	public void test(){
		int pos = 4 * 4;
		Dataset trainset  = new Dataset(Parameter.DATA[pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[pos + 1]);
		
		trainset.load();
		testset.load();
		
		FoldedDataset<Dataset, Entry> trset = new FoldedDataset<Dataset, Entry>(trainset, true);
		FoldedDataset<Dataset, Entry> teset = new FoldedDataset<Dataset, Entry>(testset, true);
		
		McNemarTest mntest = new McNemarTest(utils.getDefaultPath() + "/resources/mcnemar/12");
		
		//register trainer
		//6 kelas 
		ITrain train1 = new TrainLvq1(trset, 0.05); train1.setMaxEpoch(150);
		ITrain train2 = new TrainLvq21(trset, 0.05, 0.005); train2.setMaxEpoch(150);
//		ITrain train3 = new TrainGlvq(trset, 0.05); train3.setMaxEpoch(150);
//		ITrain train4 = new TrainFpglvq(trset, 0.05); train4.setMaxEpoch(150);
		
		//12 kelas
		ITrain train3 = new TrainGlvq(trset, 0.075); train3.setMaxEpoch(150);
		ITrain train4 = new TrainFpglvq(trset, 0.01); train4.setMaxEpoch(150);
		
		mntest.registerTrainer(TrainLvq1.class.getSimpleName(), train1);
		mntest.registerTrainer(TrainLvq21.class.getSimpleName(), train2);
		mntest.registerTrainer(TrainGlvq.class.getSimpleName(), train3);
		mntest.registerTrainer(TrainFpglvq.class.getSimpleName(), train4);
		
		Lvq c1 = new Lvq();		c1.initCodes(trainset, 1, 5);
		Lvq c2 = new Lvq();		c2.initCodes(trainset, 1, 5);
		Glvq c3 = new Glvq();		c3.initCodes(trainset, 1, 5);
		Fpglvq c4 = new Fpglvq();	c4.initCodes(trainset, 1d, false);
		
//		mntest.registerClassifier(TrainLvq1.class.getSimpleName(), c1);
//		mntest.registerClassifier(TrainLvq21.class.getSimpleName(), c2);
		mntest.registerClassifier(TrainGlvq.class.getSimpleName(), c3);
		mntest.registerClassifier(TrainFpglvq.class.getSimpleName(), c4);
		
		String result = mntest.run(teset);
		
		System.out.print(result);
	}
}
