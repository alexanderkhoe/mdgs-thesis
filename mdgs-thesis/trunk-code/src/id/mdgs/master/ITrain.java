/**
 * 
 */
package id.mdgs.master;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.Dataset.Entry;

/**
 * @author I Made Agus Setiawan
 *
 */
public interface ITrain {
	public void updateLearningRate();
	public void setMaxEpoch(int maxEpoch);
	public int getMaxEpoch();
	public int getCurrEpoch();
	public void setError(double error);
	public double getError();
	public void reloadPreviousCode(int label);
	public void iteration();
	public boolean shouldStop();
	public int getNumberOfClass();
	public void setNetwork(IClassify<?, ?> net);
	public FoldedDataset<?, ?> getTraining();
	public void reset();
	public void setTraining(FoldedDataset<?, ?> foldedDs);
	public String information();
}
