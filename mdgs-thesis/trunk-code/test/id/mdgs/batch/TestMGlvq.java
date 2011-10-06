package id.mdgs.batch;

import static org.junit.Assert.*;
import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.glvq.mglvq.MGlvq;
import id.mdgs.glvq.mglvq.TrainMGlvq;
import id.mdgs.glvq.mglvq.WinnerByMahalanobis;
import id.mdgs.glvq.mglvq.WinnerByMahalanobis.MParam;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMGlvq {

	public static Dataset datasets;
	public static Dataset dsouts;
	
	public static KFoldedDataset<Dataset, Entry> kfdsets;
	public static FoldedDataset<Dataset, Entry> fdoutsets;
	
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
	public static int nclass = 12; 
	public static int nfiture= 24;
	
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		utils.header("Load dataset");
		
		int pos = 5 * 3;
		datasets = new Dataset(Parameter.DATA_ALL[pos + 0]);
		dsouts	 = new Dataset(Parameter.DATA_ALL[pos + 1]);
		
		datasets.load();
		dsouts.load();
		
		int K = 10;
		kfdsets 	= new KFoldedDataset<Dataset, Dataset.Entry>(datasets, K, 0.5, true);
		fdoutsets	= new FoldedDataset<Dataset, Dataset.Entry>(dsouts, true);
	}

	public PrintWriter createWriter(String tag) throws IOException{
		return new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report.v2/resume." + tag + "." +
				//String.format(dateFormat.format(new Date())) + "." + 
				this.getClass().getSimpleName(), false)));	
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
	
	public int MAX_ATTEMPT 	  = 1;
	
	@Test 
	public void MainTest() throws IOException{
		testMGlvq();
	}

	public void testMGlvq() throws IOException{
		
		double[] alphaset = {//0.9, 0.85, 0.8, 0.75, 0.7, 0.65,
							 //0.6, 0.55, 0.5, 0.45, 0.4, 0.35, 
							 //0.3, 0.25, 0.2, 0.15, 
							 0.1,0.075, 0.05, 0.01, 
							 0.0075, 0.005, 0.001};
		int[] iterationset= {50, 100, 150, 200, 250, 300, 400, 500};
		int dt = 0;
		
		writer = createWriter("MGlvq.detail");
		resumeWriter = createWriter("MGlvq");
		
		utils.header("Running testMGlvq");
		utils.log(writer, "TrainMGlvq");
		utils.log(resumeWriter, "TrainMGlvq");
		
		StringBuilder sbh = new StringBuilder();
		sbh.append("#\tNC\tNFit\t");
		sbh.append("alpha\tEpoch\t");
		sbh.append("time\ttime\t");
		sbh.append("bestError\tbestEpoch\tlastError\t");
		sbh.append("accTrain\taccTest\taccTestO\t");
		sbh.append("BaccTrain\tBaccTest\tBaccTestO\t");
		utils.log(writer, sbh.toString());
		utils.log(resumeWriter, sbh.toString());
		
		FoldedDataset<Dataset, Entry> trainset 	= kfdsets.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> testset  	= kfdsets.getKFoldedForTest(0);
		FoldedDataset<Dataset, Entry> testout 	= fdoutsets;
		
		//keep original trainset before normalize
		FoldedDataset<Dataset, Entry> train4test = new FoldedDataset<Dataset, Dataset.Entry>(trainset.getDeepDataset());
		//normalize trainset
		MParam mps = WinnerByMahalanobis.normalizeData(trainset);
		
		for(int a=alphaset.length-1;a >=0;a--){
			for(int b=0;b < iterationset.length;b++){
				utils.log(String.format("dt: %d, alpha: %f, iteration: %d", 
						dt, alphaset[a],iterationset[b]));
				
				double[] avgErr = new double[3];
				for(int i=0;i < avgErr.length;i++) avgErr[i] = 0;
				long avgtime = 0;					
				double[] avgAcc = new double[3];
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sErr = new StringBuilder();
				
				for(int attempt=0;attempt < MAX_ATTEMPT;attempt++){
					
					MGlvq net = new MGlvq(mps);
					net.initCodes(trainset, 1);
					
					TrainMGlvq train = new TrainMGlvq(net, trainset, alphaset[a]);
					train.setMaxEpoch(iterationset[b]);
					
					utils.timer.start();
					
					do {
						train.iteration();
						sErr.append(String.format("%7.4f,", train.getError()));
					} while (!train.shouldStop());
					
					long waktu = utils.timer.stop();
					
					ConfusionMatrix cm1, cm2, cm3, cm4, cm5, cm6;
					cm1 = TestBatch.testNetwork(net, net.codebook.clone(), train4test, nclass);
					cm2 = TestBatch.testNetwork(net, net.codebook.clone(), testset, nclass);
					cm3 = TestBatch.testNetwork(net, net.codebook.clone(), testout, nclass);
	
					//best codebook
//					cm4 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), trainset, nclass);
//					cm5 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testset, nclass);
//					cm6 = TestBatch.testNetwork(net, train.bestCodebook.codebook.clone(), testout, nclass);
					
					avgAcc[0] += cm1.getAccuracy();
					avgAcc[1] += cm2.getAccuracy();
					avgAcc[2] += cm3.getAccuracy();
//					avgAcc[3] += cm4.getAccuracy();
//					avgAcc[4] += cm5.getAccuracy();
//					avgAcc[5] += cm6.getAccuracy();
					avgtime	  += waktu;
					avgErr[0] += train.bestCodebook.coef;
					avgErr[1] += train.bestCodebook.epoch;
					avgErr[2] += train.getError();
					
					
					sb.append(String.format("%2d\t%2d\t%3d\t", 
							attempt+1, nclass, nfiture));
					sb.append(String.format("%7.4f\t%4d\t", 
							alphaset[a], iterationset[b]));
					sb.append(String.format("%d\t%8s\t", 
							waktu, utils.elapsedTime(waktu)));
//					sb.append(String.format("%7.4f\t%4d\t%7.4f\t", 
//							train.bestCodebook.coef, train.bestCodebook.epoch,
//							train.getError()));
	
					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
							cm1.getAccuracy(), cm2.getAccuracy(), cm3.getAccuracy()));
//					sb.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
//							cm4.getAccuracy(), cm5.getAccuracy(), cm6.getAccuracy()));
					
					sb.append("|\t" + sErr.toString());
					sb.append("\n");
				}
				
				for(int i=0;i < avgAcc.length;i++) avgAcc[i] /= MAX_ATTEMPT;
				for(int i=0;i < avgErr.length;i++) avgErr[i] /= MAX_ATTEMPT;
				avgtime /= MAX_ATTEMPT;
				
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(String.format("%2s\t%2d\t%3d\t", "##", nclass, nfiture));
				sb2.append(String.format("%7.4f\t%4d\t", 
									alphaset[a], iterationset[b]));
				sb2.append(String.format("%d\t%8s\t", avgtime, utils.elapsedTime(avgtime)));
				sb2.append(String.format("%7.4f\t%4d\t%7.4f\t", avgErr[0], Math.round(avgErr[1]), avgErr[2]));
				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f\t", 
						avgAcc[0], avgAcc[1], avgAcc[2]));
//				sb2.append(String.format("%7.4f\t%7.4f\t%7.4f", 
//						avgAcc[3], avgAcc[4], avgAcc[5]));
			
				utils.log(writer, sb.toString() + sb2.toString() + "\n");
				utils.log(resumeWriter, sb2.toString());
				break;
			}
			break;
		}
		
		closeWriter(writer);
		closeWriter(resumeWriter);
		utils.log("TrainMGlvq done");
	}
}
