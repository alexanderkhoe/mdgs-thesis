package id.mdgs.batch;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvq.*;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.lvq.*;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.DatasetProfiler.PEntry;

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
 * Skenario 5
 * Mengetahui pengaruh jumlah round robin per kelas terhadap akurasi
 * Dataset yang digunakan adalah data 300 fitur dengan 6 kelas
 * Pada skenario ini digunakan 
 * 1. Alg. GLVQ dengan parameter : 	
 * 				alpha 	-> 0.05
 * 				iterasi -> 150
 * 				bobot 	-> random dari dataset
 * 2. FPGLVQ dengan parameter :
 * 				alpha 	-> 0.05
 * 				iterasi	-> 150
 * 				bobot	-> porsi: 0.5d, dengan init: random dari dataset
 * 
 * @author I Made Agus Setiawan
 *
 */
public class TestBatchSkenario5 {
	public static Dataset[] trainsets;
	public static Dataset[] testsets;
	public static int[] nclass = {6, 6, 6, 6, 6, 6, 6};
	public static int[] fiture = {300, 157, 86, 50, 32, 23, 24};
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	
	public static int NUM_DATA = 1;
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		utils.header("Load dataset");
		trainsets 	= new Dataset[NUM_DATA];
		testsets	= new Dataset[NUM_DATA];
		
		for(int i=0; i < NUM_DATA;i++){
			utils.log(String.format("Load #%d", i));
			int pos = i * 2;
			trainsets[i] 		= new Dataset(Parameter.DATA_DECOMPOSE[pos + 0]);
			testsets[i] 		= new Dataset(Parameter.DATA_DECOMPOSE[pos + 1]);
			
			trainsets[i].load();
			testsets[i].load();
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
//		testGlvq();
		testFpglvq();
//		testGlvqSkenario5CekSemuaPola();
//		testLvq21Skenario5CekSemuaPola();
//		testLvq1Skenario5CekSemuaPola();
//		testFpglvqSkenario5CekSemuaPola();
	}
	
