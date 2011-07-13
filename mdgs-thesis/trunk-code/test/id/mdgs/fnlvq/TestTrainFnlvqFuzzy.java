package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvqold.Fnlvq;
import id.mdgs.fnlvqold.FnlvqF;
import id.mdgs.fnlvqold.TrainFnlvq;
import id.mdgs.fnlvqold.TrainFnlvqFuzzy;
import id.mdgs.gui.CodebookMonitor;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;
import org.junit.Test;


public class TestTrainFnlvqFuzzy {
//	@Test
	public void testTrainFnlvqFuzzy(){
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		FnlvqF net = new FnlvqF();
		net.initCodes(trainset, 5);

		
		TrainFnlvqFuzzy train = new TrainFnlvqFuzzy(net, trainset, 0.05, 5);
		train.setMaxEpoch(5);
		
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
	
	@Test
	public void testTrainFnlvqFuzzyWithGUI(){
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 1]);
		
		trainset.load();
		testset.load();
		
		FnlvqF net = new FnlvqF();
		net.initCodes(trainset, 5);
		
		TrainFnlvqFuzzy train = new TrainFnlvqFuzzy(net, trainset, 0.05, 5);
		train.setMaxEpoch(5);
		
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + "Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		/*start training*/
		do{
			train.iteration();
			cbm.update(train.currEpoch);
			System.out.println("Epoch: " + train.currEpoch);
		}while(!train.shouldStop());
		
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
