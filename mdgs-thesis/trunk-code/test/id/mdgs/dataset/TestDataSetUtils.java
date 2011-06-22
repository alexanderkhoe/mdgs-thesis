package id.mdgs.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Parameter;
import id.mdgs.utils.utils;

import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.junit.Test;


public class TestDataSetUtils {

	public static NeuralDataSet dataset;
	public static Map<Integer,List<Integer>> stat;
	public static List<Integer> initList;
	public Set<Integer> initUnique;
	/**
	 * Make sure empty line is not exist in data file
	 * when using CSVNeuralDataSet.  
	 */
	@Test
	public void testGetClassTable(){
		utils.header("Test getClassTable()"); 
		dataset = new CSVNeuralDataSet(Parameter.IRIS_DATA, 4, 1, 
				false, CSVFormat.ENGLISH);
		utils.log(String.format("%d",dataset.getRecordCount()));
		stat = DataSetUtils.getClassTable(dataset);
		for(Integer c: stat.keySet()){
			List<Integer> list = stat.get(c);
			
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("#%d = %d -> [", c, list.size()));
			int n = 0;
			for(Integer i: list){
				sb.append(i);
				n++;
				if(n < list.size()){
					sb.append(" ,");
				}
			}
			sb.append("]");
			
			utils.log(sb.toString());
		}
	}
	
	@Test
	public void testGetUniqueRandomIndex(){
		utils.header("Test getUniqueRandomIndex()");
		for (Integer c: stat.keySet()){
			
			initList = new ArrayList<Integer>();
			initUnique = DataSetUtils.getUniqueRandomIndex(stat.get(c), 
					3, initList);
			
			StringBuilder sb1 = new StringBuilder();
			sb1.append(String.format("Init#%d = %d -> [", c, initList.size()));
			int n = 0;
			for(Integer i: initList){
				sb1.append(i);
				n++;
				if(n < initList.size()){
					sb1.append(" ,");
				}
			}
			sb1.append("]");
			
			utils.log(sb1.toString());
			
			//else data
			StringBuilder sb2 = new StringBuilder();
			sb2.append(String.format("Train#%d = %d -> [", c, initList.size()));
			n = 0;
			int numOfCategoryRecord = stat.get(c).size();
			if(numOfCategoryRecord < 2){
				sb2.append(stat.get(c).get(0) + ",");
			} 
			else {
				for(int pos = 0; pos < numOfCategoryRecord; pos++){
					if(!initUnique.contains(pos)){
						sb2.append(stat.get(c).get(pos) + ",");
					}
				}
			}

			sb2.append("]");
			
			utils.log(sb2.toString());
		}
	}
	
	@Test
	public void testSplitTrainSet(){
		utils.header("Test splitTrainSet()");
		NeuralDataSet trainset = new BasicNeuralDataSet();
		NeuralDataSet initset  = new BasicNeuralDataSet();
		List<List<Integer>> lists=DataSetUtils.splitTrainSet(dataset, trainset, initset, 5);
		
		utils.log("init index");
		for(int c=0;c<lists.size();c++){
			StringBuilder sb2 = new StringBuilder();
			sb2.append(String.format("#%d = %d -> [",c, lists.get(c).size()));
			for(int x=0;x<lists.get(c).size();x++){
				sb2.append(lists.get(c).get(x) + ",");
			}
			sb2.append("]");
			utils.log(sb2.toString());
		}
		
		utils.log("initset");
		for(int c=0;c<initset.getRecordCount();c++){
			StringBuilder sb2 = new StringBuilder();
			NeuralDataPair pair = BasicNeuralDataPair.createPair(
					initset.getInputSize(), initset.getIdealSize());
			initset.getRecord(c, pair);
			sb2.append(String.format("%d. #%d -> [",c, (int)pair.getIdealArray()[0]));
			for(int x=0;x<pair.getInputArray().length;x++){
				sb2.append(pair.getInputArray()[x] + ",");
			}
			sb2.append("]");
			utils.log(sb2.toString());
		}
		
		utils.log("trainset");
		for(int c=0;c<trainset.getRecordCount();c++){
			StringBuilder sb2 = new StringBuilder();
			NeuralDataPair pair = BasicNeuralDataPair.createPair(
					trainset.getInputSize(), trainset.getIdealSize());
			trainset.getRecord(c, pair);
			sb2.append(String.format("%d. #%d -> [",c, (int) pair.getIdealArray()[0]));
			for(int x=0;x<pair.getInputArray().length;x++){
				sb2.append(pair.getInputArray()[x] + ",");
			}
			sb2.append("]");
			utils.log(sb2.toString());
		}
	}
	
	@Test
	public void testCalculateInitWeights(){
		utils.header("Test calculateInitWeights()");
		NeuralDataSet trainset = new BasicNeuralDataSet();
		NeuralDataSet initset  = new BasicNeuralDataSet();
		List<List<Integer>> lists=DataSetUtils.splitTrainSet(dataset, trainset, initset, 5);
		
		utils.log("initset");
		for(int c=0;c<initset.getRecordCount();c++){
			StringBuilder sb2 = new StringBuilder();
			NeuralDataPair pair = BasicNeuralDataPair.createPair(
					initset.getInputSize(), initset.getIdealSize());
			initset.getRecord(c, pair);
			sb2.append(String.format("%d. #%d -> [",c, (int)pair.getIdealArray()[0]));
			for(int x=0;x<pair.getInputArray().length;x++){
				sb2.append(pair.getInputArray()[x] + ",");
			}
			sb2.append("]");
			utils.log(sb2.toString());
		}
		
		DataSetUtils.changeIdealToBiner(initset);
		double[][] w = DataSetUtils.calculateInitWeights(initset);
		for(int x=0;x<w.length;x++){
			utils.log(String.format("#%d - [%f, %f, %f]", x, w[x][0],w[x][1],w[x][2]));
		}
	}
}
