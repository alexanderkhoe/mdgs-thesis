/**
 * 
 */
package id.mdgs.dump;

import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq1Cosine extends TrainLvq1 {

	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 */
	public TrainLvq1Cosine(Lvq network, Dataset training, double learningRate) {
		super(network, training, learningRate);
	}

	/* (non-Javadoc)
	 * @see id.mdgs.lvq.TrainLvq1#train(id.mdgs.lvq.Dataset, id.mdgs.lvq.Dataset.Entry)
	 */
	@Override
	protected WinnerInfo train(Dataset codebook, Entry input) {
		WinnerInfo[] wi;
		
		wi = LvqUtils.findWinnerByCosineSimilarity(codebook, input, 1);
		if(wi == null)	return null;
		
		this.adjustWeights(wi[0].winner, input, this.alpha);
		
		return wi[0];
	}

	/* (non-Javadoc)
	 * @see id.mdgs.lvq.TrainLvq1#iterationOption1()
	 */
	@Override
	public void iterationOption1() {
		double sims = 0;
		
		for(Entry e: getTraining()){
			WinnerInfo wi;
			wi = train(this.network.codebook, e);
			
			sims += 1 - wi.coef;
		}

		setError(sims/getTraining().numEntries);
		
		updateLearningRate();
	}

}
