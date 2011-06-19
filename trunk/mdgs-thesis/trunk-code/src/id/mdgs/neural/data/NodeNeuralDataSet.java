/**
 * 
 */
package id.mdgs.neural.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.basic.BasicNeuralData;

/**
 * @author I Made Agus Setiawan
 *
 */
public class NodeNeuralDataSet implements Iterable<NodeNeuralDataPair> {
	
	public class NodeNeuralIterator implements Iterator<NodeNeuralDataPair> {

		/**
		 * The index that the iterator is currently at.
		 */
		private int currentIndex = 0;

		/**
		 * Is there more data for the iterator to read?
		 * 
		 * @return Returns true if there is more data to read.
		 */
		public boolean hasNext() {
			return this.currentIndex < NodeNeuralDataSet.this.data.size();
		}

		/**
		 * Read the next item.
		 * 
		 * @return The next item.
		 */
		public NodeNeuralDataPair next() {
			if (!hasNext()) {
				return null;
			}

			return NodeNeuralDataSet.this.data.get(this.currentIndex++);
		}

		/**
		 * Removes are not supported.
		 */
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");
		}
	}
	
	private List<NodeNeuralDataPair> data = new ArrayList<NodeNeuralDataPair>();
	
	/**
	 * 
	 */
	public NodeNeuralDataSet() {
	}
	
	public NodeNeuralDataSet(final Node[][] input, final double[][] ideal) {
		if (ideal != null) {
			for (int i = 0; i < input.length; i++) {
				final NodeNeuralData inputData = new NodeNeuralData(input[i]);
				final NeuralData idealData = new BasicNeuralData(ideal[i]);
				this.add(inputData, idealData);
			}
		} else {
			for (final Node[] element : input) {
				final NodeNeuralData inputData = new NodeNeuralData(element);
				this.add(inputData);
			}
		}
	}
	
	public NodeNeuralDataSet(final List<NodeNeuralDataPair> data) {
		this.data = data;
	}
	
	public Iterator<NodeNeuralDataPair> iterator() {
		final NodeNeuralIterator result = new NodeNeuralIterator();
		return result;
	}

	public long getRecordCount() {
		return this.data.size();
	}

	public void getRecord(long index, NodeNeuralDataPair pair) {
		final NodeNeuralDataPair source = this.data.get((int) index);
		pair.setInputArray(source.getInputArray());
		if (pair.getIdealArray() != null) {
			pair.setIdealArray(source.getIdealArray());
		}
	}

	public void add(NodeNeuralData data) {
		this.data.add(new NodeNeuralDataPair(data));
	}

	public void add(NodeNeuralData inputData, NeuralData idealData) {
		this.data.add(new NodeNeuralDataPair(inputData, idealData));
	}

	public void add(NodeNeuralDataPair inputData) {
		this.data.add(inputData);
	}

	public void close() {

	}

	public int getIdealSize() {
		if (this.data.isEmpty()) {
			return 0;
		}
		final NodeNeuralDataPair first = this.data.get(0);
		if (first.getIdeal() == null) {
			return 0;
		}

		return first.getIdeal().size();
	}

	public int getInputSize() {
		if (this.data.isEmpty()) {
			return 0;
		}
		final NodeNeuralDataPair first = this.data.get(0);
		return first.getInput().size();
	}
	
	public void setData(final List<NodeNeuralDataPair> data) {
		this.data = data;
	}	

	public List<NodeNeuralDataPair> getData() {
		return this.data;
	}
	

}
