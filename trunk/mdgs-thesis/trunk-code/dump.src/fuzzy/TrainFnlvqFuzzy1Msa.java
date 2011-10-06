/**
 * 
 */
package id.mdgs.neural.fnlvq.fuzzy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;

import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.Node;
import id.mdgs.neural.data.NodeNeuralData;
import id.mdgs.neural.data.NodeNeuralDataPair;
import id.mdgs.neural.data.NodeNeuralDataSet;
import id.mdgs.neural.fnlvq.crisp.FnlvqCrisp;
import id.mdgs.neural.fnlvq.crisp.TrainFnlvqCrispMsa;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvqFuzzy1Msa extends TrainFnlvqCrispMsa {

	protected NodeNeuralDataSet fuzzyset;
	/**
	 * @param network
	 * @param trainData
	 * @param learningRate
	 */
	public TrainFnlvqFuzzy1Msa(FnlvqCrisp network, NeuralDataSet trainData,
			double learningRate) {
		this(network, trainData, learningRate, 5);
	}

	public TrainFnlvqFuzzy1Msa(FnlvqCrisp network, NeuralDataSet trainData,
			double learningRate, int numOfGrouping) {
		super(network, trainData, learningRate);
		
		this.fuzzyset = generateFuzzyData(trainData, numOfGrouping);
		
		this.initWeights();
	}

	public void initWeights(){
		Node[][] init = new Node[this.network.getInputCount()][this.network.getOutputCount()];
		Set<Integer> cl = new HashSet<Integer>();
		
		for(NodeNeuralDataPair pair: this.fuzzyset){
			int id = DataSetUtils.getTarget(pair.getIdeal());
			if(!cl.contains(id)){
				cl.add(id);
				
				for(int i=0;i<pair.getInput().size();i++){
					init[i][id] = pair.getInputArray()[i];
				}
			}
		}
		
		this.network.setWeights(new WeightMatrix(init));
	}
	
	/**
	 * Fuzzify crisp data into number of group, and find it's min, mean and max
	 * if number of record = 1, the number gonna be the center and 
	 * 		min and max will be expand from center 1 unit
	 * if number of records between 2 until numOfGrouping, it will find
	 * 		min, mean and max. In addition, min and max added with 0.1 unit
	 * else it will calculate as ussual, except for the last group 
	 * 		fe: 16 records with 5 each then there are 4 groups
	 * 		The 3rd group will contain 5 records.
	 * 		AND the 4th group will contain 1 record and will 		
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
				int grpCounter = 0, dataCounter = 0;
	    		NodeNeuralData inNode = null;
		    	for(Integer pos: clist){
		    		//pos is index of data in trainingset
		    		set.getRecord(pos, pair);
		    		
		    		if (grpCounter % numOfGrouping == 0){
		    			//create fuzzy data
		    			inNode = new NodeNeuralData(pair.getInput().size());
		    			for(int i=0;i<inNode.size();i++){
		    				inNode.setData(i, 
		    						new FuzzyNode(Double.POSITIVE_INFINITY, 0, 
		    								Double.NEGATIVE_INFINITY));
		    			}
		    		}
		    		
		    		for(int i=0;i<pair.getInput().size();i++){
		    			double inval = pair.getInputArray()[i];
		    			FuzzyNode node = (FuzzyNode) inNode.getData(i);

		    			if(inval < node.min)	node.min = inval;
		    			if(inval > node.max)	node.max = inval;
		    			node.mean += inval;
		    		}
		    		
		    		grpCounter++;
		    		dataCounter++;
		    		
		    		if(grpCounter % numOfGrouping == 0 || 
		    				dataCounter == clist.size()){
		    			for(int i=0;i<pair.getInput().size();i++){
			    			FuzzyNode node = (FuzzyNode) inNode.getData(i);
			    			
			    			if(grpCounter < 2) {
				    			node.min  = node.mean - 1;
				    			node.max  = node.mean + 1;
			    			}else if (grpCounter < numOfGrouping){
				    			node.min  -= 0.1;
				    			node.mean /= grpCounter; 
				    			node.max  += 0.1;
			    			} else {
			    				node.mean /= grpCounter;
			    			}
			    		}		    	
		    			
		    			//add to set
		    			fuzzyset.add(inNode, pair.getIdeal().clone());
		    			
		    			//reset counter
		    			grpCounter = 0;
		    		}
				}
			}
			
			else {
				if(numOfRecordInClass >= 2){
					NodeNeuralData inNode = null;
	    			//create fuzzy data
	    			inNode = new NodeNeuralData(pair.getInput().size());
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
		    		NodeNeuralData inNode = new NodeNeuralData(pair.getInput().size());
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
	public void iteration(){
		preIteration();

		// Reset weight correction 
		this.errorCalculation.reset();
		
		//reset MSA
		this.getMsa().reset();
		
		//keep previous weight
		this.preservedWeights();
		
		int True = 0, N = 0;
		for (final NodeNeuralDataPair pair : getFuzzyTraining()) {
			
			int win 	= this.train((FnlvqFuzzy)this.network, pair);
			int target 	= DataSetUtils.getTarget(pair.getIdeal());
			
			if(win != -1 && win == target) True++;
			N++;
			postProcess(this.network.getOutput(), pair.getIdeal());
		}

		//update error
//		setError(this.errorCalculation.calculate());
		setError(1 - ((double) True/N));
		
		//calculate MSA and adjust the weights automatically
		this.getMsa().calculateSimilarities();
		
		//monotonically decreasing
		this.updateLearningRate();
		
		//update eta and kappa if in modifiedByVariable Mode
		if(this.isModifiedByVariable()){
			this.updateEtaKappa();
		}
		
		postIteration();		
	}
	

	public NodeNeuralDataSet getFuzzyTraining(){
		return this.fuzzyset;
	}
	
	protected int train(FnlvqFuzzy network, NodeNeuralDataPair pair){
		NodeNeuralData input = pair.getInput();
		NeuralData ideal = pair.getIdeal();
		
		//find winner
		int win = network.winner(input);
		
		//find ideal 
		int target = DataSetUtils.getTarget(ideal);
		
		this.adjustWeights(network.getWeights(),
				network.getMembershipDegrees(), input, win, target);	
		
		return win;
	}
	
	protected void adjustWeights(WeightMatrix weights, 
			Matrix mius, NodeNeuralData input, int win, int target) {
		
		//unknown class (-1)
		if (win == -1){
			System.out.println("masuk");
			for(int cluster = 0; cluster < weights.getCols(); cluster++){
				for(int i = 0; i < weights.getRows(); i++){
					FuzzyNode fn = (FuzzyNode) weights.get(i, cluster);
					//this.delta = 1.1d;
					
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
				FuzzyNode fn 	= (FuzzyNode) weights.get(r, win);
				double miu 	 	= mius.get(r, win);
				FuzzyNode data 	= (FuzzyNode) input.getData(r);

                final double lRange = fn.mean - fn.min;
                final double rRange = fn.max - fn.mean;
                fn.mean = fn.mean + this.alpha * (1 - miu) * (data.mean - fn.mean);
                fn.min  = fn.mean - lRange;
                fn.max  = fn.mean + rRange;
                
				//increase fuzziness (extend fuzziness)
				//adjust by variable
				if(this.isModifiedByVariable()){
					fn.min = fn.min - (1 - miu) * (1 + this.eta) * lRange;
					fn.max = fn.max + (1 - miu) * (1 + this.eta) * rRange;
				}
				
				//adjust by constant
				else {
					fn.min = fn.min - (this.beta) * lRange;
					fn.max = fn.max + (this.beta) * rRange;
				}		
				
				Assert.assertTrue("win==ideal, " + r + "," + win ,fn.max > fn.min);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			for (int r=0; r<weights.getRows();r++){
				FuzzyNode fn 	= (FuzzyNode) weights.get(r, win);
				double miu 	 	= mius.get(r, win);
				FuzzyNode data 	= (FuzzyNode) input.getData(r);

                final double lRange = fn.mean - fn.min;
                final double rRange = fn.max - fn.mean;
                fn.mean = fn.mean - this.alpha * (1 - miu) * (data.mean - fn.mean);
                fn.min  = fn.mean - lRange;
                fn.max  = fn.mean + rRange;
//                utils.log(r + fn.toString() + ", " + miu);
				//decrease fuzziness (narrow fuzziness)
				//adjust by variable
				if(this.isModifiedByVariable()){
					fn.min = fn.min + (1 - miu) * (1 - this.kappa) * lRange;
					fn.max = fn.max - (1 - miu) * (1 - this.kappa) * rRange;
				}
				
				//adjust by constant
				else {
					fn.min = fn.min + (this.gamma) * lRange;
					fn.max = fn.max - (this.gamma) * rRange;				
				}						
				
//				utils.log(r + fn.toString() + ", " + miu + ", " + this.gamma);
				Assert.assertTrue("win!=ideal, " + r + "," + win ,fn.max > fn.min);
			}
		}
	}	
	
	@Override
	protected void train(FnlvqCrisp network, NeuralDataPair pair){
		throw new RuntimeException("NeuralDataPair Parameter Not Supported.");
	}
		
	@Override
	protected void adjustWeights(WeightMatrix weights, 
			Matrix mius, NeuralData input, int win, int target){
		throw new RuntimeException("NeuralData Parameter Not Supported.");
	}

}
