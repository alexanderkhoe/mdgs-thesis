/**
 * 
 */
package id.mdgs.fnlvq;


import id.mdgs.lvq.Dataset.Entry;

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
public class FCodeBook implements Iterable<FCodeBook.FEntry>{
	/**/
	public static class FWinnerInfo {
		public FEntry winner;
		public Entry miu;
		public double coef;
		public FWinnerInfo(){
			reset();
		}
		
		public void reset(){
			winner = null;
			miu  = null;
			coef = Double.MAX_VALUE;
		}
		
		public void copy(final FWinnerInfo wi){
			this.winner = wi.winner;
			this.miu = wi.miu;
			this.coef 	= wi.coef;
		}
	}
	
	/*Dataset FEntry*/
	public static class FEntry {
		public FuzzyNode[] data;
		public int label;
		
		public  FEntry(int size){
			data = new FuzzyNode[size];
			for(int i=0;i < data.length;i++)
				data[i] = new FuzzyNode();
		}
		
		public FEntry(FEntry e) {
			this(e.size());
			this.copy(e);
		}
		
		public FEntry clone() {
			return new FEntry(this);
		}
		
		public void set(double val){
		}
		
		public int size(){
			return data.length;
		}
		
		public void copy(FEntry e){
			for(int i=0;i < data.length;i++)
				this.data[i].copy(e.data[i]);
			
			this.label = e.label;
		}
		
		public String toString(){
			StringBuilder sbmin  = new StringBuilder();
			StringBuilder sbmean = new StringBuilder();
			StringBuilder sbmax  = new StringBuilder();
			for(int i=0;i<data.length;i++){
				sbmin.append(String.format("%7.4f",data[i].min));
				sbmean.append(String.format("%7.4f",data[i].mean));
				sbmax.append(String.format("%7.4f",data[i].max));
				if(i != data.length - 1){
					sbmin.append(",");
					sbmean.append(",");
					sbmax.append(",");
				}
			}
			return (sbmin.toString()  + "\n" +
					sbmean.toString() + "\n" +
					sbmax.toString()  + "\n");
		}
	}
	
	/*Entry Iterator*/
	public class EntryIterator implements Iterator<FEntry>{
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return currentIndex < FCodeBook.this.entries.size();
		}

		@Override
		public FEntry next() {
			if (!hasNext()) {
				return null;
			}
			
			return FCodeBook.this.entries.get(currentIndex++);
		}

		@Override
		public void remove() {
			throw new RuntimeException("Called remove, unsupported operation.");
		}
		
