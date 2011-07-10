/**
 * 
 */
package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fpglvq extends Fnlvq {
	
	public double unknownThreshold;

	/**
	 * 
	 */
	public Fpglvq() {
		findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MEAN);
		this.unknownThreshold = 0;
	}

	@Override
	public int classify(Entry sample) {
		FWinnerInfo wi;
		
		wi = this.findWinner.function(codebook, sample);
		
		if(wi.coef < this.unknownThreshold)
			return -1;
		else
			return wi.winner.label;
	}
	
	public void calcThreshold(FoldedDataset<Dataset, Entry> tset){
        //init threshold
        this.unknownThreshold = Double.MAX_VALUE;

        for(Entry sample: tset){
        	FWinnerInfo wi;
        	wi = this.findWinner.function(codebook, sample);
        	
        	if(wi.coef < this.unknownThreshold){
        		this.unknownThreshold = wi.coef;
        	}
        }
	}
}
