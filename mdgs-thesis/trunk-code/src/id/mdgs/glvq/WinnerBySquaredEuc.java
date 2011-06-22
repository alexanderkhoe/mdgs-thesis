/**
 * 
 */
package id.mdgs.glvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.WinnerFunction;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class WinnerBySquaredEuc implements WinnerFunction {

	/**
	 * 
	 */
	public WinnerBySquaredEuc() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see id.mdgs.lvq.WinnerFunction#function(id.mdgs.lvq.Dataset, id.mdgs.lvq.Dataset.Entry)
	 */
	@Override
	public WinnerInfo function(Dataset codes, Entry sample) {
		WinnerInfo[] result;
		
		result = this.function(codes, sample, 1);
		if(result != null){
			return result[0];
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see id.mdgs.lvq.WinnerFunction#function(id.mdgs.lvq.Dataset, id.mdgs.lvq.Dataset.Entry, int)
	 */
	@Override
	public WinnerInfo[] function(Dataset codes, Entry sample, int knn) {
		WinnerInfo[] winner;
		int i;
		
		winner = new WinnerInfo[knn];
		for(i=0;i < knn;i++){
			winner[i] = new WinnerInfo();
		}
		
		boolean once = false;
		Iterator<Entry> eIt = codes.iterator();
		while(eIt.hasNext()) { 
			double score = 0;
			Entry code = eIt.next();
			
			score = MathUtils.squaredEuclideDistance(sample.data, code.data);
			
			if(!once) {
				once = true;
				winner[0].coef 	 = score;
				winner[0].winner = code;
				continue;
			}
			
			for(i=0; (i < knn) && (score > winner[i].coef); i++);
			
			if(i < knn){
				for(int j=knn - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}

}
