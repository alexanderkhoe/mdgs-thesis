package id.mdgs.batch;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fwlvq.Fwlvq;
import id.mdgs.fwlvq.TrainFwlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
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


public class TestFwlvq {
	public static Dataset[] trainsets;
	public static Dataset[] testsets;
	public static Dataset[] testsetsOutlier;
	public static int[] nclass = {6, 6, 6, 12, 12, 12};
	public static int[] fiture = {300, 86, 24, 300, 86, 24};
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	
	public String[] mcode = {
			"1fwlvq",
			};
	public static int urut = 0;
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

		utils.log("create resume report");
		resumeWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report/resume." +
				String.format(dateFormat.format(new Date())) + "." + 
				TestFwlvq.class.getSimpleName(), false)));		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if(resumeWriter != null){
			resumeWriter.flush();
			resumeWriter.close();
		}		
	}
	
	
	@Before
	public void setUp() throws Exception {
		utils.log("create file report");
		writer = new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report/rpt." + mcode[urut] + "." +
				String.format(dateFormat.format(new Date())) + "." + 
				this.getClass().getSimpleName(), false)));
	}

	@After
	public void tearDown() throws Exception {
		if(writer != null){
			writer.flush();
			writer.close();
		}
	}
	
	public ConfusionMatrix test(FCodeBook codebook, Dataset testset, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		Fwlvq net = new Fwlvq();
		net.initCodes(codebook);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
		}
		
		return cm;
	}
	
	public int[] iterationset = { 50, 100, 150};
	public double[] alphaset  = { 0.1, 0.075, 0.05, 0.01, 0.005, 0.001};
	public int MAX_ATTEMPT 	  = 5;
	
	@Test
	public void testFwlvq() throws IOException{
		utils.header("Running testFwlvq");
		utils.log(writer, String.format("\n%s",dateFormat.format(new Date())));
		utils.log(resumeWriter, String.format("\n%s",dateFormat.format(new Date())));
		utils.log(writer, "TrainFwlvq Opsi3");
		utils.log(resumeWriter, "TrainFwlvq Opsi3");
		urut++;
		
		/*TrainLvq*/
		for(int dt=0;dt < NUM_DATA;dt++){
			utils.log(writer, trainsets[dt].fname);
			utils.log(resumeWriter, trainsets[dt].fname);
			
			StringBuilder sbh = new StringBuilder();
			sbh.append("#\tNC\tNFit\t");
			sbh.append("alpha\tEpoch\t");
			sbh.append("time\t");
			sbh.append("bestError\tbestEpoch\tlastError\t");
			sbh.append("accTrain\taccTest\taccTestO\t");
			sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
			utils.log(writer,sbh.toString());
			utils.log(resumeWriter, sbh.toString());
			
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
					
					for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
						
						Fwlvq net = new Fwlvq();
						net.initCodes(trainsets[dt], 5);
						
						TrainFwlvq train = new TrainFwlvq(net, trainsets[dt], alphaset[a]);
						train.setMaxEpoch(iterationset[b]);
						
						utils.timer.start();
						
						do {
							train.iteration3();
						} while (!train.shouldStop());
						
						long waktu = utils.timer.stop();
						
						ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
						cm1 = test(net.codebook, trainsets[dt], nclass[dt]);
						cm2 = test(net.codebook, testsets[dt], nclass[dt]);
						cm3 = test(net.codebook, testsetsOutlier[dt], nclass[dt]);

						//best codebook
						cm4 = test(train.bestCodebook.codebook, trainsets[dt], nclass[dt]);
						cm5 = test(train.bestCodebook.codebook, testsets[dt], nclass[dt]);
						cm6 = test(train.bestCodebook.codebook, testsetsOutlier[dt], nclass[dt]);
						
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
					writer.flush();
					resumeWriter.flush();
//					return;
				}
			}
		}
		
		utils.log("TrainFwlvq Opsi3 done");
	}
}
