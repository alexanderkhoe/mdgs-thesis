package id.mdgs.data;

import id.mdgs.batch.TestLvq;
import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.DatasetProfiler;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestDataDesc {
	public static Dataset[] trainsets;
	public static Dataset[] testsets;
	public static Dataset[] testsetsOutlier;
	public static int[] nclass = {6, 6, 6, 12, 12, 12};
	public static int[] fiture = {300, 86, 24, 300, 86, 24};
	public static PrintWriter writer; 
	public static PrintWriter resumeWriter;
	
	public String[] mcode = {
			"1Lvq1-Opsi1",
			"2Lvq1-Opsi3",
			"3Lvq21Opsi3",
			"4Lvq3-Opsi3",
			"5Glvq-Opsi1",
			"6Glvq-Opsi3",
			"7GlvqPSOOpsi3",
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

		utils.log("create data desc report");
		resumeWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report/data.description.txt"
				, false)));		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if(resumeWriter != null){
			resumeWriter.flush();
			resumeWriter.close();
		}		
	}	
	
	@Test
	public void test(){
		DatasetProfiler dp1, dp2, dp3;
		for(int i=0; i < NUM_DATA;i++){
			dp1 = new DatasetProfiler();
			dp2 = new DatasetProfiler();
			dp3 = new DatasetProfiler();
			
			dp1.run(trainsets[i]);
			dp2.run(testsets[i]);
			dp3.run(testsetsOutlier[i]);
			
			utils.log(resumeWriter, String.format("Kelas: %d, Fitur: %d",
					nclass[i], fiture[i]));
			utils.log(resumeWriter, "Kelas\tTrain\tTest\tTest Outlier");
			for(int j=0;j < dp1.size();j++){
				utils.log(resumeWriter, 
						String.format("%d\t%d\t%d\t%d",j, 
								dp1.entries.get(j).data.size(),
								dp2.entries.get(j).data.size(),
								dp3.entries.get(j).data.size()));
			}
			utils.log(resumeWriter, "");
			
		}
	}
}
