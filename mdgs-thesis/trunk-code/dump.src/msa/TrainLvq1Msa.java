/**
 * 
 */
package id.mdgs.lvq.msa;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.lvq.TrainLvq1;
import id.mdgs.neural.data.FuzzyNode;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq1Msa extends TrainLvq1 {

	protected Dataset oldCodebook;
	protected Msa msa;
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 */
	public TrainLvq1Msa(Lvq network, Dataset training, double learningRate) {
		super(network, training, learningRate);
		
		validateNetwork(network);
		this.oldCodebook = new Dataset(network.codebook);
		this.msa = new Msa(this);
	}
	
	public void validateNetwork(Lvq network){
		HitList hl = new HitList();
		for(Entry code: network.codebook){
			hl.addHit(code.label);
		}
		
		for(HitEntry he: hl){
			if(he.freq != 1)
				throw new RuntimeException("Number of code must equal to 1 for each class");
		}
	}
	
	/* (non-Javadoc)
	 * @see id.mdgs.lvq.TrainLvq1#iterationOption1()
	 */
	@Override
	public void iteration() {
		double avgErr = 0;
		int N = 0;
		
		/*keep old codebook*/
		this.oldCodebook.set(this.network.codebook);
		
		this.msa.reset();
		
		for(Entry e: getTraining()){
			WinnerInfo wi;
			wi = train(this.network.codebook, e);
			
			if(wi != null){
				wi.coef = (double) 1 / (1 + wi.coef);
				this.msa.updateSimilarities(e.label, wi.winner.label, wi.coef);
			}
			
			avgErr += wi.coef;
			N++;
		}

		this.msa.calculateSimilarities();
		
		setError(avgErr/N);
		
		updateLearningRate();
	}
	
	
	/* (non-Javadoc)
	 * @see id.mdgs.lvq.TrainLvq1#train(id.mdgs.lvq.Dataset, id.mdgs.lvq.Dataset.Entry)
	 */
//	@Override
//	protected WinnerInfo train(Dataset codebook, Entry input) {
//		WinnerInfo wi;
//		
//		wi = this.findWinner.function(this.network.codebook, input);
//		
//		this.adjustWeights(wi.winner, input, this.alpha);
//		
//		return wi;
//	}
	
	@Override
	public void reloadPreviousCode(int label){
		/*label is per row*/
		Entry currentCode 	= this.network.codebook.findEntry(label);
		Entry oldCode		= this.oldCodebook.findEntry(label);
		if(currentCode == null)
			throw new RuntimeException("currentCode with label:" +label+ " not found");
		if(oldCode == null)
			throw new RuntimeException("oldCode with label:" +label+ " not found");
		
		currentCode.copy(oldCode);
	}

}
