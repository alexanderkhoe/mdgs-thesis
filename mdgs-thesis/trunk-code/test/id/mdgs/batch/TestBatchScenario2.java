package id.mdgs.batch;


import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.fnlvq.TrainFpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.lvq.TrainLvq3;
import id.mdgs.master.ITrain;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Skenario 2
 * Mengetahui performa 5 algoritma dengan 6 jenis data 
 * Parameter yang digunakan dipilih dari percobaan terbaik untuk 6 kelas, 
 * dan digunakan untuk 12 kelas
 * Pada skenario ini digunakan
 * 1. LVQ1 init bobot, raandom
 * 2. LVQ21 init, erandom
 * 3. GLVQ  init random 
 * 2. FPGLVQ bobot	-> porsi: 0.5d, dengan init: random dari dataset
 * 
 * @author I Made Agus Setiawan
 *
 */
public class TestBatchScenario2 {

	public static Dataset[] trainsets;
	public static Dataset[] testsets;
	public static Dataset[] testsetsOutlier;
	public static int[] nclass = {6, 6, 6, 12, 12, 12};
	public static int[] fiture = {300, 86, 24, 300, 86, 24};
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	
	public String[] mcode = {
			"Skenario2.1Lvq1",
			"Skenario2.2Lvq21",
			"Skenario2.3Lvq3",
			"Skenario2.4Glvq",
			"Skenario2.5Fpglvq",
			};
	public static int urut = 6;
	public static int NUM_DATA = 6;
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		utils.header("Load dataset");
		trainsets 	= new Dataset[6];
		testsets	= new Dataset[6];
		testsetsOutlier = new Dataset[6];
		
