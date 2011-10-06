package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.gui.CodebookMonitor;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;

public class TestFpglvq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


	}
	
	
	public void testUnknown(){
		int Pos = 1 * 3;
		int nclass = 6;
		Dataset dataset = new Dataset(Parameter.DATA_ALL[Pos + 0]);
		dataset.load();
		
		Dataset unknownset   = new Dataset(Parameter.DATA_ALL[(4*3) + 1]);
		unknownset.load();
		
		KFoldedDataset<Dataset, Entry> kfold = new KFoldedDataset<Dataset, Dataset.Entry>(dataset, 2, 0.5, false);
		FoldedDataset<Dataset, Entry> trainset = kfold.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset	= kfold.getKFoldedForTest(0);
		
		
		
	}
	
	public void testFnglvq(){
		int Pos = 4 * 4;
		int nclass = 12;
		Dataset ds1 = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset ds2  = new Dataset(Parameter.DATA[Pos + 1]);
		
//		Dataset trainset = new Dataset(Parameter.ECG300C15N100_TRAIN);
//		Dataset testset  = new Dataset(Parameter.ECG300C15N100_TEST);
		
		//use unknown beat
//		Dataset unknownset   = new Dataset(Parameter.DATA_EXT[0]);
//		Dataset unknownset   = new Dataset(Parameter.DATA[(4*4) + 1]);
//		unknownset.load();
		
//		Dataset trainset = new Dataset(Parameter.DATA_UCI[Pos + 2]);
//		Dataset testset  = new Dataset(Parameter.DATA_UCI[Pos + 3]);
//		Dataset ds1 = new Dataset(Parameter.DATA_IRIS[Pos + 0]);
//		Dataset ds2 = new Dataset(Parameter.DATA_IRIS[Pos + 1]);

		ds1.load();
		ds2.load();
		
//		DataNormalization norm = new DataNormalization(trainset);
//		norm.normalize(trainset);
//		norm.normalize(testset);
		
//		nclass = DataSetUtils.selectClassOnly(trainset, testset, 10);
//		unknownset.load();
		
		//add unknown beat to test set
//		for(Entry e: unknownset){
//			if(e.label == 9){
//				e.label = -1;
//				testset.add(e);
//			}
//		}
		
//		Dataset dataset = new Dataset(utils.getDefaultPath() + "/resources/mobil.txt");
//		dataset.load();
		ds1.join(ds2);
		KFoldedDataset<Dataset, Entry> ds = new KFoldedDataset<Dataset, Dataset.Entry>(ds1, 2, 0.5, false);
		FoldedDataset<Dataset, Entry> trainset = ds.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset	= ds.getKFoldedForTest(0);
		
		
		for(int i=0;i < trainset.folded.size();i++){
			System.out.println(trainset.folded.get(i));
		}
		
		System.out.println("Break");
		for(int i=0;i < testset.folded.size();i++){
			System.out.println(testset.folded.get(i));
		}
		System.exit(0);
		
		Fpglvq net = new Fpglvq();
//		net.initCodes(trainset, 5, false);
		net.initCodes(trainset, 0.5d, true);
		
//		net.findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MINIMUM);
		
		ITrain train = new TrainFpglvq(net, trainset, 0.05);
		train.setMaxEpoch(150);
		
		/*view monitor*/
		CodebookMonitor cbm = new CodebookMonitor(train.getClass().getSimpleName() + "Codebook Monitor", net.codebook, train);
		cbm.pack();
		RefineryUtilities.centerFrameOnScreen(cbm);
		cbm.setExtendedState(JFrame.MAXIMIZED_BOTH);
		cbm.setVisible(true);
		
		/*start training*/
		do{ 
//			break;
			train.iteration();
			cbm.update(train.getCurrEpoch(), train.getError());
			System.out.format("Epoch:%d, Error:%f\n", train.getCurrEpoch(), train.getError());
		}while(!train.shouldStop());
		
		//calculate threshold
//		net.calcThreshold(trainset);
		
//		System.out.println(net.codebook.toString());
		
		//testset = trainset;
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
