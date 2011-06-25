package id.mdgs.dataset;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.RuntimeErrorException;

public class FoldedDataset<T, E> implements Iterable<E> {
	public class FoldedIterator implements Iterator<E>{
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < FoldedDataset.this.folded.size();
		}

		@Override
		public E next() {
			if (!hasNext()) {
				return null;
			}
			
			return FoldedDataset.this.get(currentIndex++);
			
//			int pos = FoldedDataset.this.folded.get(currentIndex++);
//			
//			if(masterDataset instanceof Dataset)
//				return (E) ((Dataset)FoldedDataset.this.masterDataset).get(pos);
//			else 
//				return (E) ((FCodeBook)FoldedDataset.this.masterDataset).get(pos);
			
		}

		@Override
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");			
		}
		
		public void rewind(){
			currentIndex = 0;
		}		
	}
	
	public T masterDataset;
	public List<Integer> folded; 
	
	public FoldedDataset(T ds){
		this(ds, true);
	}
	
	public FoldedDataset(T ds, boolean autofeed){
		assert((masterDataset instanceof Dataset) || (masterDataset instanceof FCodeBook));
		
		masterDataset = ds;
		folded = new ArrayList<Integer>();
		
		if(autofeed){
			doAutoFeed();
			makeRoundRobin(1);
		}
	}
	
	public T getMasterData(){
		return (T) masterDataset;
	}
	
	public int masterSize(){
		if(masterDataset instanceof Dataset)
			return ((Dataset)masterDataset).size();
		else 
			return ((FCodeBook)masterDataset).size();
	}
	
	public void doAutoFeed(){
		int max;
		max = masterSize();
		
		folded.clear();
		
		for(int i=0;i < max;i++){
			add(i);
		}
	}
	
	public void add(int pos){
		if(pos >= masterSize() && pos < 0){
			throw new RuntimeException(
					String.format("[FoldedDataset] Pos (%d) exceed number of entries (%d) or Pos is negative", 
							pos, size())
					);
		}
		
		this.folded.add(pos);
	}
	
	private void addAll(List<Integer> ds){
		for(int pos: ds){
			this.add(pos);
		}
	}
	
	public void add(FoldedDataset<?, ?> dset){
		if(masterDataset != dset.masterDataset)
			throw new RuntimeException("Can not join FoldedDataset with difference master.");
		
		this.addAll(dset.folded);
	}
	
	public int size(){
		return folded.size();
	}
	
	@SuppressWarnings("unchecked")
	public E get(int idx) {
		if(idx >= size()){
			throw new RuntimeException(
					String.format("[FoldedDataset] Pos (%d) exceed number of entries (%d)", 
							idx, size())
					);
		}
		
		int pos = this.folded.get(idx);
//		System.out.print(pos + "\t");
		if(masterDataset instanceof Dataset){
			return (E) ((Dataset)this.masterDataset).get(pos);
		} else {
			return (E) ((FCodeBook)this.masterDataset).get(pos);
		}
	}
	
	/**
	 * Re-arrange the sequence of data used in round-robin order (saling silang)
	 * with num sample data per class 
	 * 
	 * @param num number of sample data per class that is crossed for each cycle
	 */
	@SuppressWarnings("unchecked")
	public void makeRoundRobin(int num){
		if(size() == 0){
			throw new RuntimeException("Data empty");
		}
		
		DatasetProfiler dp = new DatasetProfiler();
		if(masterDataset instanceof Dataset)
			dp.run((FoldedDataset<Dataset, Entry>) this);
		else 
			dp.run((FoldedDataset<FCodeBook, FEntry>) this);
		
		ArrayList<Integer> dump = (ArrayList<Integer>) ((ArrayList<Integer>) folded).clone();
		folded.clear();
		
		//find max
		int maxData = 0;
		for(PEntry pe: dp){
			if (maxData < pe.size()){
				maxData = pe.size();
			}
		}
		
		for(int i=0;i < maxData;i+=num){
			for(PEntry pe: dp){
				for(int j=0;j < num;j++){
					if((i+j) >= pe.size()) continue;
					int pos = dump.get(pe.get((i+j) % pe.size()));
					this.add(pos);
				}
			}
		}
	}
	
	
	
	@Override
	public Iterator<E> iterator() {
		final FoldedIterator it = new FoldedIterator();
		return it;
	}
}
