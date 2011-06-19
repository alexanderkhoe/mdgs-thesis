/**
 * 
 */
package id.mdgs.fglvq;

import java.util.Iterator;

import id.mdgs.fnlvq.FCodeBook;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.fnlvq.FCodeBook.FEntry;
import id.mdgs.fnlvq.FCodeBook.FWinnerInfo;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fglvq extends Fnlvq {

	/**
	 * 
	 */
	public Fglvq() {
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
			//sort ascending
			winner[i].coef = Double.MAX_VALUE;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		while(eIt.hasNext()) {
			double difference = 0;
			FEntry code = eIt.next();
			Entry sim = miu.findEntry(code.label);
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(input.data[j]);
			}
			
			difference = 1 - MathUtils.min(sim.data);
			//difference = MathUtils.squaredEuclideDistance(input.data, code.data);
			//difference = MathUtils.euclideDistance(input.data, code.data);
			
			for(i=0; (i < winner.length) && (difference > winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = difference;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}

}
