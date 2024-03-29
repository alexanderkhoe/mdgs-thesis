/**
 * 
 */
package id.mdgs.fnlvqold;

import java.util.Iterator;

import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.FCodeBook.FWinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FglvqF extends FnlvqF {

	/**
	 * 
	 */
	public FglvqF() {
	}

	@Override
	public FWinnerInfo[] findWinner(FCodeBook codebook, FEntry input, int num) {
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = 1;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		Iterator<Entry>  mIt = miu.iterator();
		while(eIt.hasNext() && mIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			Entry sim = mIt.next();
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(input.data[j]);
			}
			
			//transfor to disimilarity
			score = 1 - MathUtils.min(sim.data);
			
			/*sort desc*/
			for(i=0; (i < winner.length) && (score > winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
				winner[i].miu	 = sim;
			}
		}
		
		return winner;
	}

}
