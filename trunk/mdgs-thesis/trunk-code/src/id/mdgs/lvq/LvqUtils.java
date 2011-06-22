/**
 * 
 */
package id.mdgs.lvq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.HitList;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.dataset.HitList.HitEntry;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class LvqUtils {
	/**/
	private static List<String> labels = new ArrayList<String>();
	
	/* LABEL ROUTINE*/
	public static int getLabelId(String label){
		if(!LvqUtils.labels.contains(label)){
			LvqUtils.labels.add(label);
			return LvqUtils.labels.size() - 1;
		}
		
		return LvqUtils.labels.indexOf(label);
	}
	
	public static String getLabel(int id){
		if(id >= LvqUtils.labels.size()){
			return null;
		}
		
		return LvqUtils.labels.get(id);
	}
	
	public static List<String> getLabels(){
		return LvqUtils.labels;
	}
	
	public static void resetLabels(){
		LvqUtils.labels.clear();
	}
	
	/*WINNER INFO*/
	public static class WinnerInfo {
		public Entry winner;
		public double coef;
		public WinnerInfo(){
			reset();
		}
		
		public void reset(){
			winner = null;
			coef = Double.MAX_VALUE;
		}
		
		public void copy(final WinnerInfo wi){
			this.winner = wi.winner;
			this.coef 	= wi.coef;
		}
	}
	
	/*Fuzzy WINNERINFO*/
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
	
	public static class MinMax {
		public double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		public void setMinMax(double val){
			if(val < min) min = val;
			if(val > max) max = val;
		}
	}
	
	public static List<Entry> pickRandomCodes(Dataset data, int num){
		List<Entry> codebook = null;
		DatasetProfiler profiler = new DatasetProfiler();
		codebook = new ArrayList<Entry>();

		profiler.run(data);
		
		for(PEntry pe: profiler){
			int max = num;
			if(max > pe.size()) max = pe.size();
			int[] unique;
			
			/*find unique random index for each class*/
			unique = MathUtils.randPerm(max, pe.size());
			
			/*add codebook*/
			for(int i = 0;i < unique.length;i++){
				Entry code = data.get(pe.get(unique[i]));
				Entry foundCode = new Entry(code);
				codebook.add(foundCode);				
			}
		}
		
		return codebook;
	}
	
	public static List<Entry> pickCodesByPca(Dataset data, int num){
		List<Entry> codebook = null;
		DatasetProfiler profiler = new DatasetProfiler();
		codebook = new ArrayList<Entry>();
		
		profiler.run(data);
		
		for(PEntry pe: profiler){
			int max = num;
			if(max > pe.size()) max = pe.size();
			int[] unique;
			
			/*find unique random index for each class*/
			unique = MathUtils.randPerm(max, pe.size());
			
			/*add codebook*/
			for(int i = 0;i < unique.length;i++){
				Entry code = data.get(pe.get(unique[i]));
				Entry foundCode = new Entry(code);
				codebook.add(foundCode);				
			}
		}
		
		return codebook;
	}
	/**
	 * 
	 * @param data	Source where codevector is looked
	 * @param num	max number of code to find
	 * @param label label of code
	 * @return
	 */
	public static List<Entry> pickKnownCodes(Dataset data, int num, int label){
		List<Entry> codebook = null;
		
		codebook = new ArrayList<Entry>();
		
		Iterator<Entry> eIt = data.iterator();
		while (num > 0 && eIt.hasNext()){
			Entry code = eIt.next();
			if(code.label == label){
				Entry foundCode = new Entry(code.size());
				foundCode.copy(code);
				codebook.add(foundCode);
				
				num--;				
			}
		}
		
		return codebook;
	}
	
	/**
	 * 
	 * @param classes 	Contain info about number of codevector need to find for each class.
	 * @param data 		Source where codevector is looked
	 * @param knn		number of k in knn
	 * @return
	 */
	public static List<Entry> pickInsideCodes(HitList classes, Dataset data, int knn){
		List<Entry> codebook = null;
		int total = 0;
		
		//count total of codevector
		for(HitEntry he: classes){
			total += he.freq;
		}
		
		//create container
		codebook = new ArrayList<Entry>();
		
		Iterator<Entry> eIt = data.iterator();
		while(total > 0 && eIt.hasNext()){
			Entry code = eIt.next();
			HitEntry he = classes.findHit(code.label);

			if(he != null) {
				if(he.freq > 0) {
					if( correctByKnn(data, code, knn) ){
						Entry foundCode = new Entry(code.size());
						foundCode.copy(code);
						codebook.add(foundCode);
						
						total--;
						he.freq--;
					}
				}
			}
		}
		
		return codebook;
	}
	
	protected static boolean correctByKnn(Dataset data, Entry code, int knn){
		WinnerInfo[] winner;
		HitList hits;
		
		if(knn < 1) knn = 1;
		winner = LvqUtils.findWinner(data, code, knn);
		if(winner == null){
			return false;
		}
			
		if(winner.length != knn){
			winner = null;
			return false;
		}
		
		hits = new HitList();
		
		for(int i = 0; i < knn; i++){
			hits.addHit(winner[i].winner.label);
		}
		
		if(hits.size() > 0){
			if(hits.get(0).label == code.label){
				winner = null;
				return true;
			}
		}
		
		return false;
	}
	
	public static WinnerInfo findWinner(Dataset codes, Entry e){
		WinnerInfo[] result;
		
		result = LvqUtils.findWinner(codes, e, 1);
		if(result != null){
			return result[0];
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param codes
	 * @param sample
	 * @param knn
	 * @return
	 */
	public static WinnerInfo[] findWinner(Dataset codes, Entry sample, int knn){
		WinnerInfo[] winner;
		int i;
		
		winner = new WinnerInfo[knn];
		for(i=0;i < knn;i++){
			winner[i] = new WinnerInfo();
		}
		
		Iterator<Entry> eIt = codes.iterator();
		while(eIt.hasNext()) {
			double difference = 0;
			Entry code = eIt.next();
			
//			for(i=0;i < codes.numFeatures; i++){
//				double diff = sample.data[i] - code.data[i];
//				difference += diff * diff;
//				
////				if(difference > winner[knn-1].coef)
////					break;
//			}
//			
//			difference = Math.sqrt(difference);
//			difference = MathUtils.euclideDistance(sample.data, code.data);
			difference = MathUtils.squaredEuclideDistance(sample.data, code.data);
			
			
			for(i=0; (i < knn) && (difference > winner[i].coef); i++);
			
			if(i < knn){
				for(int j=knn - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = difference;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}
	
	public static double cosineSimilarity(Entry code, Entry sample){
		double dot = 0;
		double lengthCode = 0;
		double lengthSample = 0;
		
		for(int i=0;i < code.data.length; i++) {
			dot += code.data[i] * sample.data[i];
			
			lengthCode += code.data[i] * code.data[i];
			lengthSample += sample.data[i] * sample.data[i];
		}
			
		lengthCode = Math.sqrt(lengthCode);
		lengthSample = Math.sqrt(lengthSample);
		
		
		return dot / (lengthCode * lengthSample); 
	}
	
	/*Using cosine similarity*/
	public static WinnerInfo[] findWinnerByCosineSimilarity(
			Dataset codes, Entry sample, int knn){
		WinnerInfo[] winner;
		int i;
		
		winner = new WinnerInfo[knn];
		for(i=0;i < knn;i++){
			winner[i] = new WinnerInfo();
			/*cosine similarity range between -1 .. 1 
			 * set init coef = -1 */
			winner[i].coef = -1;
		}
		
		Iterator<Entry> eIt = codes.iterator();
		while(eIt.hasNext()) {
			double similarity = 0;
			Entry code = eIt.next();
			
			similarity = cosineSimilarity(code, sample);
			
			for(i=0; (i < knn) && (similarity < winner[i].coef); i++);
			
			if(i < knn){
				for(int j=knn - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = similarity;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}	
}
