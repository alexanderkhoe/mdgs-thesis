package id.mdgs;


import id.mdgs.utils.utils;

import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.encog.util.logging.DumpMatrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestReadData {

	public static String IRIS_DATA = utils.getDefaultPath() + "/resources/iris.data";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testReadIris(){
		NeuralDataSet set = new CSVNeuralDataSet(IRIS_DATA, 4, 1, false, CSVFormat.ENGLISH);
		
		for(NeuralDataPair pair: set){
			System.out.println(DumpMatrix.dumpArray(pair.getInputArray()) + " " +
					DumpMatrix.dumpArray(pair.getIdealArray()));
		}
	}
	
}
