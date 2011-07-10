/**
 * 
 */
package id.mdgs.glvq.mglvq;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.glvq.Glvq;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.lvq.LvqUtils.MinMax;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class MGlvq extends Glvq {
	
	public static class MParam {
		public RealMatrix invS;
		public Entry mean;
		public Entry std;
		public double threshold;
		public MParam(){
			threshold = 0;
		}
	}
	
	//codebook vector
	public Dataset codebook;
	
	public MParam[] mps;
	
	public MGlvq(MParam[] mps){
		codebook = new Dataset();
		this.mps = mps;
	}
	
//	public int classify(Entry e){
//		WinnerInfo[] win;
//		
//		/*normalize*/
//		
//		win = this.findWinner(this.codebook, e);
//		return win[0].winner.label;
//	}
	
//	public void initCodesRandom(Dataset data){
//		DatasetProfiler profiler = new DatasetProfiler();
//		profiler.run(data);
//		
//		MinMax[] dataRange = new MinMax[data.numFeatures];
//		for(int i=0;i<dataRange.length;i++) 
//			dataRange[i] = new MinMax();
//		
//		/*find min max data training*/
//		for(Entry e: data){			
//			for(int i = 0;i < e.size();i++){
//				dataRange[i].setMinMax(e.data[i]);
//			}
//		}
//		
//		for(PEntry pe: profiler){
//			Entry code = new Entry(data.numFeatures);
//			code.label = pe.label;
//			for(int j=0;j < code.size();j++){
//				//code.data[j] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
//				code.data[j] = MathUtils.randomDouble(-1,1);
//			}
//			
//			codebook.add(code);
//		}
//	}
//	
//	public void initCodes(Dataset data){
//		//pick random (every class) from dataset
//		List<Entry> le = null;
//		
//		le = LvqUtils.pickRandomCodes(data, 1);
//		
//		codebook.reset();
//		//copy info
//		codebook.copyInfo(data);
//		
//		if(le != null)
//			codebook.addAll(le);		
//	}
//
//	public void initCodes(Dataset data, int num, int knn){
//		//pick from dataset
//		HitList classes = new HitList();
//		List<Entry> le1 = null, le2 = null;
//		
//		for(Entry e: data){
//			classes.addHit(e.label);
//		}
//		
//		for(HitEntry he: classes){
//			if(he.freq > num){
//				he.freq = num;
//			}
//		}
//		
//		le1 = LvqUtils.pickInsideCodes(classes, data, knn);
//		
//		//check how many code is not found
//		int rem = 0;
//		for(HitEntry he: classes){
//			if(he.freq > 0){
//				rem += he.freq;
//			}
//		}
//
//		if(rem > 0){
//			le2 = LvqUtils.pickInsideCodes(classes, data, knn);
//		}
//		
//		codebook.reset();
//		//copy labels table and dimension
//		codebook.copyInfo(data);
//		
//		if(le1 != null)
//			codebook.addAll(le1);
//		if(le2 != null)
//			codebook.addAll(le2);
//	}
	
	public double getDistance(Entry sample, Entry code){
		assert(mps != null);
		
		RealMatrix inv = mps[code.label].invS;
		return MathUtils.mahalanobisDistance(sample.data, code.data, inv);
	}
	
	public Entry normalize(Entry sample, int codelabel){
		assert(mps != null);
		MParam mp = mps[codelabel];
		
		Entry result = new Entry(sample.size());
		for(int i=0;i < sample.size();i++){
			result.data[i] = (sample.data[i] - mp.mean.data[i]);// / mp.std.data[i];
		}
		
		return result;
	}
	
	public WinnerInfo[] findWinner(Dataset codebook, Entry input){
		WinnerInfo[] winner;
		int i;
		winner = new WinnerInfo[codebook.size()];
		for(i=0;i < winner.length;i++){
			winner[i] = new WinnerInfo();
		}
		
		Iterator<Entry> eIt = codebook.iterator();
		while(eIt.hasNext()) {
			double difference = 0;
			Entry code = eIt.next();
			
			difference = getDistance(normalize(input, code.label), code);
			
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
