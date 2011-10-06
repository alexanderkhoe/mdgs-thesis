package id.mdgs.neural.fnlvq.crisp;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.lvq.Lvq;

import java.util.List;

import org.encog.engine.util.EngineArray;
import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;

/**
 * @author I Made Agus Setiawan
 * @version 1.0
 * @created 27-Apr-2011 12:36:28 PM
 */
public class FnlvqCrisp extends Lvq {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1482659638935566184L;
	
	/**
	 * Every column on this matrix represent cluster 
	 * with each row for each input node
	 */
	protected WeightMatrix weights;
	protected NeuralData output;
	/**
	 * 
	 */
	protected Matrix miu;
	
	public FnlvqCrisp(int inCount, int outCount){
		setInputCount(inCount);
		setOutputCount(outCount);
		this.weights = new WeightMatrix(inCount, outCount);
		this.miu = new Matrix(inCount, outCount);
		this.output = new BasicNeuralData(outCount);
	}
	
	@Override
	public void reset(){
		this.weights.randomize(0D, 2D);
	}
	
	public void setWeights(final WeightMatrix wm){
		assert(this.weights.getCols() == wm.getCols() &&
			   this.weights.getRows() == wm.getRows());
		
		this.weights.set(wm);
	}


	public int findWinner(NeuralData result){
		int win = EngineArray.maxIndex(result.getData());
		if (result.getData(win) == 0){
			//class not recognized
			return -1;
		}
		return win;
	}
	/**
	 * 
	 * @param input
	 */
	@Override
	public int winner(NeuralData input){
		NeuralData result = this.compute(input);
		return findWinner(result);		
	}
	
	/**
	 * 
	 * @param input
	 */
	@Override
	public int classify(NeuralData input){
		return winner(input);
	}
	
	/**
	 * 
	 * @param input
	 */
	@Override
	public NeuralData compute(NeuralData input){
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

	public double transferFunction(double[] clusterData){
		//find minimum for each cluster, save as output			
		final double clusterOutput = EngineArray.min(clusterData);
		return clusterOutput;
	}
	
	public NeuralData getOutput(){
		return this.output;
	}
	
	public WeightMatrix getWeights(){
		return this.weights;
	}
	
	public Matrix getMembershipDegrees(){
		return this.miu;
	}
	
	@Override
	public void updateProperties() {
		// not needed
	}

}