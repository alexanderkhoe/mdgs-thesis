package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvqold.Fnlvq;
import id.mdgs.fnlvqold.TrainFnlvq;
import id.mdgs.gui.CodebookMonitor;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;
import org.junit.Test;

public class TestFnlvq {
	@Test
	public void testFnlvq(){
		LvqUtils.resetLabels();
		int Pos = 3 * 4;
		int nclass = 12;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 2]);
		
		trainset.load();
		testset.load();
		
		Fnlvq net = new Fnlvq();
		net.initCodes(trainset, 0.25);

		
		TrainFnlvq train = new TrainFnlvq(net, trainset, 0.001);
		train.setMaxEpoch(50);
		
		/*monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + " Codebook Monitor", net.codebook);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		int iteration = 0;
		do {
			train.iteration();
			System.out.format("Epoch:%d, Error:%f\n", iteration, train.getError());
			cbm.update(train.getCurrEpoch());
			iteration++;
		}while ( !train.shouldStop());
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
			
		}
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
}
