package id.mdgs.fnlvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.lvq.LvqUtils.MinMax;
import id.mdgs.master.FWinnerFunction;
import id.mdgs.master.IClassify;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fnlvq implements IClassify<FCodeBook, Entry>{

	public FCodeBook codebook;
	public FWinnerFunction findWinner;
	
	public Fnlvq(){
		codebook = new FCodeBook();
		findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MINIMUM);
	}
	
	public int classify(Entry sample){
		FWinnerInfo wi;
		
		wi = this.findWinner.function(codebook, sample);
		
		if(MathUtils.equals(wi.coef, 0))
			return -1;
		else
			return wi.winner.label;
	}
	
	/**
	 * //pick code from dataset by random or sequence
	 * 
	 * @param data
	 * @param num    number of data to be agregated, if want to use all the data in the class,
	 * 				 set as high as possible, regarding the max number of data in each class.
	 * @param random true if random, false if sequence
	 */
	public void initCodes(Dataset data, int num, boolean random){
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);
		
		codebook.reset();
		codebook.numFeatures = data.numFeatures;

		/**/
		for(PEntry pe: profiler){
			FEntry code = new FEntry(data.numFeatures);
			code.label = pe.label;
			
			int max = num;
			if(pe.size() < max) max = pe.size();
			
			int[] rpos;
			if(random)
				rpos = MathUtils.randPerm(max, pe.size());
			else
				rpos = MathUtils.genSeq(max);
			
			double[][] fz = new double[data.numFeatures][3];
			for(int i=0;i<data.numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int i=0;i<rpos.length;i++){
				Entry sample = data.get(pe.get(rpos[i]));
				
				for(int j=0;j < sample.size();j++){
					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
					fz[j][1] += sample.data[j];
				}
			}
			
			for(int j=0;j < data.numFeatures;j++) {
				fz[j][1] /= max;
				if(max == 1){
					fz[j][0] = fz[j][1] - 0.2;
					fz[j][2] = fz[j][1] + 0.2;
				}
			}

			
			for(int i=0;i < code.size();i++){
				code.data[i].set(fz[i]);
			}
			
			codebook.add(code);
		}
	}
	
	/**
	 * ambil porsi sebesar portion dari tiap data kelas
	 * Untuk menggunakan semua data sebagai basis, set portion=1.0 dan random=false
	 * 
	 * @param data
	 * @param portion
	 * @param random true if random, false if sequence 
	 */
	public void initCodes(Dataset data, double portion, boolean random){
		//pick random (every class) from dataset
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		codebook.reset();
		codebook.numFeatures = data.numFeatures;
		
		/**/
		for(PEntry pe: profiler){
			FEntry code = new FEntry(data.numFeatures);
			code.label = pe.label;
			
			int max = (int) portion * pe.size();
			if(max < 3) max = pe.size();
			
			int[] rpos;
			if(random)
				rpos = MathUtils.randPerm(max, pe.size());
			else
				rpos = MathUtils.genSeq(max);
			
			double[][] fz = new double[data.numFeatures][3];
			for(int i=0;i<data.numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int i=0;i<rpos.length;i++){
				Entry sample = data.get(pe.get(rpos[i]));
				
				for(int j=0;j < sample.size();j++){
					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
					fz[j][1] += sample.data[j];
				}
			}
			
			for(int j=0;j < data.numFeatures;j++) {
				fz[j][1] /= max;
				if(pe.size() < 3){
					fz[j][0] = fz[j][1] - 0.2;
					fz[j][2] = fz[j][1] + 0.2;
				}
			}

			
			for(int i=0;i < code.size();i++){
				code.data[i].set(fz[i]);
			}
			
			codebook.add(code);
		}
	}
	
	public void initCodes(FoldedDataset<Dataset, Entry> data, double portion, boolean random){
		//pick random (every class) from dataset
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		codebook.reset();
		codebook.numFeatures = data.getMasterData().numFeatures;
		
		/**/
		for(PEntry pe: profiler){
			FEntry code = new FEntry(data.getMasterData().numFeatures);
			code.label = pe.label;
			
			int max = (int) portion * pe.size();
			if(max < 3) max = pe.size();
			
			int[] rpos;
			if(random)
				rpos = MathUtils.randPerm(max, pe.size());
			else
				rpos = MathUtils.genSeq(max);
			
			double[][] fz = new double[data.getMasterData().numFeatures][3];
			for(int i=0;i<data.getMasterData().numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int i=0;i<rpos.length;i++){
				Entry sample = data.get(pe.get(rpos[i]));
				
				for(int j=0;j < sample.size();j++){
					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
					fz[j][1] += sample.data[j];
				}
			}
			
			for(int j=0;j < data.getMasterData().numFeatures;j++) {
				fz[j][1] /= max;
				if(pe.size() < 3){
					fz[j][0] = fz[j][1] - 0.2;
					fz[j][2] = fz[j][1] + 0.2;
				}
			}

			
			for(int i=0;i < code.size();i++){
				code.data[i].set(fz[i]);
			}
			
			codebook.add(code);
		}
	}
	
	/**
	 * init bobot dengan nilai random antara rentang min-max
	 * Jika min=max, maka rentang akan didasarkan atas nilai min-max pada 
	 * dataset per dimensi
	 * 
	 * @param data
	 * @param min
	 * @param max
	 */
	public void initCodes(Dataset data, double min, double max){
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		codebook.reset();
		codebook.numFeatures = data.numFeatures;
		
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
			FEntry code = new FEntry(data.numFeatures);
			code.label = pe.label;
			
			double[][] fz = new double[data.numFeatures][3];
			for(int i=0;i<data.numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int j=0;j < code.size();j++){
				if(MathUtils.equals((max - min) , 0)) {
					fz[j][1] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
				} else {
					fz[j][1] = MathUtils.randomDouble(min,max);
				}
				
				fz[j][0] = fz[j][1] - 0.2;
				fz[j][2] = fz[j][1] + 0.2;
				
			}
			
			codebook.add(code);
		}
	}
	
	public void initCodes(FoldedDataset<Dataset, Entry> data, double min, double max){
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
			FEntry code = new FEntry(data.getMasterData().numFeatures);
			code.label = pe.label;
			
			double[][] fz = new double[data.getMasterData().numFeatures][3];
			for(int i=0;i<fz.length;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int j=0;j < code.size();j++){
				if(MathUtils.equals((max - min) , 0)) {
					fz[j][1] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
				} else {
					fz[j][1] = MathUtils.randomDouble(min,max);
				}
				
				double range = (dataRange[j].max - dataRange[j].min) / 2;
				fz[j][0] = fz[j][1] - range;
				fz[j][2] = fz[j][1] + range;
				
			}
			
			codebook.add(code);
		}
	}
