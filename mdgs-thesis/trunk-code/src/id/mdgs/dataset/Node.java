/**
 * 
 */
package id.mdgs.dataset;

/**
 * @author I Made Agus Setiawan
 *
 */
public interface Node extends Cloneable {

	public void reset();
	
	public void randomize(double min, double max);
	
	public void set(double[] val);
	
	public Node clone();
	
	public void copy(Node node);
	
	public double getMaxIntersection(double val);
	
	public double getMaxIntersection(Node fn);
	
	public String toString();
}
