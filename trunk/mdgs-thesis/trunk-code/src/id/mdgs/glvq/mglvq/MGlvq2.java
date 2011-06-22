/**
 * 
 */
package id.mdgs.glvq.mglvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class MGlvq2 extends MGlvq {

	/**
	 * @param mps
	 */
	public MGlvq2(MParam[] mps) {
		super(mps);
	}

	public void calcThreshold(Dataset tset){
		
		DatasetProfiler dp = new DatasetProfiler();
		dp.run(tset);
		
		for(PEntry pe: dp){
			Entry code = codebook.findEntry(pe.label);
			MParam mp = mps[pe.label];
			
			for(int i=0;i < pe.size();i++){
				Entry sample = tset.get(pe.get(i));
				double dist = getDistance(normalize(sample, code.label), code);
				mp.threshold = MathUtils.max(mp.threshold, dist);
			}
		}
	}
	
	public int classify(Entry e){
		WinnerInfo[] win;
		
		win = this.findWinner(this.codebook, e);
		if(e.label == -1)
			System.out.println(String.format("thres:win <=> %f:%f\n", mps[win[0].winner.label].threshold, win[0].coef ));
		
		if(win[0].coef > mps[win[0].winner.label].threshold){
			return -1;
		} else {
			return win[0].winner.label;
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i < mps.length;i++){
			sb.append(String.format("Kelas %d: %f\n", i, mps[i].threshold));
		}
		
		return sb.toString();
	}
}
