package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.junit.BeforeClass;
import org.junit.Test;


public class TestDaubechies {
	public static Dataset[] trainsets;
	public static Dataset[] testsets;
	public static int[] nclass = {15 };
	public static int[] fiture = {38 };
	
	public static KFoldedDataset<Dataset, Entry>[] kfdsets;
	public static FoldedDataset<Dataset, Entry>[] fdoutsets;
	
	public String[] mcode = {
			"Skenario2.1Lvq1",
			"Skenario2.2Lvq21",
			"Skenario2.4Glvq",
			"Skenario2.5Fpglvq",
			"Skenario2.3Lvq3",			
			};
	public static int NUM_DATA = 5;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		utils.header("Load dataset");
		trainsets 	= new Dataset[NUM_DATA];
		testsets	= new Dataset[NUM_DATA];
		
		for(int i=0; i < NUM_DATA;i++){
			utils.log(String.format("Load #%d", i));
			int pos = i * 2;
			trainsets[i] 		= new Dataset(Parameter.DATA_DAUBECHIES[pos + 0]);
			testsets[i] 		= new Dataset(Parameter.DATA_DAUBECHIES[pos + 1]);
			
			trainsets[i].load();
			testsets[i].load();
			
			trainsets[i].join(testsets[i]);
		}

		kfdsets = new KFoldedDataset[NUM_DATA];
		
		int K = 10;
		for(int i=0; i < NUM_DATA;i++){
			kfdsets[i] 	= new KFoldedDataset<Dataset, Dataset.Entry>(trainsets[i], K, 0.5, true);
		}
	}
	
	@Test
	public void test(){
		for(int i=0;i < NUM_DATA;i++){
			FoldedDataset<Dataset, Entry> train = kfdsets[i].getKFoldedForTrain(0);
			FoldedDataset<Dataset, Entry> test = kfdsets[i].getKFoldedForTest(0);
			
			@SuppressWarnings("unchecked")
			IClassify<?, Entry>[] nets = (IClassify<?, Entry>[]) new IClassify<?, ?>[2];
			ITrain[] trainers = new ITrain[2];
			
			nets[0] = new Glvq();		//((Glvq)net[0]).initCodes(train, 1);
			nets[1] = new Fpglvq();		//((Glvq)net[0]).initCodes(train, 0.5d, true);

			//register trainer
			trainers[0] = new TrainGlvq(null, 0.05); 	trainers[0].setMaxEpoch(150);
			trainers[1] = new TrainFpglvq(null, 0.05); 	trainers[1].setMaxEpoch(200);
			
			ConfusionMatrix[] cm = new ConfusionMatrix[2];
			double[] acc = new double[2];
			for(int j=0;j < 2 ;j++){
				IClassify<?, Entry> net = nets[j];
				ITrain trainer = trainers[j];
				
				if(net instanceof Lvq) ((Lvq) net).initCodes(train, 1);
				else if(net instanceof Fnlvq) ((Fnlvq) net).initCodes(train, 1d, true);
				
				trainer.reset();
				trainer.setNetwork(net);
				trainer.setTraining(train);
				
				do {
					trainer.iteration();
				}while(!trainer.shouldStop());

				cm[j] = new ConfusionMatrix(15);
				int TP = 0;
				for(Entry sample: test){
					int win = -1, target;
					
					target = sample.label;
					win = net.classify(sample);
					cm[j].feed(win, target);
					
					if(win == target) TP++;
				}
				
				acc[j] = ((double) TP) / test.size();
			}
			System.out.println(String.format("%d\t%7.4f\t%7.4f", i, acc[0], acc[1]));; 
			//System.out.println(cm[0].toString());
			//System.out.println(cm[1].toString());
		}

	}
}
