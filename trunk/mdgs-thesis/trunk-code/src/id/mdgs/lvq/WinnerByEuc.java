/**
 * 
 */
package id.mdgs.lvq;

import java.util.Iterator;

import org.encog.engine.util.BoundMath;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class WinnerByEuc implements WinnerFunction {

	/**
	 * 
	 */
	public WinnerByEuc() {
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
			double difference = 0;
			Entry code = eIt.next();
			
			for(i=0;i < codes.numFeatures; i++){
				double diff = sample.data[i] - code.data[i];
				difference += diff * diff;
				
//				if(difference > winner[knn-1].coef)
//					break;
			}
			
			difference = Math.sqrt(difference);
//			difference = BoundMath.sqrt(difference);
			
			if(!once) {
				once = true;
				winner[0].coef 	 = difference;
				winner[0].winner = code;
				continue;
			}
			
			for(i=0; (i < knn) && (difference > winner[i].coef); i++);
			
			if(i < knn){
				for(int j=knn - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = difference;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}

}
