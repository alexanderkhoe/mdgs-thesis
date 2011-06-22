/**
 * 
 */
package id.mdgs.fnlvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.FWinnerFunction;
import id.mdgs.master.WinnerFunction;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class WinnerByFuzzy implements FWinnerFunction {
	public static enum TRANSFER {
		MINIMUM, MEAN
	}
	
	public TRANSFER transfer;
	/**
	 * 
	 */
	public WinnerByFuzzy(TRANSFER transfer) {
		this.transfer = transfer;
	}

	public double transferFunction(double[] data){
		switch(transfer){
		case MEAN:
			return MathUtils.mean(data);
		default:
			return MathUtils.min(data);
		}
	}
	
	@Override
	public FWinnerInfo function(FCodeBook codebook, Entry sample) {
		FWinnerInfo[] wis;
		
		wis = this.function(codebook, sample, 1);
		
		return wis[0];
	}

	@Override
	public FWinnerInfo[] function(FCodeBook codebook, Entry sample, int num) {
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			winner[i].coef = Double.NEGATIVE_INFINITY; //sort descending, set coef to minimum
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		while(eIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			
			for(int j=0;j < code.size();j++){
				code.miu[j] = code.data[j].getMaxIntersection(sample.data[j]);
			}
			
			score = transferFunction(code.miu);
			
			/*sort desc*/
			for(i=0; (i < winner.length) && (score < winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}
}
