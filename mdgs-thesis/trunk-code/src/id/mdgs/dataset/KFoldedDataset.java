/**
 * 
 */
package id.mdgs.dataset;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FoldedDataset.FoldedIterator;
import id.mdgs.utils.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author I Made Agus Setiawan
 *
 */
public class KFoldedDataset<T, E> implements Iterable<FoldedDataset<T, E>> {
	public class KFoldedIterator implements Iterator<FoldedDataset<T, E>>{
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < KFoldedDataset.this.size();
		}

		/*re*/
		public FoldedDataset<T, E> next() {
//			throw new RuntimeException("not supported");
			this.currentIndex++;
			return null;
		}	
		
		public FoldedDataset<T, E> nextTrain() {
			if (!hasNext()) {
				return null;
			}
			
			//currentIndex 
			return KFoldedDataset.this.getKFoldedForTrain(currentIndex);
		}

		public FoldedDataset<T, E> nextTest() {
			if (!hasNext()) {
				return null;
			}
			
			//currentIndex 
			return KFoldedDataset.this.getKFoldedForTest(currentIndex);
		}

		
		@Override
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");			
		}
		
		public void rewind(){
			currentIndex = 0;
		}
	}
	
	private T masterDataset;
	public List<FoldedDataset<T, E>> foldeds; 
	private int nTrain;
	private int K;
	
	/**
	 * Be aware when setting parameter K and portion. Commonly, K is [5,10] and
	 * portion is 0.5, 0.8, 0.9 depend on the K value. Remember portion is amount of
	 * training data, the rest is testing data.
	 * 
	 * @param ds
	 * @param K
	 * @param trainPortion
	 */
	public KFoldedDataset(T ds, int K, double trainPortion, boolean random){
		assert((masterDataset instanceof Dataset) || (masterDataset instanceof FCodeBook));
		
		this.masterDataset = ds;		
		
		this.foldeds = new ArrayList<FoldedDataset<T, E>>();
		this.fold(K, random);
		
		setTrainPortion(trainPortion);
	}
	
	public int size() {
		return this.foldeds.size();
	}

	public void setTrainPortion(double portion){
		this.nTrain = (int) Math.round(portion * K);
	}
	
	public FoldedDataset<T, E> getKFoldedForTrain(int curIdx){
		if(curIdx >= this.K || curIdx < 0)
			throw new IndexOutOfBoundsException("[KFoldedDataset] curIdx:" + curIdx);

		FoldedDataset<T, E> train = new FoldedDataset<T, E>(masterDataset, false);
		
		int it = curIdx;
		int done = 0;
		while(done < this.nTrain){
			int pos = it % K;
			train.add(this.foldeds.get(pos));
			it++;
			done++;
		}
		
		train.makeRoundRobin(1);
		return train;
	}
	
	public FoldedDataset<T, E> getKFoldedForTest(int curIdx){
		if(curIdx >= this.K || curIdx < 0)
			throw new IndexOutOfBoundsException("[KFoldedDataset] curIdx:" + curIdx);

		FoldedDataset<T, E> test = new FoldedDataset<T, E>(masterDataset, false);
		
		int it = curIdx + this.nTrain;
		int done = 0;
		while(done < (K - this.nTrain)){
			int pos = it % K;
			test.add(this.foldeds.get(pos));
			it++;
			done++;
		}
		
		test.makeRoundRobin(1);
		return test;
	}
	
	public FoldedDataset<T, E> get(int idx) {
		if(idx >= this.K || idx < 0)
			throw new IndexOutOfBoundsException("[KFoldedDataset] curIdx:" + idx);

		
		return this.foldeds.get(idx);
	}
	
	/**
	 * 
	 * @param K Jumlah kelompok data
	 * @param random true : data dibagi secara acak untuk setiap K fold
	 * 				 false: dibagi secara sequensial
	 */
	public void fold(int K, boolean random){
		this.K = K;
		
		DatasetProfiler dp = new DatasetProfiler();
		if(masterDataset instanceof Dataset)
			dp.run((Dataset)masterDataset);
		else 
			dp.run((FCodeBook)masterDataset);
		
		this.foldeds.clear();
		
		//make K object
		for(int i=0;i<K;i++){
			this.foldeds.add(new FoldedDataset<T, E>(masterDataset, false));
		}
			
		/**
		 * jika jumlah data tidak mencukupi untuk di bagi sebanyak K,
		 * maka 
		 * 	- jika threshold (N/K) lebih dari 0.5, maka beberapa data akan diduplikasi
		 * 	  pada beberapa fold terakhir, untuk menghindari suatu fold tidak kebagian
		 *    perwakilan kelas, jika data nya sedikit.
		 *  - jika kurang dari 0.5, maka sisa data tidak dipakai.
		 * 
		 * pemilihan sample data dilakukan secara sequensial/random
		 */
		for(PEntry pe: dp){
			//jumlah sample data tiap fold
			int max = Math.round((float) pe.size() / this.K);
			
			int[] rpos;
			if(random)
				rpos = MathUtils.randPerm(pe.size(), pe.size());
			else
				rpos = MathUtils.genSeq(pe.size());
			
			int i = 0;
			while( i < (max * K) ){
				for(int j=0;j < K;j++,i++){
					int pos = i % pe.size();
					foldeds.get(j).folded.add(pe.get(rpos[pos]));
				}
			}
		}
	}
	
	@Override
	public KFoldedIterator iterator() {
		final  KFoldedIterator it = new KFoldedIterator();
		return it;
	}
}
