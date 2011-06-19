package id.mdgs.lvq;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.glvq.TrainGlvq;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.junit.Test;


public class TestPSO {

	@Test
	public void testPSO(){
		LvqUtils.resetLabels();
		int Pos = 0 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
		net.initCodes(trainset);

		TrainPso trainInit = new TrainPso(net, trainset, 5);
		trainInit.setMaxEpoch(100);
		int iteration = 0;
		do {
//			boolean append = false;
//			for(int i=0;i<trainInit.particles.length;i++){
//				trainInit.particles[i].save(String.format("%s/resources/trash/particles-%d.txt",
//						utils.getDefaultPath(),iteration, i), append);
//				trainInit.velocities[i].save(String.format("%s/resources/trash/velocities-%d.txt",
//					utils.getDefaultPath(),iteration, i), append);
//				
//				if(!append) append = true;
//			}
			trainInit.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + trainInit.globalBest.coef);
			iteration++;

		} while (iteration < trainInit.maxEpoch);		
		
		TrainGlvq train = new TrainGlvq(net, trainset, 0.05);
		train.iteration(100);
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			
		}
		//utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
		
//		net.codebook.set(trainInit.globalBest);
//		
////		TrainLvq1 train = new TrainLvq1(net, trainset, 0.1);
//		TrainLvq1 train = new TrainLvq21(net, trainset, 0.1, 0.1);
////		TrainLvq1 train = new TrainLvq3(net, trainset, 0.1, 0.1, 0.1);
//		
//		//train.setFindWinner(new WinnerByCos());
//		
//		iteration = 0;
//		train.resetCounter();
//		do {
////			train.iterationOption1();
//			//train.iterationOption1Variant();
//			//train.iterationOption2(6*1000);
//			train.iterationOption3();
//			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
//			iteration++;
//		} while (iteration < 100);
//		
//		utils.log(String.format("alpha: %f",train.alpha));
//		ConfusionMatrix cm = new ConfusionMatrix(6);
//		
//		Iterator<Entry> it = testset.iterator();
//		while(it.hasNext()){
//			Entry sample = it.next(); 
//			
//			int win = net.classify(sample);
//			int target = sample.label;
//			
//			cm.feed(win, target);
//			
//		}
//		utils.log(cm.toString());
//		utils.log("Test Result :");
//		utils.log(String.format("True : %d", cm.getTruePrediction()));
//		utils.log(String.format("Total: %d", cm.getTotal()));
//		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));		
	}
}
