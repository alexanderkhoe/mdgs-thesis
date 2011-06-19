package id.mdgs.lvq;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fglvq.Fglvq;
import id.mdgs.fglvq.TrainFglvq;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.glvq.TrainGlvq;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.junit.Test;


public class TestFGlvq {
	@Test
	public void testFGLvq(){
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Fnlvq net = new Fglvq();
		//net.initCodes(trainset, 1, 5);
		net.initCodes(trainset, 5);
//		net.initCodesRandom(trainset);
		
		TrainFglvq train = new TrainFglvq(net, trainset, 0.05);
		train.setMaxEpoch(100);
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.format("Epoch:%d, Error:%f\n", iteration, train.getError());
			iteration++;
		}while (iteration < train.maxEpoch);
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		//testset = trainset;
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
	}
}
