package id.mdgs.evaluation;

import id.mdgs.dataset.Dataset;
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


public class TestKFoldCrossValidation {
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

		int k = 10; double porsi = 0.5;
		String fname = String.format("%d.%.1f-fold.%d-class.dataid-%d.%d-fitures", k, porsi, nclass, id, trainset.numFeatures); 
		KFoldCrossValidation kfold = new KFoldCrossValidation(trainset, k, porsi, true, utils.getDefaultPath() + "/resources/evaluation/" + fname + ".log");

		//optimal parameter - base on train
		double[][] optimal = { //LVQ1, LVQ21, GLVQ, FPGLVQ
				{0.05, 0.075, 0.05, 0.005}, //300-6class
				{0.05, 0.075, 0.05, 0.05}, //86 -6class
				{0.075, 0.05, 0.1, 0.001},  //24 -6class
				{0.05, 0.1, 0.075, 0.01}, //300-12class
				{0.001, 0.075, 0.05, 0.01}, //86 -12class
				{0.1, 0.075, 0.1, 0.001},  //24 -12class
		};



//		//optimal parameter - base on test
//		double[][] optimal = { //LVQ1, LVQ21, GLVQ, FPGLVQ
//				{0.05, 0.075, 0.075, 0.05}, //300-6class
//				{0.075, 0.075, 0.05, 0.05}, //86 -6class
//				{0.075, 0.05, 0.1, 0.001},  //24 -6class
//				{0.05, 0.1, 0.1, 0.01}, //300-6class
//				{0.001, 0.075, 0.05, 0.01}, //86 -6class
//				{0.05, 0.05, 0.1, 0.001},  //24 -6class
//		};

		//base on data train
		int[][] optiter = {
				{20, 100, 100, 150},
				{20, 150, 100, 150},
				{20, 150, 100, 150},
				{50, 100, 100, 150},
				//{20, 150, 100, 150},
				{150, 150, 150, 150},
				{50, 150, 100, 150},
		};
		
//		//base on data test
//		int[][] optiter = {
//				{20, 100, 100, 150},
//				{20, 150, 100, 150},
//				{20, 150, 150, 150},
//				{50, 100, 100, 150},
//				{20, 150, 100, 150},
//				{20, 150, 100, 150},
//		};
		
		//register trainer
		ITrain train1 = new TrainLvq1(null, optimal[id][0]); 			train1.setMaxEpoch(optiter[id][0]);
		ITrain train2 = new TrainLvq21(null, optimal[id][1], 0.01); 	train2.setMaxEpoch(optiter[id][1]);
		ITrain train3 = new TrainGlvq(null, optimal[id][2]); 			train3.setMaxEpoch(optiter[id][2]);
		ITrain train4 = new TrainFpglvq(null, optimal[id][3]); 			train4.setMaxEpoch(optiter[id][3]);
		
		Lvq c1 = new Lvq();		
		Lvq c2 = new Lvq();		
		Glvq c3 = new Glvq();		
		Fpglvq c4 = new Fpglvq();	
		//proses init bobot di tester
		
		kfold.registerClassifier(c1, train1);
		kfold.registerClassifier(c2, train2);
//		kfold.registerClassifier(c3, train3);
//		kfold.registerClassifier(c4, train4);
		
		System.out.print(kfold.run());
	}
}
