/**
 * 
 */
package id.mdgs.neural.lvq;

import org.encog.engine.util.BoundMath;
import org.encog.engine.util.EngineArray;
import org.encog.mathutil.matrices.Matrix;
import org.encog.mathutil.matrices.MatrixMath;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Lvq1 extends Lvq {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2087606708071910569L;

	private Matrix weights;	
	
	/**
	 * 
	 */
	public Lvq1() {
	}

	/**
	 * @param inCount
	 * @param outCount
	 */
	public Lvq1(int inCount, int outCount) {
		this.inputNeuronCount = inCount;
		this.outputNeuronCount= outCount;
		this.weights = new Matrix(inCount, outCount);
	}


	/**
	 * 
	 * @param input
	 */
	public int classify(NeuralData input){
		return winner(input);
	}

	/**
	 * 
	 * @param input
	 */
	public NeuralData compute(NeuralData input){
		NeuralData result = new BasicNeuralData(this.outputNeuronCount);
		
		for (int i = 0; i < this.outputNeuronCount; i++) {
			double out = 0;
			for (int j = 0; j < input.size(); j++) {
				double diff = input.getData(j)
						- this.weights.get(j, i);
				out += diff * diff;
			}
			result.setData(i, BoundMath.sqrt(out));
		}
		
		return result;
	}

	public int getInputCount(){
		return this.inputNeuronCount;
	}

	public int getOutputCount(){
		return this.outputNeuronCount;
	}

	/**
	 * 
	 * @param newInputCount
	 */
	public void setInputCount(int newInputCount){
		this.inputNeuronCount = newInputCount;
	}

	/**
	 * 
	 * @param newOutputCount
	 */
	public void setOutputCount(int newOutputCount){
		this.outputNeuronCount = newOutputCount;
	}
	
	
	public void reset(){
		this.weights.randomize(-1, 1);
	}

	/**
	 * 
	 * @param seed
	 */
	public void reset(int seed){
		reset();
	}

	/**
	 * 
	 * @param input
	 */
	public int winner(NeuralData input){
		NeuralData output = compute(input);
		double lowest = Double.MAX_VALUE;
		int win = -1;
		for(int i=0;i<output.getData().length;i++){
			if(lowest > output.getData()[i]){
				lowest = output.getData()[i];
				win = i;
			}
		}
		
		return win;
	}

	public Matrix getWeights() {
		return this.weights;
	}

	public void setWeights(Matrix newWeights){
		this.weights.set(newWeights);
	}

	@Override
	public void updateProperties() {
	}	
}
