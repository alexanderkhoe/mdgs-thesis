package id.mdgs.dataset;

import java.util.Iterator;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset.FoldedIterator;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.Test;


public class TestLoadWeight {

	@Test
	public void test(){
		int pos = 0 * 4;
		int nclass = 6;
		Dataset trainset  = new Dataset(Parameter.DATA[pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[pos + 1]);
		
		trainset.load();
		testset.load();
		
//		Glvq net = new Glvq();
//		net.loadCodebook(utils.getDefaultPath() + "/resources/McNemar/" + TrainGlvq.class.getSimpleName() + ".prototype");
		Lvq net = new Lvq();
		net.loadCodebook(utils.getDefaultPath() + "/resources/McNemar/" + TrainLvq21.class.getSimpleName() + ".prototype");
		
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
//		FoldedDataset<Dataset, Entry> teset = new FoldedDataset<Dataset, Entry>(testset, true);
//		FoldedIterator it1 = (FoldedIterator) teset.iterator();
		Iterator<Entry> it = testset.iterator();
		
//		for(int i=0;i < teset.size();i++){
//			System.out.println(teset.folded.get(i) + "\t" + teset.get(i).label);
//		}
		
//		for(int i=0;i < testset.size();i++){
//			System.out.println(i + "\t" + testset.get(i).label);
//		}

//		for(int i=0;i < teset.size();i++){
//			
//			Entry sample1 = (Entry) teset.get(i);
//			
//			int win1 = net.classify(sample1);
//			
//			Entry sample2 = (Entry) testset.get(teset.folded.get(i));
//			int win2 = net.classify(sample2);
//			
//			if(teset.folded.get(i) == 3000){
//				System.out.println(sample1.toString());
//				System.out.println(sample2.toString()); break;
//			}
//			//System.out.println(teset.folded.get(i) + "\t" + sample1.label + "\t" + sample2.label + "\t" + win1 + "\t" + win2);
//		}
			
		int c = 0;
		while(it.hasNext()){
			Entry sample = (Entry) it.next(); 
			System.out.print(c + "\t" ); c++;
			int win = net.classify(sample);
			int target = sample.label;
			if(win == target) {
				System.out.println(1);
			} else {
				System.out.println(0);
			}
			
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
