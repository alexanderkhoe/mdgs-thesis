/**
 * 
 */
package id.mdgs.utils;


import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.HitList.HitEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.encog.engine.data.EngineData;
import org.encog.engine.util.EngineArray;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.encog.util.logging.DumpMatrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author I Made Agus Setiawan
 *
 */
public class DataSetUtils {
	/**
	 * MODE1
	 * ideal value in dataset required in the form of binary
	 * with length equal to number of classes
	 * if nC = 5 then ideal should be;
	 * class 0 = 1 0 0 0 0
	 * class 1 = 0 1 0 0 0 
	 * 
	 * MODE2
	 * ideal value in dataset required in decimal format
	 * then the ideal data array will contain only single value 
	 * class 0 = 0
	 * class 1 = 1 
	 * and so on
	 * NOTE: label should start from 0 
	 * 
	 * @param ideal
	 * @return
	 */	
	public static int getTarget(NeuralData ideal){
		if(ideal.getData().length == 1){
			return (int) ideal.getData()[0];
		} else {
			return EngineArray.maxIndex(ideal.getData());
		}
	}
	
	public static Map<Integer,List<Integer>> getClassTable(final NeuralDataSet set){
		Map<Integer,List<Integer>> output = new HashMap<Integer,List<Integer>>();
		NeuralDataPair pair = BasicNeuralDataPair.createPair(
				set.getInputSize(), set.getIdealSize());
		
		for(int i=0; i< set.getRecordCount(); i++){
			set.getRecord(i, pair);
			int cat = getTarget(pair.getIdeal());
			List<Integer> list;
			
			if(!output.containsKey(cat)){
				output.put(cat, new ArrayList<Integer>());
			}
			list = output.get(cat);
			list.add(i);
		}
		return output;
	}
	
	/**
	 * If the class only has 1 data (idSet.size), then
	 * it will added to the idList
	 * 
	 * @param idSet Set of index data of specific class/category 
	 * @param numOfRecordToSeek 
	 * @param idList output data contain index of specific class
	 * @return 
	 */
	public static Set<Integer> getUniqueRandomIndex(List<Integer> idSet,
			final int numOfRecordToSeek, List<Integer> idList){		
		Set<Integer> unique = new HashSet<Integer>();		
		
		int numOfCategoryRecord = idSet.size();
		//check the category distribution
		if(numOfCategoryRecord < 2){
			//add the only data on the set 
			//idx2 the real index of data
			int idx2 = idSet.get(0);
			idList.add(idx2);
			unique.add(0);
		} 
		
		else {
			int maxdata = numOfRecordToSeek;
			if (numOfCategoryRecord < 2 * maxdata){
				//pick half of it as init,  and half more as train
				maxdata = (int) numOfCategoryRecord / 2;
			}
			
			while(unique.size() < maxdata){
				int idx = RangeRandomizer.randomInt(0, numOfCategoryRecord-1);
				if(!unique.contains(idx)){
					int idx2 = idSet.get(idx);
					//add to initSet
					idList.add(idx2);
					unique.add(idx);
				}
			}
		}

		return unique;
	}
	
	/**
	 * wah salah split data, pair masih pake dimensi 4x1, skr fixed
	 * @param set
	 * @param trainset
	 * @param initset
	 * @param numOfRecordToSeek
	 * @return
	 */
	public static List<List<Integer>> splitTrainSet(final NeuralDataSet set, 
			final NeuralDataSet trainset, final NeuralDataSet initset, 
			int numOfRecordToSeek){
		
		List<List<Integer>> output = new ArrayList<List<Integer>>();
		
		//find data per category
		Map<Integer,List<Integer>> stat = getClassTable(set);
		NeuralDataPair pair;
		
		for(Integer cat: stat.keySet()){
			int numOfCategoryRecord = stat.get(cat).size();
			
			List<Integer> initList = new ArrayList<Integer>();
			Set<Integer> idUniqueSet = getUniqueRandomIndex(stat.get(cat), 
					numOfRecordToSeek, initList);
			
			for(Integer pos: initList){
				pair = BasicNeuralDataPair.createPair(
						set.getInputSize(), set.getIdealSize());
				set.getRecord(pos, pair);
				initset.add(pair);			
			}
					
			//if the class only has 1 data, copy the data also to the train
			if(numOfCategoryRecord < 2){
				pair = BasicNeuralDataPair.createPair(
						set.getInputSize(), set.getIdealSize());
				set.getRecord(stat.get(cat).get(0), pair);
				trainset.add(pair);
			} 
			//if class has more than 1 data, separate it uniquely 
			//between initset and trainset
			else {
				for(int pos = 0; pos < numOfCategoryRecord; pos++){
					if(!idUniqueSet.contains(pos)){
						//get index of dataset
						pair = BasicNeuralDataPair.createPair(
								set.getInputSize(), set.getIdealSize());
						int idx2 = stat.get(cat).get(pos);
						//add to trainSet
						set.getRecord(idx2, pair);
						trainset.add(pair);
					}
				}
			}
			output.add(initList);
		}
		return output;
	}
	
