/**
 * 
 */
package id.mdgs.lvq;

import java.util.ArrayList;
import java.util.List;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.DatasetProfiler.PEntry;
import id.mdgs.lvq.HitList.HitEntry;
import id.mdgs.lvq.LvqUtils.MinMax;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Lvq {

	//codebook vector
	public Dataset codebook;
	
	public Lvq(){
		codebook = new Dataset();
	}
	
	public int classify(Entry e){
		WinnerInfo wi;
		
		wi = LvqUtils.findWinner(this.codebook, e);
		return wi.winner.label;
	}
	
	public void initCodesRandom(Dataset data){
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);
		
		MinMax[] dataRange = new MinMax[data.numFeatures];
		for(int i=0;i<dataRange.length;i++) 
			dataRange[i] = new MinMax();
		
		/*find min max data training*/
		for(Entry e: data){			
			for(int i = 0;i < e.size();i++){
				dataRange[i].setMinMax(e.data[i]);
			}
		}
		
		for(PEntry pe: profiler){
			Entry code = new Entry(data.numFeatures);
			code.label = pe.label;
			for(int j=0;j < code.size();j++){
				//code.data[j] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
				code.data[j] = MathUtils.randomDouble(-1,1);
			}
			
			codebook.add(code);
		}
	}
	
	public void initCodes(Dataset data){
		//pick random (every class) from dataset
		List<Entry> le = null;
		
		le = LvqUtils.pickRandomCodes(data, 1);
		
		codebook.reset();
		//copy info
		codebook.copyInfo(data);
		
		if(le != null)
			codebook.addAll(le);		
	}

	public void initCodes(Dataset data, int num, int knn){
		//pick from dataset
		HitList classes = new HitList();
		List<Entry> le1 = null, le2 = null;
		
		for(Entry e: data){
			classes.addHit(e.label);
		}
		
		for(HitEntry he: classes){
			if(he.freq > num){
				he.freq = num;
			}
		}
		
		le1 = LvqUtils.pickInsideCodes(classes, data, knn);
		
		//check how many code is not found
		int rem = 0;
		for(HitEntry he: classes){
			if(he.freq > 0){
				rem += he.freq;
			}
		}

		if(rem > 0){
			le2 = LvqUtils.pickInsideCodes(classes, data, knn);
		}
		
		codebook.reset();
		//copy labels table and dimension
		codebook.copyInfo(data);
		
		if(le1 != null)
			codebook.addAll(le1);
		if(le2 != null)
			codebook.addAll(le2);
	}
	
	public void initCodeByPca(){
		
	}
}
