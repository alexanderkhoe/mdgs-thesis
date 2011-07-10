/**
 * 
 */
package id.mdgs.dataset;

import id.mdgs.lvq.LvqUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Dataset implements Iterable<Dataset.Entry>{

	/*Dataset Entry*/
	public static class Entry {
		public double[] data;
		public int label;
		
		public  Entry(int size){
			data = new double[size];
		}
		
		public Entry(Entry e) {
			data = new double[e.size()];
			this.copy(e);
		}
		
		public Entry(double[] data) {
			this.data = new double[data.length];
			for(int i=0;i < data.length;i++){
				this.data[i] = data[i];
			}
		}
		
		public Entry clone() {
			return new Entry(this);
		}
		
		public void set(double val){
			for(int i=0;i < data.length;i++){
				data[i] = val;
			}
		}
		
		public int size(){
			return data.length;
		}
		
		public void copy(Entry e){
			for(int i=0;i < data.length;i++)
				this.data[i] = e.data[i];
			
			this.label = e.label;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<data.length;i++){
				sb.append(data[i]);
				//if(i != data.length - 1)
					sb.append(",");
			}
			sb.append(label);
			return sb.toString();
		}
	}
	
	/*Entry Iterator*/
	public class EntryIterator implements Iterator<Entry>{
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < Dataset.this.entries.size();
		}

		@Override
		public Entry next() {
			if (!hasNext()) {
				return null;
			}
			
			return Dataset.this.entries.get(currentIndex++);
		}

		@Override
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");
//			assert (currentIndex > 0 && currentIndex <= Dataset.this.entries.size()) : "error";
//			Dataset.this.entries.remove(currentIndex--);
//			Dataset.this.numEntries--;
		}
		
		public void rewind(){
			currentIndex = 0;
		}
	}
	
	/*Start Dataset variable and routine*/
	public int numFeatures;
	public int numEntries;
	public List<Entry>	  entries;	
	public String fname;
	public String delimiter = ",;|\t";
	
	
	public Dataset(){
		setFilename(null);
		this.entries 	= new ArrayList<Entry>();
		
		reset();
	}
	
	public Dataset(String fname){
		setFilename(fname);
		this.entries 	= new ArrayList<Entry>();
		
		reset();
	}
	
	public Dataset(Dataset ds){
		setFilename(ds.fname);
		this.entries 	= new ArrayList<Entry>();
		reset();
		
		copyInfo(ds);
		for(Entry e: ds.entries) {
			this.add(e.clone());
		}
	}
	
	public Dataset clone(){
		return new Dataset(this);
	}
	
	public void set(Dataset ds){
		/*assume property are the same, except entry value*/
		if(this.numEntries != ds.numEntries)
			throw new RuntimeException("number of entry not match");
		if(this.numFeatures != ds.numFeatures)
			throw new RuntimeException("dimension not match");
		
		for(int pos=0;pos < this.size();pos++)
			this.get(pos).copy(ds.get(pos));
	}
	
	/*for codebook, not for others*/
	public Entry findEntry(int label) {
		for(Entry code: this.entries){
			if(code.label == label){
				return code;
			}
		}
		return null;
	}
	
	public void reset(){
		this.numFeatures= 0;
		this.numEntries = 0;
		this.entries.clear();
	}
	
	public void copyInfo(Dataset ds){
		this.numFeatures = ds.numFeatures;
	}
	
	public int size(){
		return this.entries.size();
	}
	
	/* DATA ROUTINE */
	public void setFilename(String fname){
		this.fname = fname;
	}
	
	public void add(Entry e){
		this.entries.add(e);
		this.numEntries++;
	}
	
	public void addAll(List<Entry> es){
		for(Entry e: es)
			add(e);
	}
	
	public void join(Dataset ds){
		//assure same properties
//		if(this.numEntries != ds.numEntries)
//			throw new RuntimeException("number of entry not match");
		if(this.numFeatures != ds.numFeatures)
			throw new RuntimeException("dimension not match");
		
		this.addAll(ds.entries);
	}
	
	public Entry get(int pos) {
		if(pos >= size()){
			throw new RuntimeException(
					String.format("[Dataset] Pos (%d) exceed number of entries (%d)", 
							pos, size())
					);
		}
		
		return this.entries.get(pos);
	}
	
	public int load(double[][] data){
		//reset 
		this.reset();
		
		for(int i=0;i < data.length;i++){
			Entry e = new Entry(data[i].length);
			
			this.numFeatures = e.size();
			
			for(int j=0;j < data[i].length;j++){
				e.data[j] = data[i][j];
			}
			
			add(e);
		}
		
		return numEntries;
	}
	
	public int load(){
		if(this.fname == null)
			throw new RuntimeException("[Dataset] Filename not defined yet.");
		
		return load(this.fname);
	}
	
	public int load(String fname) {
		FileReader fr = null;
		BufferedReader br = null; 
		String currLine;
		
		//reset 
		this.reset();
		
		try {
			
			fr = new FileReader(fname);
			br = new BufferedReader(fr);
			
			this.fname = fname;
			
			int lineno = 0;
			while((currLine = br.readLine()) != null){
				lineno++;
				
				if(lineno == 1){
					StringTokenizer stok = new StringTokenizer(currLine, delimiter);
					if(stok.hasMoreTokens()){
						this.numFeatures = Integer.parseInt(stok.nextToken());
						this.numEntries = 0;
					} 
				}
				
				else {
					StringTokenizer stok = new StringTokenizer(currLine, delimiter);
					Entry entry = new Entry(this.numFeatures);
					int counter = 0;
					while (stok.hasMoreTokens()) {
						if (counter < this.numFeatures){
							String sval = stok.nextToken();
							double dval = Double.parseDouble(sval);
							entry.data[counter++] = dval;
						} else {
							String label = stok.nextToken();
							
							//entry.label = LvqUtils.getLabelId(label);
							entry.label = Integer.parseInt(label.trim());
							add(entry);
							
							break;
						}
					}
					
					if(counter != this.numFeatures){
						this.reset();
						throw new RuntimeException("line:" + lineno + " num of data not match.");
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.toString());
		} catch (NumberFormatException e) {
			throw new RuntimeException(e.toString());
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		} finally {
				try {
					if(br != null) br.close();
					if(fr != null) fr.close();
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
		}
		
		return numEntries;
	}
	
	/**
	 * Can't Handle this number -> 4.5998e-005
	 * it treat as different field 4.5998 and e-005
	 * status : PENDING
	 * @return
	 * @throws IOException
	 */
//	public int loadPENDING() throws IOException {
//		LineNumberReader lnr = new LineNumberReader(new FileReader(this.fname));
//	    lnr.setLineNumber(1);
//	    StreamTokenizer stok = new StreamTokenizer(lnr);
//	    stok.parseNumbers();
//	    stok.eolIsSignificant(true);
//	    
//        char[] c = delimiter.toCharArray();
//        for (int i=0; i<c.length; i++) {
//            stok.whitespaceChars(c[i], c[i]);
//        }	    
//        
//	    stok.nextToken();
//	    boolean once = false; List<Double> ld = new ArrayList<Double>();
//	    while (stok.ttype != StreamTokenizer.TT_EOF) {
//			int lineno = stok.lineno();
//			//get first line for num of features			      
//			if(lineno == 1 && stok.ttype == StreamTokenizer.TT_NUMBER){
//				this.numFeatures = (int) stok.nval;
//				this.numEntries = 0;
//				this.labels 	= new ArrayList<String>();
//				this.entries  = new ArrayList<Entry>();
//				once = true;
//				
//				while (stok.ttype != StreamTokenizer.TT_EOL) 
//					stok.nextToken();
//			} else {
//				if (once) {
//					Entry entry = new Entry(this.numFeatures);
//					int counter = 0;
//					while (stok.ttype != StreamTokenizer.TT_EOL) {
//						if (counter < this.numFeatures){
//							if (stok.ttype == StreamTokenizer.TT_NUMBER){
//								entry.data[counter++] = stok.nval;
//							}
//						} else {
//							if(stok.sval == null)
//								stok.sval = Integer.toString((int)stok.nval);
//
//							if(!labels.contains(stok.sval)){
//								labels.add(stok.sval);
//							}
//							if(!ld.contains(stok.nval)){
//								ld.add(stok.nval);
//								if(stok.nval > 4 && stok.nval < 5){
//									System.out.print(
//											counter + " " + entry.data.length + " " + 
//											lineno + " " + stok.nval + " " + stok.sval + "\n" +
//											entry.toString());
//								}
//							}
//							
//							entry.label = getLabelId(stok.sval);
//							entries.add(entry);
//							
//							
//							while (stok.ttype != StreamTokenizer.TT_EOL &&
//									stok.ttype != StreamTokenizer.TT_EOF) 
//								stok.nextToken();
//							break;
//						}
//						
//						stok.nextToken();
//					}
//					
//					if(counter != this.numFeatures){
//						this.reset();
//						throw new RuntimeException("line:" + stok.lineno() + " num of data not match.");
//					}
//					
//					this.numEntries++;
//				}
//			}
//			
//			stok.nextToken();	      
//		}			
//	    for(int i=0;i<ld.size();i++){
//	    	utils.log(ld.get(i).toString());
//	    }
//	    lnr.close();
//
//		return this.numEntries;
//	}
	
//	public String toString(){
//		StringBuilder sb = new StringBuilder();
//		
//		sb.append("[Dataset: " + fname + "]\n");
//		sb.append(numEntries + " entries, " + numFeatures + " features, " +
//				LvqUtils.getLabels().size() + " classes");
//		return sb.toString();
//	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i < size(); i++){
			sb.append("Label: " + entries.get(i).label + "\n");
			sb.append(entries.get(i).toString() + "\n");
		}
		
		return sb.toString();
	}
	
	@Override
	public Iterator<Entry> iterator() {
		final EntryIterator it = new EntryIterator();
		return it;
	}
	
	/* WRITE TO FILE ROUTINE */
	public int save(String fname){
		return save(fname, false);
	}
	/**
	 * always overwrite existing fname file
	 */
	public int save(String fname, boolean append){
		FileWriter fw = null;
		BufferedWriter bw = null; 
		int numOfLine = 0;
		
		if(this.entries == null) {
			throw new RuntimeException("empty dataset");
		}
		
		try {
			fw = new FileWriter(fname, append);
			bw = new BufferedWriter(fw);

			//write number of features
			bw.write(String.valueOf(this.numFeatures));
			bw.newLine();
			
			//iterate entries
			for(Entry e: this.entries){
				for(int i = 0;i < this.numFeatures;i++){
					String val = String.valueOf(e.data[i]);
					bw.write(val + ",");
				}
				//bw.write(LvqUtils.getLabel(e.label));
				bw.write(String.valueOf(e.label));
				bw.newLine();
				
				numOfLine++;
			}
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		} finally {
			try {
				if(bw != null) {
					bw.flush();
					bw.close();
				}
				
				if(fw != null){
					fw.close();
				}
				
			} catch (IOException e) {
				throw new RuntimeException(e.toString());
			}
		}
		
		return numOfLine;
	}
}
