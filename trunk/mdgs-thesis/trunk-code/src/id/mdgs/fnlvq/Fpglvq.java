/**
 * 
 */
package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fpglvq extends Fnlvq {

	/**
	 * 
	 */
	public Fpglvq() {
		findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MEAN);
	}

//	@Override
//	public int classify(Entry sample) {
//		FWinnerInfo wi;
//		
//		wi = this.findWinner.function(codebook, sample);
//		
//		if(MathUtils.equals(wi.coef, 0))
//			return -1;
//		else
//			return wi.winner.label;
//	}

}
