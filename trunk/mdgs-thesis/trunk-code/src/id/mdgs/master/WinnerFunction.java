/**
 * 
 */
package id.mdgs.master;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.lvq.LvqUtils.WinnerInfo;

/**
 * @author I Made Agus Setiawan
 *
 */
public interface WinnerFunction {
	public WinnerInfo function(Dataset codes, Entry sample);
	public WinnerInfo[] function(Dataset codes, Entry sample, int knn); 

}
