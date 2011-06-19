/**
 * 
 */
package id.mdgs.lvq;

import java.util.Iterator;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;

/**
 * @author I Made Agus Setiawan
 *
 */
public class WinnerByCos implements WinnerFunction {

	/**
	 * 
	 */
	public WinnerByCos() {
		// TODO Auto-generated constructor stub
	}

	public static double cosineSimilarity(Entry code, Entry sample){
		double dot = 0;
		double lengthCode = 0;
		double lengthSample = 0;
		
		for(int i=0;i < code.data.length; i++) {
			dot += code.data[i] * sample.data[i];
			
			lengthCode += code.data[i] * code.data[i];
			lengthSample += sample.data[i] * sample.data[i];
		}
			
		lengthCode = Math.sqrt(lengthCode);
		lengthSample = Math.sqrt(lengthSample);
		
		
		return dot / (lengthCode * lengthSample); 
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
			/*cosine similarity range between -1 .. 1 
			 * set init coef = -1 */
			winner[i].coef = -1;
		}
		
		Iterator<Entry> eIt = codes.iterator();
		while(eIt.hasNext()) {
			double similarity = 0;
			Entry code = eIt.next();
			
			similarity = cosineSimilarity(code, sample);
			
			for(i=0; (i < knn) && (similarity < winner[i].coef); i++);
			
			if(i < knn){
				for(int j=knn - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = similarity;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}

}
