/**
 * 
 */
package id.mdgs.glvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainGlvqMsa extends TrainGlvq {
	protected Dataset oldCodebook;
	public Msa msa;
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 */
	public TrainGlvqMsa(Lvq network, Dataset training, double learningRate) {
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
	
	
	
	@Override
	public void reloadPreviousCode(int label) {
		/*label is per row*/
		Entry currentCode 	= this.network.codebook.findEntry(label);
		Entry oldCode		= this.oldCodebook.findEntry(label);
		if(currentCode == null)
			throw new RuntimeException("currentCode with label:" +label+ " not found");
		if(oldCode == null)
			throw new RuntimeException("oldCode with label:" +label+ " not found");
		
		currentCode.copy(oldCode);
	}

	
	@Override
	public void iterationOption3() {
		//save old codebook
		this.oldCodebook.set(this.network.codebook);
		this.msa.reset();
		
		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		
		Entry sample = null;
		double avgLost = 0;
		int N = 0; int True = 0;
		for(int i=0;i<maxData;i++){
			for(PEntry pe: profiler){
				if(i >= pe.size()) continue;
				
				int pos = pe.get(i % pe.size());
				sample = training.get(pos);
				
				WinnerInfo win;
				win = train(this.network.codebook, sample);
				
				double mce = win.coef;
				avgLost += lostFunction(mce);
				N++;
				if(mce < 0) True++;

				this.msa.updateSimilarities(sample.label, win.winner.label, MathUtils.norm(-1, 1, mce));
			}
		}
		
		this.msa.calculateSimilarities();
		
		updateLearningRate();
		updateXiParameter();		
		setError(avgLost/N);
//		setError(1 - ((double)True/N));
		
		this.currEpoch++;
		bestCodebook.evaluate(network.codebook, getError(), currEpoch);
	}

	
}