	public static List<List<Integer>> splitTrainSet(final NeuralDataSet set, 
            final NeuralDataSet trainset, final NeuralDataSet initset, 
            double initPortion){
    
    List<List<Integer>> output = new ArrayList<List<Integer>>();
    
    //find data per category
    Map<Integer,List<Integer>> stat = getClassTable(set);
    
    for(Integer cat: stat.keySet()){
            int numOfCategoryRecord = stat.get(cat).size();
            int numOfRecordToSeek = (int)(initPortion * numOfCategoryRecord);
            List<Integer> idList = new ArrayList<Integer>();
            Set<Integer> idUniqueSet = getUniqueRandomIndex(stat.get(cat), 
                            numOfRecordToSeek, idList);
            
            for(Integer pos: idList){
                NeuralDataPair pair = BasicNeuralDataPair.createPair(4, 1);
                set.getRecord(pos, pair);
                initset.add(pair);                      
            }
                            
            //add the rest to trainset
            for(int pos = 0; pos < numOfCategoryRecord; pos++){
                if(!idUniqueSet.contains(pos)){
                    //get index of dataset
                    int idx2 = stat.get(cat).get(pos);
                    //add to trainSet
                    NeuralDataPair pair = BasicNeuralDataPair.createPair(4, 1);
                    set.getRecord(idx2, pair);
                    trainset.add(pair);
                }
            }
            output.add(idList);
    	}
    	return output;
	}
	
	/**
	 * wah ternyata salah hitung initial weight , pair nya pake 4,1
	 * @param set
	 * @return
	 */
	public static double[][] calculateInitWeights(NeuralDataSet set){
		double[][] output = new double[set.getInputSize()*set.getIdealSize()][3];
		//reset
		for(int i=0;i<output.length;i++){
			output[i][0] = Double.POSITIVE_INFINITY; //min
			output[i][1] = 0;   //mean
			output[i][2] = Double.NEGATIVE_INFINITY;//max
		}
		
		//find data per category
		Map<Integer,List<Integer>> stat = getClassTable(set);
		
		//sort category 
	    SortedSet<Integer> sortedset= new TreeSet<Integer>(stat.keySet());
	    Iterator<Integer> it = sortedset.iterator();
	    NeuralDataPair pair = BasicNeuralDataPair.createPair(
	    		set.getInputSize(), set.getIdealSize());
	    
	    int cat = 0;
	    while (it.hasNext()) {
	    	cat = it.next();
	    	int numOfRecordInClass = stat.get(cat).size();
	    	int instanceNum = 0;
	    	
	    	if(numOfRecordInClass < 2){
	    		//define data as center and expand fuzzy by 0.1
	    		set.getRecord(stat.get(cat).get(0), pair);
	    		for(int i=0;i<pair.getInputArray().length;i++){
	    			double inval = pair.getInputArray()[i];
	    			double[] node = output[cat*set.getInputSize()+i];
	    			node[0] = inval - (0.1*inval);
	    			node[1] = inval; 
	    			node[2] = inval + (0.1*inval);
	    		}
	    		continue;
	    	} else {
		    	for(Integer pos: stat.get(cat)){
		    		set.getRecord(pos, pair);
		    		
		    		//utils.log(String.format("%d, %s",cat, DumpMatrix.dumpArray(pair.getInputArray())));
		    		for(int i=0;i<pair.getInputArray().length;i++){
		    			double inval = pair.getInputArray()[i];
		    			double[] node = output[cat*set.getInputSize()+i];

		    			if(inval < node[0])	node[0] = inval;
		    			if(inval > node[2])	node[2] = inval;
		    			node[1] += inval;
		    		}
		    		
		    		instanceNum++;
				}
		    	//calc mean
		    	for(int i=0;i<set.getInputSize();i++){
		    		output[cat*set.getInputSize()+i][1] /= instanceNum;
		    	}		    	
	    	}
	    }
	    
		return output;
	}
	
	
	/**
	 * CHANGE TARGET DATA FROM DECIMAL TO BINER
	 */
	public static void changeIdealToBiner(final NeuralDataSet set){
		Iterator<NeuralDataPair> it = set.iterator();
		//find number of class
		Set<Integer> classes = new HashSet<Integer>();
		while(it.hasNext()){
			NeuralDataPair pair = it.next();
			classes.add((int) pair.getIdealArray()[0]);
		}

		it = set.iterator();
		while(it.hasNext()){
			NeuralDataPair pair = it.next();
			NeuralData ideal = pair.getIdeal();
			double[] targetIdeal;
			int target;
			
			if(ideal.getData().length == 1){
				targetIdeal = new double[classes.size()];
				target = (int) ideal.getData()[0];
				EngineArray.fill(targetIdeal, 0);
				targetIdeal[target] = 1;
				ideal.setData(targetIdeal);
			}		
		}
	}
	
	public static NeuralDataSet myDataset2EncogDataset(Dataset dtset){
		NeuralDataSet set = new BasicNeuralDataSet();
		
		//Hitlist
		HitList hl = new HitList();
		hl.run(dtset);
		
		for(Entry e: dtset){
			NeuralDataPair pair = BasicNeuralDataPair.createPair(e.size(), 1);
			double[] ideal = new double[hl.size()];
			EngineArray.fill(ideal, 0);
			ideal[e.label] = 1;
			
			pair.setInputArray(e.data);
			pair.setIdealArray(ideal);
	        set.add(pair);
		}
            
		return set;
	}
	
	public static int selectClassOnly(Dataset train, Dataset test, int threshold){
		Dataset tmp1 = new Dataset(); tmp1.copyInfo(train);
		Dataset tmp2 = new Dataset(); tmp2.copyInfo(test);
		
		HitList hl = new HitList();
		hl.run(train);
		
		int numClass = 0;
		for(HitEntry he: hl){
			
			if(he.freq >= threshold){
				Iterator<Entry> it1 = train.iterator();
				while(it1.hasNext()){
					Entry e = it1.next();
					if(he.label == e.label)
//						e.label = numClass;
						tmp1.add(e);
				}

				Iterator<Entry> it2 = test.iterator();
				while(it2.hasNext()){
					Entry e = it2.next();
					if(e.label == he.label){
//						e.label = numClass;
						tmp2.add(e);
					}
				}
				
				numClass++;
			}
		}
		
		train.entries = tmp1.entries;
		test.entries  = tmp2.entries;
		return numClass;
	}
}



