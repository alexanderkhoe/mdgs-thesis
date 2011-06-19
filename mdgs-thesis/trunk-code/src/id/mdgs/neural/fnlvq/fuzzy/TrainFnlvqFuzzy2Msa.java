/**
 * 
 */
package id.mdgs.neural.fnlvq.fuzzy;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.NodeNeuralData;
import id.mdgs.neural.data.NodeNeuralDataSet;
import id.mdgs.neural.fnlvq.crisp.FnlvqCrisp;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.utils;

import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvqFuzzy2Msa extends TrainFnlvqFuzzy1Msa {

	/**
	 * @param network
	 * @param trainData
	 * @param learningRate
	 */
	public TrainFnlvqFuzzy2Msa(FnlvqCrisp network, NeuralDataSet trainData,
			double learningRate) {
		super(network, trainData, learningRate);
	}

	/**
	 * @param network
	 * @param trainData
	 * @param learningRate
	 * @param numOfGrouping
	 */
	public TrainFnlvqFuzzy2Msa(FnlvqCrisp network, NeuralDataSet trainData,
			double learningRate, int numOfGrouping) {
		super(network, trainData, learningRate, numOfGrouping);
	}
	
	/**
	 * Fuzzify crisp data into number of group, and find it's min, mean and max
	 * if number of record = 1, the number gonna be the center and 
	 * 		min and max will be expand from center 1 unit
	 * if number of records between 2 until numOfGrouping, it will find
	 * 		min, mean and max. In addition, min and max added with 0.1 unit
	 * else it will calculate as ussual, except for the last group 
	 * 		will be grouped with the remaining records. 
	 * 		fe: 16 records with 5 each, then there are 3 groups
	 * 		The 3rd group will contain 6 records.		
	 * 		
	 * @param set
	 * @param numOfGrouping
	 * @return
	 */
	public static NodeNeuralDataSet generateFuzzyData(NeuralDataSet set, int numOfGrouping){
		Map<Integer, List<Integer>> map = DataSetUtils.getClassTable(set);
	    NeuralDataPair pair = BasicNeuralDataPair.createPair(
	    		set.getInputSize(), set.getIdealSize());
	    
	    NodeNeuralDataSet fuzzyset = new NodeNeuralDataSet();
	    
		for(Integer c: map.keySet()){
			List<Integer> clist = map.get(c);
			int numOfRecordInClass = clist.size();
			
			if(numOfRecordInClass >= numOfGrouping){
				int NF = (int) Math.floor(numOfRecordInClass/numOfGrouping);
				NodeNeuralData inNode = null;
				
				for(int x=0;x < NF;x++){
					//create data
	    			inNode = new NodeNeuralData(set.getInputSize());
	    			for(int i=0;i<inNode.size();i++){
	    				inNode.setData(i, 
	    						new FuzzyNode(Double.POSITIVE_INFINITY, 0, 
	    								Double.NEGATIVE_INFINITY));
	    			}
	    			
	    			int start, end;
	    			start = x*numOfGrouping;
	    			end   = (x+1)*numOfGrouping;
	    			if(x == NF-1)
	    				end   = numOfRecordInClass;
	    			
					//fill in data
	    			for(int y = start;y < end;y++){
	    				int pos = clist.get(y);
	    				set.getRecord(pos, pair);
	    				
	    				for(int i=0;i<pair.getInput().size();i++){
			    			double inval = pair.getInputArray()[i];
			    			FuzzyNode node = (FuzzyNode) inNode.getData(i);

			    			if(inval < node.min)	node.min = inval;
			    			if(inval > node.max)	node.max = inval;
			    			node.mean += inval;
			    		}	    				
	    			}
	    			
	    			for(int i=0;i<pair.getInput().size();i++){
		    			FuzzyNode node = (FuzzyNode) inNode.getData(i);
		    			node.mean /= (end - start);
	    			}
	    			
	    			//add data
	    			fuzzyset.add(inNode, pair.getIdeal().clone());
				}
			}
			
			else {
				if(numOfRecordInClass >= 2){
					NodeNeuralData inNode = null;
	    			//create fuzzy data
					inNode = new NodeNeuralData(set.getInputSize());
	    			for(int i=0;i<inNode.size();i++){
	    				inNode.setData(i, 
	    						new FuzzyNode(Double.POSITIVE_INFINITY, 0, 
	    								Double.NEGATIVE_INFINITY));
	    			}
	    			
	    			for(Integer pos: clist){
			    		//pos is index of data in trainingset
			    		set.getRecord(pos, pair);
			    		
			    		for(int i=0;i<pair.getInput().size();i++){
			    			double inval = pair.getInputArray()[i];
			    			FuzzyNode node = (FuzzyNode) inNode.getData(i);

			    			if(inval < node.min)	node.min = inval;
			    			if(inval > node.max)	node.max = inval;
			    			node.mean += inval;
			    		}			    		
	    			}
	    			
	    			for(int i=0;i<inNode.size();i++){
		    			FuzzyNode node = (FuzzyNode) inNode.getData(i);
		    			node.min  -= 0.1;
		    			node.mean /= numOfRecordInClass; 
		    			node.max  += 0.1;
	    			}
	    			
	    			fuzzyset.add(inNode, pair.getIdeal().clone());
				}

				else {
		    		//define data as center and expand fuzzy by 0.1
		    		set.getRecord(clist.get(0), pair);
		    		NodeNeuralData inNode = new NodeNeuralData(set.getInputSize());
		    		for(int i=0;i<pair.getInputArray().length;i++){
		    			double inval = pair.getInputArray()[i];
		    			FuzzyNode node = new FuzzyNode(); 
		    			node.min  = inval - 1;
		    			node.mean = inval; 
		    			node.max  = inval + 1;
		    			
		    			inNode.setData(i, node);
		    		}
		    		
		    		fuzzyset.add(inNode, pair.getIdeal().clone());
		    	} 
			}
		}
		return fuzzyset;
	}
	
	@Override
	protected void adjustWeights(WeightMatrix weights, 
			Matrix mius, NodeNeuralData input, int win, int target) {

		//unknown class (-1)
		if (win == -1){
		
			for(int cluster = 0; cluster < weights.getCols(); cluster++){
				for(int i = 0; i < weights.getRows(); i++){
					FuzzyNode fn = (FuzzyNode) weights.get(i, cluster);
					this.delta = 1.0003d;
					
					fn.min  = fn.mean - this.delta * (fn.mean - fn.min);
					fn.max  = fn.mean + this.delta * (fn.max - fn.mean);
					Assert.assertTrue("win==-1 , " + i + cluster,fn.max > fn.min);
				}
			}
		}
		
		//equals
		else if (win == target){
			//shift winning cluster toward the target
			for (int r=0; r<weights.getRows();r++){
				FuzzyNode fn1 	= (FuzzyNode) weights.get(r, win);
				double miu 	 	= mius.get(r, win);
				FuzzyNode data 	= (FuzzyNode) input.getData(r);
		
				//penyebab max < min 
				//shift = fn1.mean = c1; --> salah variable
				final double c1= fn1.mean;
				fn1.mean = fn1.mean + this.alpha * (1 - miu) * (data.mean - fn1.mean);
				final double shift = fn1.mean - c1;
				fn1.min  = fn1.min + shift;
				fn1.max  = fn1.max + shift;
				
				//increase fuzziness (extend fuzziness)
				final double Range = fn1.mean - fn1.min;
				fn1.min = fn1.mean - (1+0.25*(1 - miu)*this.alpha) * Range;
				fn1.max = fn1.mean + (1+0.25*(1 - miu)*this.alpha) * Range;
		
				Assert.assertTrue("win==ideal, " + r + "," + win ,fn1.max > fn1.min);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			for (int r=0; r<weights.getRows();r++){
				FuzzyNode fn1 	= (FuzzyNode) weights.get(r, win);
				double miu 	 	= mius.get(r, win);
				FuzzyNode data 	= (FuzzyNode) input.getData(r);
		
				final double c1= fn1.mean;
				fn1.mean = fn1.mean - this.alpha * (1 - miu) * (data.mean - fn1.mean);
				final double shift = fn1.mean - c1;
				fn1.min  = fn1.min + shift;
				fn1.max  = fn1.max + shift;
		
				//decrease fuzziness (narrow fuzziness)
				FuzzyNode fnOld = new FuzzyNode();
				fnOld.copy(fn1);
				//penyebab terjebak segitiga rapet aka width mendekati 0
				//(1 - (miu*this.alpha)) awalnya ((1 - miu)*this.alpha)
				final double Range = fn1.mean - fn1.min;
				fn1.min = fn1.mean - (1 - (miu*this.alpha)) * Range;
				fn1.max = fn1.mean + (1 - (miu*this.alpha)) * Range;	
				
				if (!(fn1.max > fn1.mean)){
					utils.log("win != target, min try to exceed max, rollback.\n" +
							"cur: " + fn1.toString() + "\n"  +
							"old: " + fnOld.toString());
					fn1.copy(fnOld);
				}
				
				Assert.assertTrue("win!=ideal, " + r + "," + win ,fn1.max > fn1.min);
			}
		}
	}	
}
