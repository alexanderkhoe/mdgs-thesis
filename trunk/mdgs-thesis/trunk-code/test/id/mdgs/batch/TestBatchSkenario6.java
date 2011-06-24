package id.mdgs.batch;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;
import id.mdgs.fnlvq.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Skenario 6
 * Mengetahui Effect Outlier removal terhadap akurasi
 * Pada skenario ini digunakan Alg. GLVQ dengan 
 * parameter : 	alpha -> 0.05
 * 				bobot -> random dataset
 * 				iterasi -> 150
 * 
 * Fpglvq, alpha:0.05, bobot 0.5d random, iter 150
 * 
 * @author I Made Agus Setiawan
 *
 */
public class TestBatchSkenario6 {
	public static Dataset[] trainsets;
	public static Dataset[] testsets;
	public static Dataset[] trainsetsOut;
	public static Dataset[] testsetsOut;
	public static int[] nclass = {12, 6};
	public static int[] fiture = {300 , 300};
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	
	public static int NUM_DATA = 1;
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
	
	public static String msg;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		utils.header("Load dataset");
		trainsets 	= new Dataset[NUM_DATA];
		testsets	= new Dataset[NUM_DATA];
		trainsetsOut= new Dataset[NUM_DATA];
		testsetsOut	= new Dataset[NUM_DATA];
		
		int[] idx = {3, 0};
		for(int i=0; i < NUM_DATA;i++){
			utils.log(String.format("Load #%d", i));
			int pos = idx[i] * 4;
			
			trainsets[i] 		= new Dataset(Parameter.DATA[pos + 0]);
			testsets[i] 		= new Dataset(Parameter.DATA[pos + 1]);
			trainsetsOut[i] 	= new Dataset(Parameter.DATA[pos + 3]);
			testsetsOut[i] 		= new Dataset(Parameter.DATA[pos + 2]);
			
			trainsets[i].load();
			testsets[i].load();
			trainsetsOut[i].load();
			testsetsOut[i].load();			
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
	
	public int MAX_ATTEMPT 	  = 10;
	
	@Test
	public void mainTest() throws IOException{
		testGlvqOut();
		testFpglvqOut();
//		testGlvqNon();
//		testFpglvqNon();
	}
	
	public void testGlvqOut() throws IOException{
		writer = createWriter("Skenario6.Glvq" + ".detail");
		resumeWriter = createWriter("Skenario6.Glvq");
		
		msg = "Outlier";
		utils.header("Running testGlvq");
		utils.log(writer, "TrainGlvq - " + msg + " init: random from dataset");
		utils.log(resumeWriter, "TrainGlvq - " + msg + " init: random from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrainO\taccTestO\taccTest\t");
		sbh.append("BaccTrainO\tBaccTestO\tBaccTest\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alpha,iteration));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Lvq net = new Lvq();
					net.initCodes(trainsetsOut[dt]);
					
					TrainLvq1 train = new TrainGlvq(net, trainsetsOut[dt], alpha);
					train.setMaxEpoch(iteration);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test1(net.codebook, trainsetsOut[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsetsOut[dt], nclass[dt]);
					cm3 = test1(net.codebook, testsets[dt], nclass[dt]);
					
					//best codebook
					cm4 = test1(train.bestCodebook.codebook, trainsetsOut[dt], nclass[dt]);
					cm5 = test1(train.bestCodebook.codebook, testsetsOut[dt], nclass[dt]);
					cm6 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
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
							alpha, iteration));
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
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alpha, iteration));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainGlvq done");
	}
	
	public void testFpglvqOut() throws IOException{
		writer = createWriter("Skenario6.Fpglvq" + ".detail");
		resumeWriter = createWriter("Skenario6.Fpglvq");
		
		msg="Outlier";
		utils.header("Running testFpglvq");
		utils.log(writer, "TrainFpglvq - " + msg + " init: 0.5d, random:true");
		utils.log(resumeWriter, "TrainFpglvq - " + msg + " init: 0.5d, random:true");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrainO\taccTestO\taccTest\t");
		sbh.append("BaccTrainO\tBaccTestO\tBaccTest\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alpha,iteration));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Fpglvq net = new Fpglvq();
					net.initCodes(trainsetsOut[dt], 0.5d, true);
					
					TrainFpglvq train = new TrainFpglvq(net, trainsetsOut[dt], alpha);
					train.setMaxEpoch(iteration);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test2(net.codebook, trainsetsOut[dt], nclass[dt]);
					cm2 = test2(net.codebook, testsetsOut[dt], nclass[dt]);
					cm3 = test2(net.codebook, testsets[dt], nclass[dt]);
					
					//best codebook
					cm4 = test2(train.bestCodebook.codebook, trainsetsOut[dt], nclass[dt]);
					cm5 = test2(train.bestCodebook.codebook, testsetsOut[dt], nclass[dt]);
					cm6 = test2(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
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
							alpha, iteration));
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
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alpha, iteration));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainGlvq done");
	}

	
	public void testGlvqNon() throws IOException{
		writer = createWriter("Skenario6.Glvq" + ".detail");
		resumeWriter = createWriter("Skenario6.Glvq");
		
		msg = "NonOutlier";
		utils.header("Running testGlvq");
		utils.log(writer, "TrainGlvq - " + msg + " init: random from dataset");
		utils.log(resumeWriter, "TrainGlvq - " + msg + " init: random from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alpha,iteration));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Lvq net = new Lvq();
					net.initCodes(trainsets[dt]);
					
					TrainLvq1 train = new TrainGlvq(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);
					cm3 = test1(net.codebook, testsetsOut[dt], nclass[dt]);
					
					//best codebook
					cm4 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm5 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					cm6 = test1(train.bestCodebook.codebook, testsetsOut[dt], nclass[dt]);
					
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
							alpha, iteration));
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
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alpha, iteration));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainGlvq done");
	}
	
	public void testFpglvqNon() throws IOException{
		writer = createWriter("Skenario6.Fpglvq" + ".detail");
		resumeWriter = createWriter("Skenario6.Fpglvq");
		
		msg="NonOutlier";
		utils.header("Running testFpglvq");
		utils.log(writer, "TrainFpglvq - " + msg + " init: 0.5d, random:true");
		utils.log(resumeWriter, "TrainFpglvq - " + msg + " init: 0.5d, random:true");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alpha,iteration));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[6];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Fpglvq net = new Fpglvq();
					net.initCodes(trainsets[dt], 0.5d, true);
					
					TrainFpglvq train = new TrainFpglvq(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = test2(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test2(net.codebook, testsets[dt], nclass[dt]);
					cm3 = test2(net.codebook, testsetsOut[dt], nclass[dt]);
					
					//best codebook
					cm4 = test2(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm5 = test2(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					cm6 = test2(train.bestCodebook.codebook, testsetsOut[dt], nclass[dt]);
					
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
							alpha, iteration));
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
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alpha, iteration));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t%7.4f", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainGlvq done");
	}

	
}
