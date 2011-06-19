/**
 * 
 */
package id.mdgs.neural.data;

import java.io.Serializable;

/**
 * @author I Made Agus Setiawan
 *
 */
public class NodeNeuralData implements  Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2761868782555260418L;
	private Node[] data;
	
	public NodeNeuralData(final Node[] d) {
		this(d.length);
		this.setData(d);
	}

	public NodeNeuralData(final int size) {
		this.data = new Node[size];
	}

	public void clear() {
		for(int i=0;i<size();i++){
			this.data[i].reset();
		}
	}

	public Node getData(int index) {
		return this.data[index];
	}

	public Node[] getData(){
		return this.data;
	}
	
	public void setData(Node[] data) {
		for(int i=0;i<data.length;i++){
			this.data[i] = data[i];
		}
	}

	public void setData(int index, Node d) {
		this.data[index] = d;
	}

	public int size() {
		return this.data.length;
	}
	
	public NodeNeuralData clone(){
		NodeNeuralData out = new NodeNeuralData(this.size());
		for(int i=0;i<out.size();i++){
			out.getData()[i] = this.data[i].clone();
		}		
		
		return out;
	}
}
