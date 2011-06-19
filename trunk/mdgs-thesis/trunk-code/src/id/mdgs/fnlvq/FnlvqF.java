/**
 * 
 */
package id.mdgs.fnlvq;

import java.util.Iterator;

import id.mdgs.fnlvq.FCodeBook.FEntry;
import id.mdgs.fnlvq.FCodeBook.FWinnerInfo;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FnlvqF extends Fnlvq {

	/**
	 * 
	 */
	public FnlvqF() {
	}

	public int classify(FEntry sample){
		FWinnerInfo wi;
		
		wi = this.findWinner(codebook, sample);
		
		if(MathUtils.equals(wi.coef, 0))
			return -1;
		else
			return wi.winner.label;
	}
	
	public FWinnerInfo findWinner(FCodeBook codebook, FEntry input){
		FWinnerInfo[] wis;
		
		wis = findWinner(codebook, input, 1);
		
		return wis[0];
	}
	
	public FWinnerInfo[] findWinner(FCodeBook codebook, FEntry input, int num){
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = Double.NEGATIVE_INFINITY;
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
			
			score = MathUtils.min(sim.data);
			
			/*sort desc*/
			for(i=0; (i < winner.length) && (score < winner[i].coef); i++);
			
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
	
	
	
	@Override
	public int classify(Entry sample){
		return 0;
	}
	
	@Override
	public FWinnerInfo findWinner(FCodeBook codebook, Entry input) {
		return null;
	}

	@Override
	public FWinnerInfo[] findWinner(FCodeBook codebook, Entry input, int num) {
		return null;
	}

}
