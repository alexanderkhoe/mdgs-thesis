/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.fnlvq.FCodeBook;
import id.mdgs.fnlvq.FCodeBook.FEntry;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.HitList.HitEntry;
import id.mdgs.lvq.HitList.HitEntryIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author I Made Agus Setiawan
 *
 */
public class DatasetProfiler  implements Iterable<DatasetProfiler.PEntry>{

	public class PEntry {
		public int label;
		public List<Integer> data;
		
		public PEntry(){
			label 	= -1;
			data 	= new ArrayList<Integer>();
		}
		
		public int get(int pos){
			if(pos >= size())
				return -1;
			
			return data.get(pos);
		}
		
		public void add(int index){
			this.data.add(index);
		}
		
		public int size() {
			return data.size();
		}
	}
	
	/*PEntry Iterator*/
	public class PEntryIterator implements Iterator<PEntry> {
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < DatasetProfiler.this.entries.size();
		}

		@Override
		public PEntry next() {
			if (!hasNext()) {
				return null;
			}
			
			return DatasetProfiler.this.entries.get(currentIndex++);
		}

		@Override
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");			
		}
		
		public void rewind(){
			currentIndex = 0;
		}		
	} 

	/*HitList variable and routine*/
	public List<PEntry> entries;
	
	public DatasetProfiler(){
		this.entries = new ArrayList<PEntry>();
	}
	
	public int size(){
		return this.entries.size();
	}
	
	public void reset(){
		this.entries.clear();
	}
	
	public PEntry findPEntry(int label){
		PEntry pe = null;
		
		for(int pos=0;pos < this.size();pos++){
			if(this.entries.get(pos).label == label){
				pe = this.entries.get(pos);
				break;
			}
		}
		
		return pe;
	}
	
	
	public void run(Dataset set){
		Entry e;
		PEntry pe;
		
		for(int pos=0; pos < set.size(); pos++){
			e = set.get(pos);
			
			pe = findPEntry(e.label);
			if(pe != null){
				pe.add(pos);
			} else {
				pe = new PEntry();
				pe.label = e.label;
				pe.add(pos);
				this.entries.add(pe);
			}
		}
	}
	
	public void run(FCodeBook set){
		FEntry e;
		PEntry pe;
		
		for(int pos=0; pos < set.size(); pos++){
			e = set.get(pos);
			
			pe = findPEntry(e.label);
			if(pe != null){
				pe.add(pos);
			} else {
				pe = new PEntry();
				pe.label = e.label;
				pe.add(pos);
				this.entries.add(pe);
			}
		}
	}
	
	@Override
	public Iterator<PEntry> iterator() {
		final PEntryIterator it = new PEntryIterator();
		return it;
	}
}
