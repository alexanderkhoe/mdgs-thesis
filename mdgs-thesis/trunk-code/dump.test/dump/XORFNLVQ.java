package dump;


import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQ;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQTrain;
import id.mdgs.utils.DataSetUtils;

import java.util.List;

import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class XORFNLVQ {
	public static double XOR_INPUT[][] = { { 0.0, 0.0 }, { 1.0, 0.0 },
		{ 0.0, 1.0 }, { 1.0, 1.0 } };

	public static double XOR_IDEAL[][] = { { 1, 0 }, { 0, 1 }, { 0, 1 }, { 1, 0 } };

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test1(){
		NeuralDataSet dataset = new BasicNeuralDataSet(XOR_INPUT, XOR_IDEAL);		
	
		//create network
		FNLVQ network = new FNLVQ(2, 2);
		network.getWeights().set(new FuzzyNode(0, 0.5, 1));
		
		FNLVQTrain train = 
			new FNLVQTrain(network, dataset, 0.05);
				
		int iteration = 0;
//		System.out.print(network.getWeights().toString());
		
		for(iteration = 0;iteration<=100;iteration++)
		{
			train.iteration();
			System.out.println("Iteration: " + iteration + ", Error:" + train.getError());
//			System.out.print(network.getWeights().toString());
		}
				
		System.out.print(network.getWeights().toString());
		
		NeuralDataPair pair = BasicNeuralDataPair.createPair(4, 1);
		for(int i=0;i<4;i++){
			dataset.getRecord(i, pair);
			System.out.println("Pattern " + i + " winner: " + 
					network.winner(pair.getInput()) +
					" , Ideal: " + DataSetUtils.getTarget(pair.getIdeal()));
		}		
	}
}
