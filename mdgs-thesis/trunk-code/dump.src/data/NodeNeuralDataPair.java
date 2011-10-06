/**
 * 
 */
package id.mdgs.neural.data;

import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;

/**
 * @author I Made Agus Setiawan
 *
 */
public class NodeNeuralDataPair {

	private final NodeNeuralData input;
	private final NeuralData ideal;
	
	public NodeNeuralDataPair(final NodeNeuralData input) {
		this.input = input;
		this.ideal = null;
	}

	/**
	 * Construct a BasicNeuralDataPair class with the specified input and ideal
	 * values.
	 *
	 * @param input
	 *            The input to the neural network.
	 * @param ideal
	 *            The expected results from the neural network.
	 */
	public NodeNeuralDataPair(final NodeNeuralData input, final NeuralData ideal) {
		this.input = input;
		this.ideal = ideal;
	}	

	public double[] getIdealArray() {
		if( this.ideal==null )
			return null;		
		return this.ideal.getData();
	}

	public Node[] getInputArray() {
		return this.input.getData();
	}

	public void setIdealArray(double[] data) {
		this.ideal.setData(data);
	}

	public void setInputArray(Node[] data) {
		this.input.setData(data);
	}

	public NeuralData getIdeal() {
		return this.ideal;
	}

	public NodeNeuralData getInput() {
		return this.input;
	}
	
	public static NodeNeuralDataPair createPair(final int inputSize, final int idealSize) {
		NodeNeuralDataPair result;

		if (idealSize > 0) {
			result = new NodeNeuralDataPair(new NodeNeuralData(inputSize),
					new BasicNeuralData(idealSize));
		} else {
			result = new NodeNeuralDataPair(new NodeNeuralData(inputSize));
		}

		return result;
	}	
}