		for(int i=0; i < NUM_DATA;i++){
			utils.log(String.format("Load #%d", i));
			int pos = i * 4;
			trainsets[i] 		= new Dataset(Parameter.DATA[pos + 0]);
			testsets[i] 		= new Dataset(Parameter.DATA[pos + 1]);
			testsetsOutlier[i] 	= new Dataset(Parameter.DATA[pos + 2]);
			
			trainsets[i].load();
			testsets[i].load();
			testsetsOutlier[i].load();
		}

	}

	public PrintWriter createWriter(String tag) throws IOException{
		return new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report.v2/resume." + tag + "." +
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
	
	public int MAX_ATTEMPT 	  = 10;
	
	public ConfusionMatrix test1(Dataset codebook, Dataset testset, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		Lvq net = new Lvq();
		net.codebook.copyInfo(codebook);
		net.codebook.addAll(codebook.entries);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
		}
		
		return cm;
	}
	
	public ConfusionMatrix test2(FCodeBook codebook, Dataset testset, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		Fpglvq net = new Fpglvq();
		net.codebook.copyInfo(codebook);
		net.codebook.addAll(codebook.entries);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
		}
		
		return cm;
	}
	
	@Test 
	public void MainTest() throws IOException{
//		testLvq1();
//		testLvq21();
//		testGlvq();
		testFplvq();
//		testLvq3();
	}
	
	public void testLvq1() throws IOException{
		double[] alphaset  = {0.05, 0.075, 0.075, 0.05, 0.075, 0.075};
		int[] iterationset = { 20 };
		
		int id = 0;
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running testLvq1");
		utils.log(writer, "TrainLvq1");
		utils.log(resumeWriter, "TrainLvq1");
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());

		for(int dt=0;dt < NUM_DATA;dt++){
			int a = dt;
			
			for(int b=0 ;b < iterationset.length; b++){
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
					net.initCodes(trainsets[dt]);
					
					TrainLvq1 train = new TrainLvq1(net, trainsets[dt], alphaset[a]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);
					cm3 = test1(net.codebook, testsetsOutlier[dt], nclass[dt]);

					//best codebook
					cm4 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm5 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					cm6 = test1(train.bestCodebook.codebook, testsetsOutlier[dt], nclass[dt]);
					
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
					
					
					sb.append(String.format("%2d\t%2d\t%3d\t", 
							attempt+1, nclass[dt], fiture[dt]));
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
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
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
		utils.log("TrainLvq1 done");
	}
	
	public void testLvq21() throws IOException{
		double[] alphaset   = {0.075,0.075,0.05,0.075,0.075,0.05};
		double[] windowset  = {0.01,0.005,0.005,0.01,0.005,0.005};
		int[] iterationset = { 150 };
		
		int id = 1;
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running testLvq21");
		utils.log(writer, "TrainLvq21");
		utils.log(resumeWriter, "TrainLvq21");
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\twidth\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		int a = id, c = id;
		for(int dt=0;dt < NUM_DATA;dt++){
			a = dt;
			c = dt;
			for(int b=0;b < iterationset.length;b++){
				
				utils.log(String.format("dt: %d, alpha: %f, window: %f, iteration: %d", 
						dt, alphaset[a],windowset[c],iterationset[b]));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < 6;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Lvq net = new Lvq();
					net.initCodes(trainsets[dt]);
					
					TrainLvq1 train = new TrainLvq21(net, trainsets[dt], alphaset[a], windowset[c]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);
					cm3 = test1(net.codebook, testsetsOutlier[dt], nclass[dt]);

					//best codebook
					cm4 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm5 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					cm6 = test1(train.bestCodebook.codebook, testsetsOutlier[dt], nclass[dt]);
					
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
					
					
					sb.append(String.format("%2d\t%2d\t%3d\t", 
							attempt+1, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%7.4f\t%4d\t", 
							alphaset[a], windowset[c], iterationset[b]));
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
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%7.4f\t%4d\t", 
									alphaset[a], windowset[c],iterationset[b]));
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
		utils.log("TrainLvq21 done");
	}	
	
	
	public void testGlvq() throws IOException{
		double[] alphaset  = {0.075,0.05,0.1,0.075,0.05,0.1};
		int[] iterationset = { 150 };
		
		int id = 3;
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running testGlvq");
		utils.log(writer, "TrainGlvq");
		utils.log(resumeWriter, "TrainGlvq");
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		int a = id;
		for(int dt=0;dt < NUM_DATA;dt++){
			a=dt;
			
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
					net.initCodes(trainsets[dt]);
					
					TrainGlvq train = new TrainGlvq(net, trainsets[dt], alphaset[a]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);
					cm3 = test1(net.codebook, testsetsOutlier[dt], nclass[dt]);

					//best codebook
					cm4 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm5 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					cm6 = test1(train.bestCodebook.codebook, testsetsOutlier[dt], nclass[dt]);
					
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
					
					
					sb.append(String.format("%2d\t%2d\t%3d\t", 
							attempt+1, nclass[dt], fiture[dt]));
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
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
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
		double[] alphaset  = {0.005,0.05,0.001,0.005,0.05,0.001};
		int[] iterationset = { 150 };
		
		int id = 4;
		writer = createWriter(mcode[id] + ".detail");
		resumeWriter = createWriter(mcode[id]);
		
		utils.header("Running testFpglvq");
		utils.log(writer, "TrainFpglvq " + "porsi: 0.5d, dengan init: random dari dataset");
		utils.log(resumeWriter, "TrainFpglvq " + "porsi: 0.5d, dengan init: random dari dataset");
		urut++;
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		int a = id;
		for(int dt=0;dt < NUM_DATA;dt++){
			a = dt;
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
					net.initCodes(trainsets[dt], 5 , true);
					
					TrainFpglvq train = new TrainFpglvq(net, trainsets[dt], alphaset[a]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test2(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test2(net.codebook, testsets[dt], nclass[dt]);
					cm3 = test2(net.codebook, testsetsOutlier[dt], nclass[dt]);

					//best codebook
					cm4 = test2(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm5 = test2(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					cm6 = test2(train.bestCodebook.codebook, testsetsOutlier[dt], nclass[dt]);
					
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
					
					
					sb.append(String.format("%2d\t%2d\t%3d\t", 
							attempt+1, nclass[dt], fiture[dt]));
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
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
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

	
	
//	public void testLvq3() throws IOException{
//	int id = 2;
//	writer = createWriter(mcode[id] + ".detail");
//	resumeWriter = createWriter(mcode[id]);
//	
//	utils.header("Running testLvq3");
//	utils.log(writer, "TrainLvq3");
//	utils.log(resumeWriter, "TrainLvq3");
//	urut++;
//	
//	StringBuilder sbh = new StringBuilder();
//	sbh.append("#\tNC\tNFit\t");
//	sbh.append("alpha\tEpoch\t");
//	sbh.append("time\t");
//	sbh.append("bestError\tbestEpoch\tlastError\t");
//	sbh.append("accTrain\taccTest\taccTestO\t");
//	sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
//	utils.log(writer, sbh.toString());
//	utils.log(resumeWriter, sbh.toString());
//	
//	int a = id, c = id, d = id;
//	for(int dt=0;dt < NUM_DATA;dt++){
//		for(int b=0;b < iterationset.length;b++){
//			
//			utils.log(String.format("dt: %d, alpha: %f, window: %f, epsilon: %f, iteration: %d", 
//					dt, alphaset[a],windowset[c], epsilon[d],iterationset[b]));
//			
//			double[] avgErr = new double[3];
//			for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
//			long avgtime = 0;					
//			double[] avgAcc = new double[6];
//			for(int i=0;i < 6;i++) avgAcc[i] = 0;
//			StringBuilder sb = new StringBuilder();
//			StringBuilder sErr = new StringBuilder();
//			
//			for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
//				
//				Lvq net = new Lvq();
//				net.initCodes(trainsets[dt], 3);
//				
//				TrainLvq1 train = new TrainLvq3(net, trainsets[dt], alphaset[a], windowset[c], epsilon[d]);
//				train.setMaxEpoch(iterationset[b]);
//				
//				utils.timer.start();
//				
//				do {
//					train.iteration();
//					sErr.append(String.format("%7.4f,", train.getError()));
//				} while (!train.shouldStop());
//				
//				long waktu = utils.timer.stop();
//				
//				ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
//				cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
//				cm2 = test1(net.codebook, testsets[dt], nclass[dt]);
//				cm3 = test1(net.codebook, testsetsOutlier[dt], nclass[dt]);
//
//				//best codebook
//				cm4 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
//				cm5 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
//				cm6 = test1(train.bestCodebook.codebook, testsetsOutlier[dt], nclass[dt]);
//				
//				avgAcc[0] += cm1.getAccuracy();
//				avgAcc[1] += cm2.getAccuracy();
//				avgAcc[2] += cm3.getAccuracy();
//				avgAcc[3] += cm4.getAccuracy();
//				avgAcc[4] += cm5.getAccuracy();
//				avgAcc[5] += cm6.getAccuracy();
//				avgtime	  += waktu;
//				avgErr[0] += train.bestCodebook.coef;
//				avgErr[1] += train.bestCodebook.epoch;
//				avgErr[2] += train.getError();
//				
//				
//				sb.append(String.format("%2d\t%2d\t%3d\t", 
//						attempt+1, nclass[dt], fiture[dt]));
//				sb.append(String.format("%7.4f\t%4d\t", 
//						alphaset[a], iterationset[b]));
//				sb.append(String.format("%8s\t", 
//						utils.elapsedTime(waktu)));
//				sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
//						train.bestCodebook.coef, train.bestCodebook.epoch,
//						train.getError()));
//
//				sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
//						cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
//				sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
//						cm4.getAccuracy(), cm5.getAccuracy(), cm6.getAccuracy()));
//				
//				sb.append("|\t" + sErr.toString());
//				sb.append("\n");
//			}
//			
//			for(int i=0;i < 6;i++) avgAcc[i] /= MAX_ATTEMPT;
//			for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
//			avgtime /= MAX_ATTEMPT;
//			
//			StringBuilder sb2 = new StringBuilder();
//			
//			sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
//			sb2.append(String.format("%7.4f\t%4d\t", 
//								alphaset[a], iterationset[b]));
//			sb2.append(String.format("%8s\t", utils.elapsedTime(avgtime)));
//			sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
//			sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
//					avgAcc[0], avgAcc[1], avgAcc[2], 
//					avgAcc[3], avgAcc[4], avgAcc[5]));
//		
//			utils.log(writer, sb.toString() + sb2.toString() + "\n");
//			utils.log(resumeWriter, sb2.toString());
//		}
//	}
//	closeWriter(writer);
//	closeWriter(resumeWriter);
//	utils.log("TrainLvq3 done");
//}	

}
