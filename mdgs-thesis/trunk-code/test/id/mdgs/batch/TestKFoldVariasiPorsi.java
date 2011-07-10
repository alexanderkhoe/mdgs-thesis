package id.mdgs.batch;

import id.mdgs.dataset.Dataset;
import id.mdgs.evaluation.KFoldCrossValidation;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.fnlvq.TrainFpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestKFoldVariasiPorsi {
	@Test
	public void test(){
		int id = 1;
		int pos = id * 4;
		int nclass = 12;
		Dataset trainset  = new Dataset(Parameter.DATA[pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[pos + 1]);
		
		trainset.load();
		testset.load();
		
		trainset.join(testset);
		
		
		//optimal parameter - base on train
		double[][] optimal = { //LVQ1, LVQ21, GLVQ, FPGLVQ
				{0.05, 0.075, 0.05, 0.005}, //300-6class
				{0.05, 0.075, 0.05, 0.05}, //86 -6class
				{0.075, 0.05, 0.1, 0.001},  //24 -6class
				{0.05, 0.1, 0.075, 0.01}, //300-6class
				{0.001, 0.075, 0.05, 0.01}, //86 -6class
				{0.1, 0.075, 0.1, 0.001},  //24 -6class
		};

		//base on data train
		int[][] optiter = {
				{20, 100, 100, 150},
				{20, 150, 100, 150},
				{20, 150, 100, 150},
				{50, 100, 100, 150},
				{20, 150, 100, 150},
				{50, 150, 100, 150},
		};
		
		for(int i=9;i > 0;i--){
			int k = 10; double porsi = ((double) i )/ 10;
			String fname = String.format("%d.%.1f-fold.%d-class.dataid-%d.%d-fitures", k, porsi, nclass, id, trainset.numFeatures); 
			KFoldCrossValidation kfold = new KFoldCrossValidation(trainset, k, porsi, true, utils.getDefaultPath() + "/resources/evaluation/" + fname + ".log");
			
			//register trainer
			ITrain train3 = new TrainGlvq(null, optimal[id][2]); 			train3.setMaxEpoch(optiter[id][2]);
			ITrain train4 = new TrainFpglvq(null, optimal[id][3]); 			train4.setMaxEpoch(optiter[id][3]);
			
			Glvq c3 = new Glvq();		
			Fpglvq c4 = new Fpglvq();	
			//proses init bobot di tester
			
			kfold.registerClassifier(c3, train3);
			kfold.registerClassifier(c4, train4);
			
			System.out.print(kfold.run());
		}

	}
}
