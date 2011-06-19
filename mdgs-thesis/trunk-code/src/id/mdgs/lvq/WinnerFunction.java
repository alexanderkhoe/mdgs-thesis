/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;

/**
 * @author I Made Agus Setiawan
 *
 */
public interface WinnerFunction {
	public WinnerInfo function(Dataset codes, Entry sample);
	public WinnerInfo[] function(Dataset codes, Entry sample, int knn); 
}
