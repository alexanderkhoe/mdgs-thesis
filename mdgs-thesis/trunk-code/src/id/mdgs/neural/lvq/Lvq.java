package id.mdgs.neural.lvq;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.engine.util.EngineArray;
import org.encog.mathutil.matrices.Matrix;
import org.encog.mathutil.matrices.MatrixMath;
import org.encog.ml.BasicML;
import org.encog.ml.MLClassification;
import org.encog.ml.MLResettable;
import org.encog.ml.MLError;

/**
 * @author madeagus
 * @version 1.0
 * @created 27-Apr-2011 12:36:25 PM
 */
public abstract class Lvq extends BasicML implements MLClassification, MLResettable, MLError {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4221911165485355644L;

	protected int inputNeuronCount;
	protected int outputNeuronCount;


	
	public Lvq(){}
	

	/**
	 * 
	 * @param data
	 */
	public double calculateError(final NeuralDataSet data){
		return 0;
	}

	/**
	 * 
	 * @param input
	 */
	public int classify(NeuralData input){
		return 0;
	}

	/**
	 * 
	 * @param input
	 */
	public NeuralData compute(NeuralData input){
		return null;
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
	}

	/**
	 * 
	 * @param seed
	 */
	public void reset(int seed){
	}

	/**
	 * 
	 * @param input
	 */
	public int winner(NeuralData input){
		return 0;
	}

}