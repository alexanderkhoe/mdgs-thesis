package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fwlvq.Fwlvq;
import id.mdgs.fwlvq.TrainFwlvq;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.util.Iterator;

import org.junit.Test;


public class TestFwlvq {
	@Test
	public void testFnlvq(){
		
		LvqUtils.resetLabels();
		int nclass = 12;
//		Dataset trainset = new Dataset(utils.getDefaultPath() + "/resources/dataTrain50.txt");
//		Dataset testset  = new Dataset(utils.getDefaultPath() + "/resources/dataTest50.txt");

		/*data 300 fitur*/
//		Dataset trainset = new Dataset(utils.getDefaultPath() +"/resources/cekbobot/dataTrain15Kelas300.txt");
//		Dataset testset  = new Dataset(utils.getDefaultPath() + "/resources/cekbobot/dataTest15Kelas300.txt");

		int Pos = 3 * 4;
		Dataset trainset = new Dataset(Parameter.DATA[Pos + 0]);
		Dataset testset  = new Dataset(Parameter.DATA[Pos + 3]);
		
		trainset.load();
		testset.load();
//		trainset = testset;
		Fwlvq net = new Fwlvq();
		net.initCodes(trainset, 5);
//		net.initCodes(trainset);
		
		TrainFwlvq train = new TrainFwlvq(net, trainset, 0.001);
		train.setMaxEpoch(100);
		
//		System.out.println(net.wavelet(0));
//		System.out.println(net.wavelet(1));
//		System.exit(0);
		
		int iteration = 0;
		do {
			train.iteration3();
//			System.out.format("Epoch:%d, Error:%f\n", iteration, train.getError());
			System.out.format("Epoch:%d\n", iteration);
			iteration++;
		}while(!train.shouldStop());//while (iteration < train.maxEpoch && !MathUtils.equals(train.alpha, 0));
		
		System.out.println(net.codebook.toString());
		
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
	
//	@Test
	public void testBobotElly(){
		LvqUtils.resetLabels();
		Dataset bmax = new Dataset(utils.getDefaultPath() + "/resources/cekbobot/maxl3.300.txt");
		LvqUtils.resetLabels();
		Dataset bmean = new Dataset(utils.getDefaultPath() + "/resources/cekbobot/meanl3.300.txt");
		LvqUtils.resetLabels();
		Dataset bmin = new Dataset(utils.getDefaultPath() + "/resources/cekbobot/minl3.300.txt");
		bmax.load();
		bmean.load();
		bmin.load();
		
		int nclass = 15;
		LvqUtils.resetLabels();
		Dataset testset = new Dataset(utils.getDefaultPath() + "/resources/cekbobot/dataTest15Kelas300.txt");
		testset.load();
		
		FCodeBook codebook = new FCodeBook();
		codebook.numFeatures = bmin.numFeatures;
		for(int i=0;i < bmin.size();i++){
			Entry min = bmin.get(i);
			Entry mean = bmean.get(i);
			Entry max = bmax.get(i);
			
			FEntry fe = new FEntry(bmin.numFeatures);
			for(int j = 0;j < min.size();j++){
				fe.data[j].set(min.data[j], mean.data[j], max.data[j]);
			}
			
			fe.label = min.label;
			codebook.add(fe);
		}
		
		Fwlvq net = new Fwlvq();
		net.initCodes(codebook);
//		System.out.println(codebook.toString());
		
		ConfusionMatrix cm = new ConfusionMatrix(nclass);
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
//			sample = testset.get(1);
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
//			break;
		}
		//utils.log(DumpMatrix.dumpMatrix(network.getWeights()));
		utils.log(cm.toString());
		utils.log("Test Result :");
		utils.log(String.format("True : %d", cm.getTruePrediction()));
		utils.log(String.format("Total: %d", cm.getTotal()));
		utils.log(String.format("Accuracy = %.4f",cm.getAccuracy()));
	}
}
