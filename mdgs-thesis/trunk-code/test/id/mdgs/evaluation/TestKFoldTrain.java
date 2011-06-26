package id.mdgs.evaluation;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
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
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestKFoldTrain {
	@Test
	public void test(){
		int pos = 1 * 4;
		int nclass = 6;
		Dataset trainset  = new Dataset(Parameter.DATA[pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[pos + 1]);
		
		trainset.load();
		testset.load();
		
		trainset.join(testset);
		
		Dataset iris1 = new Dataset(Parameter.DATA_IRIS[0]);
		Dataset iris2 = new Dataset(Parameter.DATA_IRIS[1]);
		iris1.load();
		iris2.load();
		
		iris1.join(iris2);
		
		KFoldedDataset<Dataset, Entry> dataset = new KFoldedDataset<Dataset, Entry>(trainset, 10, 0.6, true);
		FoldedDataset<Dataset, Entry> train = dataset.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> test = dataset.getKFoldedForTest(0);
		
		@SuppressWarnings("unchecked")
		IClassify<?, Entry>[] nets = (IClassify<?, Entry>[]) new IClassify<?, ?>[2];
		ITrain[] trainers = new ITrain[2];
		
		nets[0] = new Glvq();		//((Glvq)net[0]).initCodes(train, 1);
		nets[1] = new Fpglvq();		//((Glvq)net[0]).initCodes(train, 0.5d, true);

		//register trainer
		trainers[0] = new TrainGlvq(null, 0.05); 	trainers[0].setMaxEpoch(150);
		trainers[1] = new TrainFpglvq(null, 0.05); 	trainers[1].setMaxEpoch(150);

		
		for(int k=0;k<5;k++){
			for(int i=0;i < 2 ;i++){
				IClassify<?, Entry> net = nets[i];
				ITrain trainer = trainers[i];
				//net.loadCodebook(pairs.get(i).tag);
				
				if(net instanceof Lvq) ((Lvq) net).initCodes(train, 1);
				else if(net instanceof Fnlvq) ((Fnlvq) net).initCodes(train, 0.5d, true);
				
				trainer.reset();
				trainer.setNetwork(net);
				trainer.setTraining(train);
				
				do {
					trainer.iteration();
				}while(!trainer.shouldStop());

				int TP = 0;
				for(Entry sample: test){
					int win = -1, target;
					
					target = sample.label;
					win = net.classify(sample);
					
					if(win == target) TP++;
				}
				
				double acc = ((double) TP) / test.size();
				System.out.println(trainer.getClass().getSimpleName() + "(" + k + ")" + ": " + acc); 
			}
		}

		
//		Glvq c3 = new Glvq();		c3.initCodes(train, 1);
//		Fpglvq c4 = new Fpglvq();	c4.initCodes(train, 0.5d, false);
//
//		
//		//register trainer
//		ITrain train3 = new TrainGlvq(c3, train, 0.05); train3.setMaxEpoch(150);
//		ITrain train4 = new TrainFpglvq(c4,  train, 0.05); train4.setMaxEpoch(150);
//--
//		Glvq c3 = new Glvq();		c3.initCodes(train, 1);
//		Fpglvq c4 = new Fpglvq();	c4.initCodes(train, 0.5d, true);
//
//		//register trainer
//		ITrain train3 = new TrainGlvq(null, 0.05); train3.setMaxEpoch(150);
//		ITrain train4 = new TrainFpglvq(null, 0.05); train4.setMaxEpoch(150);
//
//		//set network dan trainer
//		train3.setNetwork(c3);
//		train3.setTraining(train);
//		
//		train4.setNetwork(c4);
//		train4.setTraining(train);
//		
////		test running
//		do {
//			train3.iteration();
//		} while(!train3.shouldStop());
//		
//		ConfusionMatrix cm1 = new ConfusionMatrix(nclass);
//		for(Entry sample: test){
//			int win = -1;
//			
//			win = c3.classify(sample);
//			cm1.feed(win, sample.label);
//		}
//		
//		utils.log(cm1.toString());
//		utils.log("Test Result : " + train3.getClass().getSimpleName());
//		utils.log(String.format("True : %d", cm1.getTruePrediction()));
//		utils.log(String.format("Total: %d", cm1.getTotal()));
//		utils.log(String.format("Accuracy = %.4f",cm1.getAccuracy()));
//		
//		
//		do {
//			train4.iteration();
//		} while(!train4.shouldStop());		
//		
//		ConfusionMatrix cm2 = new ConfusionMatrix(nclass);
//		for(Entry sample: test){
//			int win = -1;
//			
//			win = c4.classify(sample);
//			cm2.feed(win, sample.label);
//		}
//		
//		utils.log(cm2.toString());
//		utils.log("Test Result : " + train4.getClass().getSimpleName());
//		utils.log(String.format("True : %d", cm2.getTruePrediction()));
//		utils.log(String.format("Total: %d", cm2.getTotal()));
//		utils.log(String.format("Accuracy = %.4f",cm2.getAccuracy()));
//-		
//		ITrain train3 = new TrainGlvq(null, 0.05); train3.setMaxEpoch(150);
//		ITrain train4 = new TrainFpglvq(null, 0.05); train4.setMaxEpoch(150);
//		
//		Lvq c1 = new Lvq();		c1.initCodes(trainset, 1, 5);
//		Lvq c2 = new Lvq();		c2.initCodes(trainset, 1, 5);

//		kholdtest.registerClassifier(c3, train3);
//		kholdtest.registerClassifier(c4, train4);
//		
//		System.out.print(kholdtest.run());

	}
}
