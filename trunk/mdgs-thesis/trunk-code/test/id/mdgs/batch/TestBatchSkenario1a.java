package id.mdgs.batch;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.fnlvq.TrainFpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestBatchSkenario1a {
	public static Dataset[] datasets;
	
	public static KFoldedDataset<Dataset, Entry>[] kfdsets;
	public static FoldedDataset<Dataset, Entry>[] fdoutsets;
	
	public static int nclass = 6;
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	public static String[] types = {"db8"}; 
	                     
	public String[] mcode = {
			"SK4a.1Lvq1",
			"SK4a.2Lvq21",
			"SK4a.4Glvq",
			"SK4a.5Fpglvq",
			"SK4a.3Lvq3",			
			};
	public static int urut = 6;
	public static int NUM_DATA = 1; //hanya db8 level 2
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
	
	
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		utils.header("Load dataset");
		datasets 	= new Dataset[NUM_DATA];
		
		for(int i=0; i < NUM_DATA;i++){
			utils.log(String.format("Load #%d", i));
			//level 2
			datasets[i] 		= new Dataset(Parameter.DATA_DB8[2]);
			datasets[i].load();
		}

		kfdsets = new KFoldedDataset[NUM_DATA];
		
		int K = 10;
		for(int i=0; i < NUM_DATA;i++){
			kfdsets[i] 	= new KFoldedDataset<Dataset, Dataset.Entry>(datasets[i], K, 0.5, true);
		}
	}

	public PrintWriter createWriter(String tag) throws IOException{
		return new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report.v0/resume." + tag + "." +
				String.format(dateFormat.format(new Date())) + "." + 
				TestLvq.class.getSimpleName(), false)));	
	}
	
	public void closeWriter(PrintWriter writer){
		if(writer != null){
			writer.flush();
			writer.close();
		}		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if(resumeWriter != null){
			resumeWriter.flush();
			resumeWriter.close();
		}		
	}
	
	public int[] iterationset = { 20, 50, 100, 150};
	public double[] alphaset  = { 0.1, 0.075, 0.05, 0.01, 0.005, 0.001};
	public double[] windowset = { 0.1, 0.75, 0.05, 0.01, 0.005, 0.001};
	public double[] epsilon   = { 0.05, 0.01, 0.005, 0.001};
	public int MAX_ATTEMPT 	  = 1;
	
	@Test 
	public void MainTest() throws IOException{
		testLvq1();
		testLvq21();
		testGlvq();
		testFplvq();
//		testLvq3();
	}
	
	public void testLvq1() throws IOException{
		int id = 0;
		String tagid = "Lvq1";
		
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running test" + tagid);
		utils.log(writer, "Train" + tagid);
		utils.log(resumeWriter, "Train" + tagid);
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tlevel\t#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTest\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTest\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		for(int dt=0;dt < NUM_DATA;dt++){
			FoldedDataset<Dataset, Entry> trainset 	= kfdsets[dt].getKFoldedForTrain(0);
			FoldedDataset<Dataset, Entry> testset  	= kfdsets[dt].getKFoldedForTest(0);
			for(int a=0;a < alphaset.length;a++){
				for(int b=0;b < iterationset.length;b++){
					utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
							dt, alphaset[a],iterationset[b]));
					
					double[] avgErr = new double[3];
					for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
					long avgtime = 0;					
					double[] avgAcc = new double[6];
					for(int i=0;i < 6;i++) avgAcc[i] = 0;
					StringBuilder sb = new StringBuilder();
					StringBuilder sErr = new StringBuilder();
					
					for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
						
						Lvq net = new Lvq();
						net.initCodes(trainset, 1);
						
						TrainLvq1 train = new TrainLvq1(net, trainset, alphaset[a]);
						train.setMaxEpoch(iterationset[b]);
						
						utils.timer.start();
						
						do {
							train.iteration();
							sErr.append(String.format("%7.4f,", train.getError()));
						} while (!train.shouldStop());
						
						long waktu = utils.timer.stop();
						
						ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
						cm1 = TestBatch.testNetwork(net, net.codebook.clone(), trainset, nclass);
						cm2 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);
						cm3 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);

						//best codebook
						cm4 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), trainset, nclass);
						cm5 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
						cm6 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
						
						avgAcc[0] += cm1.getAccuracy();
						avgAcc[1] += cm2.getAccuracy();
						avgAcc[2] += cm3.getAccuracy();
						avgAcc[3] += cm4.getAccuracy();
						avgAcc[4] += cm5.getAccuracy();
						avgAcc[5] += cm6.getAccuracy();
						avgtime	  += waktu;
						avgErr[0] += train.bestCodebook.coef;
						avgErr[1] += train.bestCodebook.epoch;
						avgErr[2] += train.getError();
						
						
						sb.append(String.format("%2d\t%d\t%s\t%2d\t%3d\t", 
								attempt+1, 0,types[0], nclass, trainset.getMasterData().numFeatures));
						sb.append(String.format("%7.4f\t%4d\t", 
								alphaset[a], iterationset[b]));
						sb.append(String.format("%d\t%8s\t", 
								waktu, utils.elapsedTime(waktu)));
						sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
								train.bestCodebook.coef, train.bestCodebook.epoch,
								train.getError()));

						sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
								cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
						sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
								cm4.getAccuracy(), cm5.getAccuracy(), cm6.getAccuracy()));
						
						sb.append("|\t" + sErr.toString());
						sb.append("\n");
					}
					
					for(int i=0;i < 6;i++) avgAcc[i] /= MAX_ATTEMPT;
					for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
					avgtime /= MAX_ATTEMPT;
					
					StringBuilder sb2 = new StringBuilder();
					
					sb2.append(String.format("%2s\t%d\t%s\t%2d\t%3d\t", "##",0, types[0], nclass, trainset.getMasterData().numFeatures));
					sb2.append(String.format("%7.4f\t%4d\t", 
										alphaset[a], iterationset[b]));
					sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
					sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
					sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
							avgAcc[0], avgAcc[1], avgAcc[2], 
							avgAcc[3], avgAcc[4], avgAcc[5]));
				
					utils.log(writer, sb.toString() + sb2.toString() + "\n");
					utils.log(resumeWriter, sb2.toString());
				}
			}

		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("Train" + tagid + " done");
	}
	
	public void testLvq21() throws IOException{
		int id = 1;
		String tagid = "Lvq21";
		double[] alphaset  = {0.075 };
		int[] iterationset = { 150 };
		double window = 0.005;
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running test" + tagid);
		utils.log(writer, "Train" + tagid);
		utils.log(resumeWriter, "Train" + tagid);
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tlevel\t#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTest\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTest\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		int a = id;
		for(int dt=0;dt < NUM_DATA;dt++){
			a=0;
			FoldedDataset<Dataset, Entry> trainset 	= kfdsets[dt].getKFoldedForTrain(0);
			FoldedDataset<Dataset, Entry> testset  	= kfdsets[dt].getKFoldedForTest(0);
			
			for(int b=0;b < iterationset.length;b++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alphaset[a],iterationset[b]));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < 6;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Lvq net = new Lvq();
					net.initCodes(trainset, 1);
					
					TrainLvq21 train = new TrainLvq21(net, trainset, alphaset[a], window);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = TestBatch.testNetwork(net, net.codebook.clone(), trainset, nclass);
					cm2 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);
					cm3 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);

					//best codebook
					cm4 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), trainset, nclass);
					cm5 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
					cm6 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgAcc[4] += cm5.getAccuracy();
					avgAcc[5] += cm6.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%s\t%2d\t%3d\t", 
							attempt+1, 0,types[0], nclass, trainset.getMasterData().numFeatures));
					sb.append(String.format("%7.4f\t%4d\t", 
							alphaset[a], iterationset[b]));
					sb.append(String.format("%d\t%8s\t", 
							waktu, utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm4.getAccuracy(), cm5.getAccuracy(), cm6.getAccuracy()));
					
					sb.append("|\t" + sErr.toString());
					sb.append("\n");
				}
				
				for(int i=0;i < 6;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%d\t%s\t%2d\t%3d\t", "##",0, types[0], nclass, trainset.getMasterData().numFeatures));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alphaset[a], iterationset[b]));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
			}
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("Train" + tagid + " done");
	}
	
	public void testGlvq() throws IOException{
		int id = 2;

		double[] alphaset  = {0.05 };
		int[] iterationset = { 150 };
		
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running testGlvq");
		utils.log(writer, "TrainGlvq");
		utils.log(resumeWriter, "TrainGlvq");
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tlevel\t#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTest\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTest\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		int a = id;
		for(int dt=0;dt < NUM_DATA;dt++){
			a=0;
			FoldedDataset<Dataset, Entry> trainset 	= kfdsets[dt].getKFoldedForTrain(0);
			FoldedDataset<Dataset, Entry> testset  	= kfdsets[dt].getKFoldedForTest(0);
			
			for(int b=0;b < iterationset.length;b++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alphaset[a],iterationset[b]));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < 6;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Glvq net = new Glvq();
					net.initCodes(trainset, 1);
					
					TrainGlvq train = new TrainGlvq(net, trainset, alphaset[a]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = TestBatch.testNetwork(net, net.codebook.clone(), trainset, nclass);
					cm2 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);
					cm3 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);

					//best codebook
					cm4 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), trainset, nclass);
					cm5 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
					cm6 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgAcc[4] += cm5.getAccuracy();
					avgAcc[5] += cm6.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%s\t%2d\t%3d\t", 
							attempt+1, 0,types[0], nclass, trainset.getMasterData().numFeatures));
					sb.append(String.format("%7.4f\t%4d\t", 
							alphaset[a], iterationset[b]));
					sb.append(String.format("%d\t%8s\t", 
							waktu, utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm4.getAccuracy(), cm5.getAccuracy(), cm6.getAccuracy()));
					
					sb.append("|\t" + sErr.toString());
					sb.append("\n");
				}
				
				for(int i=0;i < 6;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%d\t%s\t%2d\t%3d\t", "##",0, types[0], nclass, trainset.getMasterData().numFeatures));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alphaset[a], iterationset[b]));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
			}
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainGlvq done");
	}
	
	public void testFplvq() throws IOException{
		int id = 3;
		double[] alphaset  = {0.05 };
		int[] iterationset = { 150 };
		
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running testFpglvq");
		utils.log(writer, "TrainFpglvq " + "porsi: 0.5d, dengan init: random dari dataset");
		utils.log(resumeWriter, "TrainFpglvq " + "porsi: 0.5d, dengan init: random dari dataset");
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tlevel\t#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTest\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTest\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		int a = id;
		for(int dt=0;dt < NUM_DATA;dt++){
			a = 0;
			
			FoldedDataset<Dataset, Entry> trainset 	= kfdsets[dt].getKFoldedForTrain(0);
			FoldedDataset<Dataset, Entry> testset  	= kfdsets[dt].getKFoldedForTest(0);
			
			for(int b=0;b < iterationset.length;b++){
				
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alphaset[a],iterationset[b]));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < 6;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Fpglvq net = new Fpglvq();
					net.initCodes(trainset, 0.5d , true);
					
					TrainFpglvq train = new TrainFpglvq(net, trainset, alphaset[a]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = TestBatch.testNetwork(net, net.codebook.clone(), trainset, nclass);
					cm2 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);
					cm3 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);

					//best codebook
					cm4 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), trainset, nclass);
					cm5 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
					cm6 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgAcc[4] += cm5.getAccuracy();
					avgAcc[5] += cm6.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%s\t%2d\t%3d\t", 
							attempt+1, 0, types[0], 6, trainset.getMasterData().numFeatures));
					sb.append(String.format("%7.4f\t%4d\t", 
							alphaset[a], iterationset[b]));
					sb.append(String.format("%8s\t", 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm4.getAccuracy(), cm5.getAccuracy(), cm6.getAccuracy()));
					
					sb.append("|\t" + sErr.toString());
					sb.append("\n");
				}
				for(int i=0;i < 6;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%d\t%s\t%2d\t%3d\t", "##",0, types[0], 6, trainset.getMasterData().numFeatures));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alphaset[a], iterationset[b]));
				sb2.append(String.format("%8s\t", utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
			}
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainFpglvq done");
	}
}
