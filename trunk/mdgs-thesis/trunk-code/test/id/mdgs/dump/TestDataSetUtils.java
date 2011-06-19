package id.mdgs.dump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.encog.util.logging.DumpMatrix;
import org.junit.Test;

import id.mdgs.matrices.WeightMatrix;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.utils;


public class TestDataSetUtils {
	public static String IRIS_DATA = utils.getDefaultPath() + "/resources/iris.data";
	
//	@Test
	public void test1(){
		NeuralDataSet dataset = new CSVNeuralDataSet(IRIS_DATA, 4, 1, 
				false, CSVFormat.ENGLISH);		
		NeuralDataSet trainset = new BasicNeuralDataSet();
		NeuralDataSet initset = new BasicNeuralDataSet();
		
		DataSetUtils.splitTrainSet(dataset, trainset, initset, DataSetUtils.DATA_GROUP_SIZE);
		
		//create network
		double[][] init = DataSetUtils.calculateInitWeights(initset);
		utils.log(DumpMatrix.dumpMatrix(new Matrix(init)));
	}
	
	@Test
	public void testChangeIdealToBiner(){
		DataSetUtils.createData24N100();
		Map<Integer,List<Integer>> stat;
		stat = DataSetUtils.getClassTable(DataSetUtils.ecg24N100Part2);
		for(Integer c: stat.keySet()){
			utils.log(String.format("Class #%-2d : %d", c, stat.get(c).size()));
		}		
		DataSetUtils.changeIdealToBiner(DataSetUtils.ecg24N100Part2);
		
//		//show it
		utils.log("Ideal data after changed");
		Iterator<NeuralDataPair> it = DataSetUtils.ecg24N100Part2.iterator();
		while(it.hasNext()){
			utils.log(it.next().getIdeal().toString());
		}
		
		double[][] data = DataSetUtils.calculateInitWeights(DataSetUtils.ecg24N100Part2);
		utils.log(DumpMatrix.dumpMatrix(new Matrix(data)));
	}	
}
