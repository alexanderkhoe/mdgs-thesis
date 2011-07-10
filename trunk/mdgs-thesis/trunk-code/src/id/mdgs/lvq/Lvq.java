/**
 * 
 */
package id.mdgs.lvq;

import java.util.List;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.lvq.LvqUtils.MinMax;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.IClassify;
import id.mdgs.master.WinnerFunction;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Lvq implements IClassify<Dataset, Entry> {

	//codebook vector
	public Dataset codebook;
	public WinnerFunction findWinner;
	
	public Lvq(){
		codebook = new Dataset();
		findWinner = new WinnerByEuc();
	}
	
	public int classify(Entry e){
		WinnerInfo wi;
		
		wi = this.findWinner.function(this.codebook, e);
		return wi.winner.label;
	}
	
	public void initCodes(Dataset data, double min, double max){
		initCodes(data, min, max, 1);
	}
	
	/**
	 * pick code using random data range (min, max)
	 * if min == max, then range is spesific between dimension
	 * 
	 * @param data
	 */
	public void initCodes(Dataset data, double min, double max, int num){
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);
		
		codebook.reset();
		codebook.copyInfo(data);

		MinMax[] dataRange = new MinMax[data.numFeatures];
		if(MathUtils.equals((max - min) , 0)) {
			for(int i=0;i<dataRange.length;i++) 
				dataRange[i] = new MinMax();
			
			/*find min max data training*/
			for(Entry e: data){			
				for(int i = 0;i < e.size();i++){
					dataRange[i].setMinMax(e.data[i]);
				}
			}
		}
		
		for(PEntry pe: profiler){
			for(int i=0;i<num;i++){
				Entry code = new Entry(data.numFeatures);
				code.label = pe.label;
				for(int j=0;j < code.size();j++){
					if(MathUtils.equals((max - min) , 0)) {
						code.data[j] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
					} else {
						code.data[j] = MathUtils.randomDouble(min,max);
					}
				}
			
				codebook.add(code);
			}
		}
	}
	
	/**
	 * get 1 random code from dataset
	 * @param data
	 */
	public void initCodes(Dataset data){
		this.initCodes(data, 1);
	}
	
	/**
	 * pick random code from dataset, for every class, num item
	 * 
	 * @param data
	 */
	public void initCodes(Dataset data, int num){
		
		List<Entry> le = null;
		
		le = LvqUtils.pickRandomCodes(data, num);
		
		codebook.reset();
		//copy info
		codebook.copyInfo(data);
		
		if(le != null)
			codebook.addAll(le);		
	}

	/**
	 * pick code from dataset using knn (every run will produce same result if knn is equal
	 * 
	 * @param data
	 * @param num
	 * @param knn
	 */
	@SuppressWarnings("unchecked")
	public <T extends Iterable<Entry>> void initCodes(T data, int num, int knn){
		
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
		if(data.getClass().getSimpleName().equals("Dataset"))
			codebook.copyInfo((Dataset)data);
		else
			codebook.numFeatures = ((FoldedDataset<Dataset, Entry>)data).getMasterData().numFeatures;
		
		if(le1 != null)
			codebook.addAll(le1);
		if(le2 != null)
			codebook.addAll(le2);
	}

	//random from folded data
	public void initCodes(FoldedDataset<Dataset, Entry> data, int num){
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		codebook.reset();
		codebook.numFeatures = data.getMasterData().numFeatures;
		
		for(PEntry pe: profiler){
			int max = num;
			if(max > pe.size()) max = pe.size();
			int[] unique;
			
			/*find unique random index for each class*/
			unique = MathUtils.randPerm(max, pe.size());
			
			/*add codebook*/
			for(int i = 0;i < unique.length;i++){
				Entry code = data.get(pe.get(unique[i]));
				codebook.add(code.clone());				
			}
		}
	}
	
	public void initCodes(FoldedDataset<Dataset, Entry> data, double min, double max, int num){
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);
		
		codebook.reset();
		codebook.numFeatures = data.getMasterData().numFeatures;

		MinMax[] dataRange = new MinMax[data.getMasterData().numFeatures];
		if(MathUtils.equals((max - min) , 0)) {
			for(int i=0;i<dataRange.length;i++) 
				dataRange[i] = new MinMax();
			
			/*find min max data training*/
			for(Entry e: data){			
				for(int i = 0;i < e.size();i++){
					dataRange[i].setMinMax(e.data[i]);
				}
			}
		}
		
		for(PEntry pe: profiler){
			for(int i=0;i<num;i++){
				Entry code = new Entry(data.getMasterData().numFeatures);
				code.label = pe.label;
				for(int j=0;j < code.size();j++){
					if(MathUtils.equals((max - min) , 0)) {
						code.data[j] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
					} else {
						code.data[j] = MathUtils.randomDouble(min,max);
					}
				}
			
				codebook.add(code);
			}
		}
	}
	
	@Override
	public void loadCodebook(Dataset codebook) {
		this.codebook.reset();
		this.codebook.copyInfo(codebook);
		for(Entry e : codebook){
			this.codebook.add(e);
		}
	}

	@Override
	public void saveCodebook(String location) {
		this.codebook.save(location);
	}

	@Override
	public void loadCodebook(String location) {
		codebook.reset();
		this.codebook.load(location);
	}

	@Override
	public Dataset getCodebook() {
		return this.codebook;
	}
}
