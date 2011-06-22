/**
 * 
 */
package id.mdgs.dataset;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author I Made Agus Setiawan
 *
 */
public class HitList implements Iterable<HitList.HitEntry>{
	
	/*HitEntry*/
	public static class HitEntry{
		public int label 	= 0;
		public long freq 	= 0;
		
		public void swap(HitEntry he){
			HitEntry tmp = new HitEntry();
			tmp.copy(he);
			he.copy(this);
			this.copy(tmp);
			
			tmp = null;
		}
		
		public void copy(HitEntry he){
			this.label = he.label;
			this.freq  = he.freq;
		}
	}
	
	/*HitEntry Iterator*/
	public class HitEntryIterator implements Iterator<HitEntry> {
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < HitList.this.entries.size();
		}

		@Override
		public HitEntry next() {
			if (!hasNext()) {
				return null;
			}
			
			return HitList.this.entries.get(currentIndex++);
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
	public List<HitEntry> entries;
	
	public HitList(){
		this.entries = new ArrayList<HitEntry>();
	}
	
	public int size(){
		return this.entries.size();
	}
	
	public void reset(){
		this.entries.clear();
	}
	
	public HitEntry get(int pos){
		if(pos >= size()){
			throw new RuntimeException(
					String.format("Pos (%d) exceed number of entries (%d)", 
							pos, size())
					);
		}
		
		return this.entries.get(pos);
	}
	
	public long addHit(int label){
		HitEntry he;
		
		he = findHit(label);
		if(he != null){
			he.freq++;
			
			//keep in descending order, higher freq are in the beginning
			int j;
			for(j=this.size()-1; 
				j > 0  && 
				this.get(j).freq <= this.get(j-1).freq;
				j--);
			
			if(j > 0){
				while(j > 0 && this.get(j-1).freq < this.get(j).freq){
					HitEntry tmp;
					tmp = this.get(j);
					this.entries.set(j, this.get(j-1));
					this.entries.set(j-1, tmp);
					j--;
				}
			}
		} 
		
		else {
			he = new HitEntry();
			he.label	= label;
			he.freq 	= 1;
			
			this.entries.add(he);
		}
		
		return he.freq;
	}

	public HitEntry findHit(int label){
		HitEntry he = null;
		
		for(int pos=0;pos < this.size();pos++){
			if(this.get(pos).label == label){
				he = this.get(pos);
				break;
			}
		}
		
		return he;
	}
	
	public void run(Dataset set){
		for(Entry e: set){
			this.addHit(e.label);
		}
	}
	
	public String toString(){
		return this.toString(null);
	}
	
	public String toString(List<String> labels){
		StringBuilder sb = new StringBuilder();
		
		sb.append("HIT TABLE\n");
		for(int pos=0;pos < this.size();pos++){
			String label;
			HitEntry he = this.get(pos);
			
			if(labels == null){
				label = String.format("#%d",he.label);
			} else {
				if(he.label >= labels.size()){
					label = String.format("%d-EMPTY", he.label);
				} else {
					label = labels.get(he.label);
				}
			}
			sb.append(String.format("Class %s has %d entries\n", label, he.freq));
		}
		
		return sb.toString();
	}

	@Override
	public Iterator<HitEntry> iterator() {
		final HitEntryIterator it = new HitEntryIterator();
		return it;
	}
	
}
