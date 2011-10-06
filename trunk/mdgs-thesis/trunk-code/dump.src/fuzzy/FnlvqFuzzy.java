/**
 * 
 */
package id.mdgs.neural.fnlvq.fuzzy;

import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;

import id.mdgs.neural.data.NodeNeuralData;
import id.mdgs.neural.fnlvq.crisp.FnlvqCrisp;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FnlvqFuzzy extends FnlvqCrisp {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7260729280058692897L;

	/**
	 * @param inCount
	 * @param outCount
	 */
	public FnlvqFuzzy(int inCount, int outCount) {
		super(inCount, outCount);
	}

	public int winner(NodeNeuralData input){
		NeuralData result = this.compute(input);
		return findWinner(result);		
	}
	
	public int classify(NodeNeuralData input){
		return winner(input);
	}
	
	public NeuralData compute(NodeNeuralData input){
		NeuralData result = new BasicNeuralData(this.outputNeuronCount);
		
		double[] cluster = new double[this.inputNeuronCount];
		miu.clear();
				
		for (int c = 0; c < this.outputNeuronCount; c++) {			
			//find membership degree
			for(int r = 0; r < this.inputNeuronCount; r++){
				double value = this.weights.get(r, c).getMaxIntersection(input.getData(r));
				miu.set(r, c, value);
				cluster[r] = value;
			}
			result.setData(c, this.transferFunction(cluster));
		}
		
		this.output.setData(result.getData());
		return result;
	}
	
	//using Mean
	@Override
	public double transferFunction(double[] clusterData){
		//find mean for each cluster, save as output			
		double clusterOutput = 0;
		for(int c= 0;c < clusterData.length;c++)
			clusterOutput += clusterData[c];
		
		return clusterOutput/clusterData.length;
	}
	
	@Override
	public int winner(NeuralData input){
		throw new RuntimeException("NeuralData Parameter Not Supported.");
	}
	
	@Override
	public int classify(NeuralData input){
		throw new RuntimeException("NeuralData Parameter Not Supported.");
	}
	
	@Override
	public NeuralData compute(NeuralData input){
		throw new RuntimeException("NeuralData Parameter Not Supported.");
	}	
}
