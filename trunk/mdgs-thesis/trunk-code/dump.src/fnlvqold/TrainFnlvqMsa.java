/**
 * 
 */
package id.mdgs.fnlvqold;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.FCodeBook.FWinnerInfo;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvqMsa extends TrainFnlvq {

	protected FCodeBook oldCodebook;
	protected Msa msa;
	
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 */
	public TrainFnlvqMsa(Fnlvq network, Dataset training, double learningRate) {
		super(network, training, learningRate);
		
		validateNetwork(network);
		this.oldCodebook = new FCodeBook(network.codebook);
		this.msa = new Msa(this);		
	}
	
	public void validateNetwork(Fnlvq network){
		HitList hl = new HitList();
		for(FEntry code: network.codebook){
			hl.addHit(code.label);
		}
		
		for(HitEntry he: hl){
			if(he.freq != 1)
				throw new RuntimeException("Number of code must equal to 1 for each class");
		}
	}

	@Override
	protected void iteration3() {
		
		/*keep old codebook*/
		this.oldCodebook.set(this.network.codebook);
		
		this.msa.reset();
		
		//find max
		int maxData = 0;
		for(PEntry pe: profiler){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		//utils.timer.start();
		Entry sample = null;
		int N = 0; int True = 0;
		FWinnerInfo wi;
		for(int i=0;i<maxData;i++){
			for(PEntry pe: profiler){
				if(i >= pe.size()) continue;
				
				int pos = pe.get(i % pe.size());
				sample = training.get(pos);
				
				wi = train(this.network.codebook, sample);
				
				this.msa.updateSimilarities(sample.label, wi.winner.label, wi.coef);
				
				N++;
				
				if(!MathUtils.equals(wi.coef, 0) && wi.winner.label == sample.label) True++;
			}
		}
		
		this.msa.calculateSimilarities();
		//System.out.println("E: " + utils.timer.stop());
		//System.out.print("Benar: " + Benar);
		updateLearningRate();
		//setError(avgLost/N);
		setError(1 - ((double)True/N));
		
		this.currEpoch++;
	}

	@Override
	public void reloadPreviousCode(int label) {
		/*label is per row*/
		FEntry currentCode 	= this.network.codebook.findEntry(label); 
		FEntry oldCode		= this.oldCodebook.findEntry(label);
		if(currentCode == null)
			throw new RuntimeException("currentCode with label:" +label+ " not found");
		if(oldCode == null)
			throw new RuntimeException("oldCode with label:" +label+ " not found");
		
		currentCode.copy(oldCode);
	}

	@Override
	protected FWinnerInfo train(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wins;
		
		wins = this.network.findWinner(codebook, input, codebook.size());
		
		/*adjust code vector*/
		adjustWeights(codebook, wins[0].winner, input, this.alpha, 
				MathUtils.equals(wins[0].coef, 0));
		
		return wins[0];
	}

	protected FWinnerInfo[] findWinner(FCodeBook codebook, Entry sample, int num){
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = 0;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		Iterator<Entry>  mIt = this.network.miu.iterator();
		
		double bobot[] mius
		while(eIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			Entry sim = mIt.next();
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(sample.data[j]);
			}
			
			score = MathUtils.min(sim.data);
			
			/*sort desc*/
			for(i=0; (i < winner.length) && (score < winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
			}
		}
		
		return winner;		
	}
	
	@Override
	protected void adjustWeights(FCodeBook codebook, FEntry code, Entry input,
			double alpha, boolean unknown) {
		super.adjustWeights(codebook, code, input, alpha, unknown);
	}

}