	public void testFpglvqSkenario5CekSemuaPola() throws IOException{
		writer = createWriter("Skenario5-SemuaPola.Fpglvq");
		
		utils.header("Running testFpglvq-SemuaPola");
		utils.log(writer, "TrainFpglvq, " + "init: get random code from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tPola\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
			
			//find max jumlah data per kelas
			DatasetProfiler dp = new DatasetProfiler();
			dp.run(trainsets[dt]);
			int maxPola = 0;
			for(PEntry pe: dp){
				if (maxPola < pe.size()){
					maxPola = pe.size();
				}
			}
			for(int pola=1;pola<=maxPola;pola++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d, Pola: %d", 
						dt, alpha,iteration, pola));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[4];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < 1;attempt++){
					
					Fpglvq net = new Fpglvq();
					net.initCodes(trainsets[dt], 0.5d, true);
					
					TrainFpglvq train = new TrainFpglvq(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);

					train.getTraining().makeRoundRobin(pola);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4;
					cm1 = test2(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test2(net.codebook, testsets[dt], nclass[dt]);

					//best codebook
					cm3 = test2(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm4 = test2(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%2d\t%3d\t", 
							attempt+1, pola, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%4d\t", 
							alpha, iteration));
					sb.append(String.format("%d\t%8s\t", waktu, 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t", 
							cm4.getAccuracy()));
					sb.append("|\t" + sErr.toString());
				}
				
				utils.log(writer, sb.toString());
			}
		}
		
		closeWriter(writer);
		utils.log("TrainFpglvq-SemuaPola done");
	}
	
	public void testLvq1Skenario5CekSemuaPola() throws IOException{
		writer = createWriter("Skenario5-SemuaPola.Lvq1");
		
		utils.header("Running testLvq1-SemuaPola");
		utils.log(writer, "TrainLvq1, " + "init: get random code from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tPola\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
			
			//find max jumlah data per kelas
			DatasetProfiler dp = new DatasetProfiler();
			dp.run(trainsets[dt]);
			int maxPola = 0;
			for(PEntry pe: dp){
				if (maxPola < pe.size()){
					maxPola = pe.size();
				}
			}
			for(int pola=1;pola<=maxPola;pola++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d, Pola: %d", 
						dt, alpha,iteration, pola));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[4];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < 1;attempt++){
					
					Lvq net = new Lvq();
					net.initCodes(trainsets[dt]);
					
					TrainLvq1 train = new TrainLvq1(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);

					train.getTraining().makeRoundRobin(pola);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);

					//best codebook
					cm3 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm4 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%2d\t%3d\t", 
							attempt+1, pola, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%4d\t", 
							alpha, iteration));
					sb.append(String.format("%d\t%8s\t", waktu, 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t", 
							cm4.getAccuracy()));
					sb.append("|\t" + sErr.toString());
				}
				
				utils.log(writer, sb.toString());
			}
		}
		
		closeWriter(writer);
		utils.log("TrainLvq1-SemuaPola done");
	}

	
	public void testLvq21Skenario5CekSemuaPola() throws IOException{
		writer = createWriter("Skenario5-SemuaPola.Lvq21");
		
		utils.header("Running testLvq21-SemuaPola");
		utils.log(writer, "TrainLvq21, " + "init: get random code from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tPola\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		double window = 0.005;
		
		for(int dt=0;dt < NUM_DATA;dt++){
			
			//find max jumlah data per kelas
			DatasetProfiler dp = new DatasetProfiler();
			dp.run(trainsets[dt]);
			int maxPola = 0;
			for(PEntry pe: dp){
				if (maxPola < pe.size()){
					maxPola = pe.size();
				}
			}
			for(int pola=1;pola<=maxPola;pola++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d, Pola: %d", 
						dt, alpha,iteration, pola));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[4];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < 1;attempt++){
					
					Lvq net = new Lvq();
					net.initCodes(trainsets[dt]);
					
					TrainLvq21 train = new TrainLvq21(net, trainsets[dt], alpha, window);
					train.setMaxEpoch(iteration);

					train.getTraining().makeRoundRobin(pola);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);

					//best codebook
					cm3 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm4 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%2d\t%3d\t", 
							attempt+1, pola, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%4d\t", 
							alpha, iteration));
					sb.append(String.format("%d\t%8s\t", waktu, 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t", 
							cm4.getAccuracy()));
					sb.append("|\t" + sErr.toString());
				}
				
				utils.log(writer, sb.toString());
			}
		}
		
		closeWriter(writer);
		utils.log("TrainLvq21-SemuaPola done");
	}

	
	
	public void testGlvqSkenario5CekSemuaPola() throws IOException{
		writer = createWriter("Skenario5-SemuaPola.Glvq");
		
		utils.header("Running testGlvq-SemuaPola");
		utils.log(writer, "TrainGlvq, " + "init: get random code from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tPola\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		
		double alpha = 0.05;
		int iteration = 150;
		
		for(int dt=0;dt < NUM_DATA;dt++){
			
			//find max jumlah data per kelas
			DatasetProfiler dp = new DatasetProfiler();
			dp.run(trainsets[dt]);
			int maxPola = 0;
			for(PEntry pe: dp){
				if (maxPola < pe.size()){
					maxPola = pe.size();
				}
			}
			for(int pola=1;pola<=maxPola;pola++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d, Pola: %d", 
						dt, alpha,iteration, pola));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[4];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < 1;attempt++){
					
					Glvq net = new Glvq();
					net.initCodes(trainsets[dt]);
					
					TrainGlvq train = new TrainGlvq(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);

					train.getTraining().makeRoundRobin(pola);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);

					//best codebook
					cm3 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm4 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%2d\t%3d\t", 
							attempt+1, pola, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%4d\t", 
							alpha, iteration));
					sb.append(String.format("%d\t%8s\t", waktu, 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t", 
							cm4.getAccuracy()));
					sb.append("|\t" + sErr.toString());
				}
				
				utils.log(writer, sb.toString());
			}
		}
		
		closeWriter(writer);
		utils.log("TrainGlvq-SemuaPola done");
	}
	
	
	public void testGlvq() throws IOException{
		writer = createWriter("Skenario5.Glvq" + ".detail");
		resumeWriter = createWriter("Skenario5.Glvq");
		
		utils.header("Running testGlvq");
		utils.log(writer, "TrainGlvq, " + "init: get random code from dataset");
		utils.log(resumeWriter, "TrainGlvq, " + "init: get random code from dataset");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tPola\tNFit\t");
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
			for(int pola=1;pola<=10;pola++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d, Pola: %d", 
						dt, alpha,iteration, pola));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[4];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Glvq net = new Glvq();
					net.initCodes(trainsets[dt]);
					
					TrainGlvq train = new TrainGlvq(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);

					train.getTraining().makeRoundRobin(pola);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4;
					cm1 = test1(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test1(net.codebook, testsets[dt], nclass[dt]);

					//best codebook
					cm3 = test1(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm4 = test1(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%2d\t%3d\t", 
							attempt+1, pola, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%4d\t", 
							alpha, iteration));
					sb.append(String.format("%d\t%8s\t", waktu, 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t", 
							cm4.getAccuracy()));
					sb.append("|\t" + sErr.toString());
					sb.append("\n");
				}
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%d\t%2d\t%3d\t", "##", pola, nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alpha, iteration));
				sb2.append(String.format("%%d\t8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
			}
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainGlvq done");
	}
	
	
	public void testFpglvq() throws IOException{
		writer = createWriter("Skenario5.Fpglvq" + ".detail");
		resumeWriter = createWriter("Skenario5.Fpglvq");
		
		utils.header("Running testFpglvq");

		double porsi = 0.5d; 
		boolean random = true;
		utils.log(writer, "TrainFpglvq , porsi: " + porsi*100 + "%, Random: " + random);
		utils.log(resumeWriter, "TrainFpglvq , porsi: " + porsi*100 + "%, Random: " + random);
		
		utils.log(writer, "TrainFpglvq");
		utils.log(resumeWriter, "TrainFpglvq");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tPola\tNFit\t");
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
			for(int pola=1;pola<=10;pola++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d, Pola: %d", 
						dt, alpha,iteration, pola));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[4];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					Fpglvq net = new Fpglvq();
					net.initCodes(trainsets[dt], 0.5d, true);
					
					TrainFpglvq train = new TrainFpglvq(net, trainsets[dt], alpha);
					train.setMaxEpoch(iteration);

					train.getTraining().makeRoundRobin(pola);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4;
					cm1 = test2(net.codebook, trainsets[dt], nclass[dt]);
					cm2 = test2(net.codebook, testsets[dt], nclass[dt]);

					//best codebook
					cm3 = test2(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
					cm4 = test2(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
					avgAcc[3] += cm4.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%d\t%2d\t%3d\t", 
							attempt+1, pola, nclass[dt], fiture[dt]));
					sb.append(String.format("%7.4f\t%4d\t", 
							alpha, iteration));
					sb.append(String.format("%d\t%8s\t", waktu, 
							utils.elapsedTime(waktu)));
					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
							train.bestCodebook.coef, train.bestCodebook.epoch,
							train.getError()));

					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
					sb.append(String.format("%7.4f\t", 
							cm4.getAccuracy()));
					sb.append("|\t" + sErr.toString());
					sb.append("\n");
				}
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%d\t%2d\t%3d\t", "##", pola, nclass[dt], fiture[dt]));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alpha, iteration));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\t", 
						avgAcc[0], avgAcc[1], avgAcc[2], 
						avgAcc[3]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
			}
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainFpglvq done");
	}
}
