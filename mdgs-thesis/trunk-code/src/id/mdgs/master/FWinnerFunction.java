package id.mdgs.master;

import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public interface FWinnerFunction {
	public FWinnerInfo function(FCodeBook codebook, Entry sample);
	public FWinnerInfo[] function(FCodeBook codebook, Entry sample, int num); 
}
