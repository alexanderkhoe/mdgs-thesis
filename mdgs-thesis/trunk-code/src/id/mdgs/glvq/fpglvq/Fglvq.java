/**
 * 
 */
package id.mdgs.glvq.fpglvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.FCodeBook.FWinnerInfo;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.glvq.mglvq.MGlvq.MParam;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fglvq extends Fnlvq {

	double[] threshold;
	/**
	 * 
	 */
	public Fglvq() {
		//throw new RuntimeException("Class Suspended");
	}

	/* (non-Javadoc)
	 * @see id.mdgs.fnlvq.Fnlvq#findWinner(id.mdgs.fnlvq.FCodeBook, id.mdgs.lvq.Dataset.Entry, int)
	 */
	@Override
	public FWinnerInfo[] findWinner(FCodeBook codebook, Entry input, int num) {
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[codebook.size()];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = Double.NEGATIVE_INFINITY;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		Iterator<Entry>	mIt = miu.iterator();
		while(eIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			Entry sim = mIt.next();//miu.findEntry(code.label);
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(input.data[j]);
			}
			
//			score = MathUtils.min(sim.data);
			score = MathUtils.mean(sim.data);
			
			//find the largest minSim
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

	/* (non-Javadoc)
	 * @see id.mdgs.fnlvq.Fnlvq#classify(id.mdgs.lvq.Dataset.Entry)
	 */
	@Override
	public int classify(Entry sample) {
		assert(threshold != null);
		FWinnerInfo wi;
		
		wi = this.findWinner(codebook, sample);
		if(sample.label == -1)
			System.out.println(String.format("unknown: dikenal:%d, score %f", wi.winner.label, wi.coef));
			
		//if(wi.coef < threshold[wi.winner.label])
		if(MathUtils.equals(wi.coef, 0))
			return -1;
		else
			return wi.winner.label;
	}

	public void calcThreshold(Dataset tset){
		
		DatasetProfiler dp = new DatasetProfiler();
		dp.run(tset);
		
		//init threshold
		this.threshold = new double[this.codebook.size()];
		for(int i=0;i < threshold.length;i++) threshold[i] = 1;
		
		for(PEntry pe: dp){
			FEntry code = this.codebook.findEntry(pe.label);
			
			for(int i=0;i < pe.size();i++){
				Entry sample = tset.get(pe.get(i));
				double score = 0;
				for(int k=0;k < sample.size();k++){
					score += code.data[k].getMaxIntersection(sample.data[k]);
				}
				score /= sample.size();
				threshold[pe.label] = MathUtils.min(threshold[pe.label], score);
			}
		}
		
		for(int i=0;i < threshold.length;i++) System.out.println(String.format("%d. %f", i, threshold[i]));
	}
	
}
