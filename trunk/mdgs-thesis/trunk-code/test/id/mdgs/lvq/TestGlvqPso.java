package id.mdgs.lvq;

import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.glvq.TrainGlvq;
import id.mdgs.lvq.glvq.TrainGlvqPso;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.junit.Test;


public class TestGlvqPso {
	@Test
	public void testGlvqPso(){
		LvqUtils.resetLabels();
		int Pos = 0 * 4;
		int nclass = 6;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		Lvq net = new Lvq();
		net.initCodes(trainset);
		
		TrainGlvqPso train = new TrainGlvqPso(net, trainset, 10, 0.05);
		train.setMaxEpoch(100);
		
		do{
			train.iteration();
			
		} while (!train.shouldStop());
		
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
		
		for(int i=0;i<train.particles.length;i++){
			utils.log(String.format("Particle %d = %.4f",i,train.particles[i].testing(testset, nclass)));
		}
	}
}
