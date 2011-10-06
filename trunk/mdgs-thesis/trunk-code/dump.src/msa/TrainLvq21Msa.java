/**
 * 
 */
package id.mdgs.lvq.msa;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.TrainLvq21;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq21Msa extends TrainLvq21 {

	public Dataset oldCodebook;
	public Msa msa;
	
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 * @param windowWidth
	 */
	public TrainLvq21Msa(Lvq network, Dataset training, double learningRate,
			double windowWidth) {
		super(network, training, learningRate, windowWidth);
		
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
	public void iterationOption1() {
		double avgErr = 0;
		int N = 0;
		
		/*keep old codebook*/
		this.oldCodebook.set(this.network.codebook);
		
		this.msa.reset();
		
		for(Entry e: getTraining()){
			WinnerInfo wi;
			wi = train(this.network.codebook, e);
			
			if(wi != null){
//				wi.coef = Math.sqrt(wi.coef);
				wi.coef = (double) 1 / (1 + wi.coef);
				this.msa.updateSimilarities(e.label, wi.winner.label, wi.coef);
			}
			
			avgErr += wi.coef;
			N++;
		}

		this.msa.calculateSimilarities();
		
		setError(avgErr/N);
		updateLearningRate();
		currEpoch++;
	}
	
	public void iterationOption3(){
		if(profiler.size() <= 0){
			prepareOption3();
		}

		/*keep old codebook*/
		this.oldCodebook.set(this.network.codebook);

		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		
		Entry sample = null;
		double avgErr = 0;
		int N = 0;
		
		this.msa.reset();

		for(int i=0;i<maxData;i++){
			index = i;
			for(PEntry pe: profiler){
				if(index >= pe.size()) continue;
				
				int pos = pe.get(index % pe.size());
				sample = getTraining().get(pos);
				
				WinnerInfo wi;
				wi = train(this.network.codebook, sample);
				
				
				if(wi != null){
					wi.coef = (double) 1 / (1 + wi.coef);
					this.msa.updateSimilarities(sample.label, wi.winner.label, wi.coef);
				}
				
				avgErr += wi.coef;
				N++;
			}
		}

		this.msa.calculateSimilarities();
		setError(avgErr/N);
		updateLearningRate();
		currEpoch++;
	}
	
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
//		utils.log(String.format("Tukar %d",label));
	}
}