		public void rewind(){
			currentIndex = 0;
		}
	}
	
	/*Start Dataset variable and routine*/
	public int numFeatures;
	public int numEntries;
	public List<FEntry>	  entries;	
	public String fname;
	public String delimiter = ",;|\t";
	
	
	public FCodeBook(){
		setFilename(null);
		this.entries 	= new ArrayList<FEntry>();
		
		reset();
	}
	
	public FCodeBook(String fname){
		setFilename(fname);
		this.entries 	= new ArrayList<FEntry>();
		
		reset();
	}
	
	public FCodeBook(FCodeBook ds){
		setFilename(ds.fname);
		this.entries 	= new ArrayList<FEntry>();
		reset();
		
		copyInfo(ds);
		for(FEntry e: ds.entries) {
			this.add(e.clone());
		}
	}
	
	public FCodeBook clone(){
		return new FCodeBook(this);
	}
	
	public void set(FCodeBook ds){
		/*assume property are the same, except entry value*/
		if(this.numEntries != ds.numEntries)
			throw new RuntimeException("number of entry not match");
		if(this.numFeatures != ds.numFeatures)
			throw new RuntimeException("dimension not match");
		
		for(int pos=0;pos < this.size();pos++)
			this.get(pos).copy(ds.get(pos));
	}
	
	/*for codebook, not for others*/
	public FEntry findEntry(int label) {
		for(FEntry code: this.entries){
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
	
	public void copyInfo(FCodeBook ds){
		this.numFeatures = ds.numFeatures;
	}
	
	public int size(){
		return this.entries.size();
	}
	
	/* DATA ROUTINE */
	public void setFilename(String fname){
		this.fname = fname;
	}
	
	public void add(FEntry e){
		this.entries.add(e);
		this.numEntries++;
	}
	
	public void addAll(List<FEntry> es){
		for(FEntry e: es)
			add(e);
	}
	
	public FEntry get(int pos) {
		if(pos >= size()){
			throw new RuntimeException(
					String.format("[Dataset] Pos (%d) exceed number of entries (%d)", 
							pos, size())
					);
		}
		
		return this.entries.get(pos);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i < size(); i++){
			sb.append("Label: " + entries.get(i).label + "\n");
			sb.append(entries.get(i).toString() + "\n");
		}
		
		return sb.toString();
	}
	
//	public int load(){
//		if(this.fname == null)
//			throw new RuntimeException("[Dataset] Filename not defined yet.");
//		
//		return load(this.fname);
//	}
	
//	public int load(String fname) {
//		FileReader fr = null;
//		BufferedReader br = null; 
//		String currLine;
//		
//		//reset 
//		this.reset();
//		
//		try {
//			
//			fr = new FileReader(fname);
//			br = new BufferedReader(fr);
//			
//			int lineno = 0;
//			while((currLine = br.readLine()) != null){
//				lineno++;
//				
//				if(lineno == 1){
//					StringTokenizer stok = new StringTokenizer(currLine, delimiter);
//					if(stok.hasMoreTokens()){
//						this.numFeatures = Integer.parseInt(stok.nextToken());
//						this.numEntries = 0;
//					} 
//				}
//				
//				else {
//					StringTokenizer stok = new StringTokenizer(currLine, delimiter);
//					Entry entry = new Entry(this.numFeatures);
//					int counter = 0;
//					while (stok.hasMoreTokens()) {
//						if (counter < this.numFeatures){
//							String sval = stok.nextToken();
//							double dval = Double.parseDouble(sval);
//							entry.data[counter++] = dval;
//						} else {
//							String label = stok.nextToken();
//							
//							entry.label = LvqUtils.getLabelId(label);
//							add(entry);
//							
//							break;
//						}
//					}
//					
//					if(counter != this.numFeatures){
//						this.reset();
//						throw new RuntimeException("line:" + lineno + " num of data not match.");
//					}
//				}
//			}
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e.toString());
//		} catch (NumberFormatException e) {
//			throw new RuntimeException(e.toString());
//		} catch (IOException e) {
//			throw new RuntimeException(e.toString());
//		} finally {
//				try {
//					if(br != null) br.close();
//					if(fr != null) fr.close();
//				} catch (IOException e) {
//					throw new RuntimeException(e.toString());
//				}
//		}
//		
//		return numEntries;
//	}
	
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

	@Override
	public Iterator<FEntry> iterator() {
		final EntryIterator it = new EntryIterator();
		return it;
	}
	
	/* WRITE TO FILE ROUTINE */
//	public int save(String fname){
//		return save(fname, false);
//	}
//	/**
//	 * always overwrite existing fname file
//	 */
//	public int save(String fname, boolean append){
//		FileWriter fw = null;
//		BufferedWriter bw = null; 
//		int numOfLine = 0;
//		
//		if(this.entries == null) {
//			throw new RuntimeException("empty dataset");
//		}
//		
//		try {
//			fw = new FileWriter(fname, append);
//			bw = new BufferedWriter(fw);
//
//			//write number of features
//			bw.write(String.valueOf(this.numFeatures));
//			bw.newLine();
//			
//			//iterate entries
//			for(Entry e: this.entries){
//				for(int i = 0;i < this.numFeatures;i++){
//					String val = String.valueOf(e.data[i]);
//					bw.write(val + ",");
//				}
//				bw.write(LvqUtils.getLabel(e.label));
//				bw.newLine();
//				
//				numOfLine++;
//			}
//		} catch (IOException e) {
//			throw new RuntimeException(e.toString());
//		} finally {
//			try {
//				if(bw != null) {
//					bw.flush();
//					bw.close();
//				}
//				
//				if(fw != null){
//					fw.close();
//				}
//				
//			} catch (IOException e) {
//				throw new RuntimeException(e.toString());
//			}
//		}
//		
//		return numOfLine;
//	}
}