/**
 * 
 */
package id.mdgs.dump;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dump.glvq.TrainGlvq1;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.TrainLvq3;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.junit.Test;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TestInitPCA {
	@Test
	public void testInitPca(){
		LvqUtils.resetLabels();
		int Pos = 0 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		Dataset initset  = new Dataset(utils.getDefaultPath() + "/resources/initbobot.pca");
		trainset.load();
		testset.load();
		initset.load();
		
		Lvq net = new Lvq();
		net.codebook.copyInfo(initset);
		net.codebook.addAll(initset.entries);
		//net.initCodes(trainset);
		
		
//		TrainLvq1 train = new TrainLvq1(net, trainset, 0.1);
//		TrainLvq1 train = new TrainLvq21(net, trainset, 0.01, 0.005);
		TrainLvq1 train = new TrainLvq3(net, trainset, 0.01, 0.005, 0.005);
		
		int iteration = 0;
		train.resetCounter();
		do {
			train.iterationOption3();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
			iteration++;
		} while (iteration < 100);
		
//		TrainGlvq train = new TrainGlvq(net, trainset, 0.05);
//		train.iteration(124);
		
//		utils.log(String.format("alpha: %f",train.alpha));
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
	}
}