//	/**
//	 * ambil agregate dari semua data
//	 * @param data
//	 */
//	public void initCodes(Dataset data){
//		DatasetProfiler profiler = new DatasetProfiler();
//		profiler.run(data);
//
//		/**/
//		for(PEntry pe: profiler){
//			FEntry code = new FEntry(data.numFeatures);
//			code.label = pe.label;
//			
//			/*need to be optimized, not handle data lest then 2*/
//			int max = pe.size();
//			
////			int[] rpos = MathUtils.randPerm(max, pe.size());
//			double[][] fz = new double[data.numFeatures][3];
//			for(int i=0;i<data.numFeatures;i++){
//				fz[i][0] = Double.POSITIVE_INFINITY; //min
//				fz[i][1] = 0;   //mean
//				fz[i][2] = Double.NEGATIVE_INFINITY;//max
//			}
//			
//			for(int i=0;i<pe.size();i++){
//				Entry sample = data.get(pe.get(i));
//				
//				for(int j=0;j < sample.size();j++){
//					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
//					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
//					fz[j][1] += sample.data[j];
//				}
//			}
//			
//			for(int j=0;j < data.numFeatures;j++) {
//				fz[j][1] /= max;
//				if(max == 1){
//					fz[j][0] = fz[j][1] - 0.5;
//					fz[j][2] = fz[j][1] + 0.5;
//				}
//			}
//
//			for(int i=0;i < code.size();i++){
//				code.data[i].set(fz[i]);
//			}
//			
//			codebook.add(code);
//		}
//	}
	
	public void initCodes(FCodeBook codebook){
		codebook.reset();
		this.codebook.copyInfo(codebook);
		for(FEntry fe : codebook){
			this.codebook.add(fe);
		}
	}

	@Override
	public void loadCodebook(FCodeBook codebook) {
		this.codebook.reset();
		this.codebook.copyInfo(codebook);
		for(FEntry fe : codebook){
			this.codebook.add(fe);
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

}
