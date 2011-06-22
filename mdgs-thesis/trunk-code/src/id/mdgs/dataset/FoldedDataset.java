package id.mdgs.dataset;

import id.mdgs.dataset.DatasetProfiler.PEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FoldedDataset<T, E> implements Iterable<E> {
	public class FoldedIterator implements Iterator<E>{
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < FoldedDataset.this.folded.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (!hasNext()) {
				return null;
			}
			
			int pos = FoldedDataset.this.folded.get(currentIndex++);
			
			if(dataset instanceof Dataset)
				return (E) ((Dataset)FoldedDataset.this.dataset).get(pos);
			else 
				return (E) ((FCodeBook)FoldedDataset.this.dataset).get(pos);
			
		}

		@Override
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");			
		}
		
		public void rewind(){
			currentIndex = 0;
		}		
	}
	
	public T dataset;
	public List<Integer> folded; 
	
	public FoldedDataset(T ds){
		assert((dataset instanceof Dataset) || (dataset instanceof FCodeBook));
		
		dataset = ds;
		folded = new ArrayList<Integer>();
		makeRoundRobin(1);
	}
	
	public void makeRoundRobin(int num){
		DatasetProfiler dp = new DatasetProfiler();
		if(dataset instanceof Dataset)
			dp.run((Dataset)dataset);
		else 
			dp.run((FCodeBook)dataset);
		
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
					int pos = pe.get((i+j) % pe.size());
					folded.add(pos);
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
