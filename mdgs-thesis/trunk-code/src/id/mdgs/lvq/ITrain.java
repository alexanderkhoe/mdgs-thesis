/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.lvq.Dataset.Entry;

/**
 * @author I Made Agus Setiawan
 *
 */
public interface ITrain {
	public void updateLearningRate();
	public void setMaxEpoch(int maxEpoch);
	public int getCurrEpoch();
	public void setError(double error);
	public double getError();
	public void reloadPreviousCode(int label);
	public void iteration();
	public boolean shouldStop();
	public int getNumberOfClass();
}
