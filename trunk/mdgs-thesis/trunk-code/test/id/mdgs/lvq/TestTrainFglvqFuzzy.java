package id.mdgs.lvq;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fglvq.FglvqF;
import id.mdgs.fglvq.TrainFglvqFuzzy;
import id.mdgs.fnlvq.FCodeBook;
import id.mdgs.fnlvq.FnlvqF;
import id.mdgs.fnlvq.TrainFnlvqFuzzy;
import id.mdgs.fnlvq.FCodeBook.FEntry;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.junit.Test;


public class TestTrainFglvqFuzzy {
	@Test
	public void testTrainFglvqFuzzy(){
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		FglvqF net = new FglvqF();
		net.initCodes(trainset, 5);

		
		TrainFglvqFuzzy train = new TrainFglvqFuzzy(net, trainset, 0.05, 5);
		train.setMaxEpoch(10);
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.format("Epoch:%d, Error:%f\n", iteration, train.getError());
			
//			utils.log(net.codebook.toString());
//			System.exit(0);
			
			iteration++;
		}while (iteration < train.maxEpoch);
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		FCodeBook ftestset = TrainFnlvqFuzzy.generateFuzzyData(testset, 5);
		Iterator<FEntry> it = ftestset.iterator();
		while(it.hasNext()){
			FEntry sample = it.next(); 
			
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
