package id.mdgs.glvq.mglvq;

import java.util.Iterator;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.descriptive.MultivariateSummaryStatistics;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.master.WinnerFunction;
import id.mdgs.utils.MathUtils;

public class WinnerByMahalanobis implements WinnerFunction {

	public static class MParam {
		public RealMatrix invS;
		public Entry mean;
		public Entry std;
		public double threshold;
		public MParam(){
			threshold = 0;
		}
	}
	
	public MParam mps;
	
	public WinnerByMahalanobis(MParam mps){
		this.mps = mps;
	}
	
	@Override
	public WinnerInfo function(Dataset codes, Entry sample) {
		WinnerInfo[] result;
		
		result = this.function(codes, sample, 1);
		if(result != null){
			return result[0];
		}
		
		return null;
	}

	@Override
	public WinnerInfo[] function(Dataset codes, Entry sample, int num) {
		WinnerInfo[] winner;
		int i;
		winner = new WinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new WinnerInfo();
		}
		
		Iterator<Entry> eIt = codes.iterator();
		while(eIt.hasNext()) {
			double difference = 0;
			Entry code = eIt.next();
			
			difference = getDistance(normalize(sample, code.label), code);
			
			for(i=0; (i < winner.length) && (difference > winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = difference;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}

	public double getDistance(Entry sample, Entry code){
		assert(mps != null);
		
		RealMatrix inv = mps.invS;
		return MathUtils.mahalanobisDistance(sample.data, code.data, inv);
	}
	
	/**
	 * 
	 * @param sample
	 * @param codelabel , not used anymore
	 * @return
	 */
	public Entry normalize(Entry sample, int codelabel){
		assert(mps != null);
		
		Entry result = new Entry(sample.size());
		for(int i=0;i < sample.size();i++){
			result.data[i] = (sample.data[i] - mps.mean.data[i]) / mps.std.data[i];
		}
		
		return result;
	}
	
//	/******************************************/
	public static MParam normalizeData(Dataset dataset){
		return WinnerByMahalanobis.normalizeData(new FoldedDataset<Dataset, Entry>(dataset));
	}
	
	public static MParam normalizeData(FoldedDataset<Dataset, Entry> dataset){
		MParam mps = new MParam();
		
		/*calculate mean*/
		MultivariateSummaryStatistics stats = new 
			MultivariateSummaryStatistics(dataset.getMasterData().numFeatures, true);
		
		for(Entry e : dataset){
			try {
				stats.addValue(e.data);
			} catch (DimensionMismatchException e1) {
				e1.printStackTrace();
			}
		}
		
		mps.mean = new Entry(stats.getMean());
		mps.std  = new Entry(stats.getStandardDeviation());
		stats.clear();
		
//		/*normalize data*/
		for(Entry e : dataset){
			for(int j=0;j < e.size();j++){
				e.data[j] = (e.data[j] - mps.mean.data[j]) / mps.std.data[j];
			}
		}
		
		//hitung ulang untuk mencari covariance
		for(Entry e : dataset){
			try {
				stats.addValue(e.data);
			} catch (DimensionMismatchException e1) {
				e1.printStackTrace();
			}
		}
		
		RealMatrix cov = stats.getCovariance();
		/*regularisasi - buat memastikan non singular*/
		double eps = 0.00000001d;
		for(int i=0;i < cov.getRowDimension();i++){
			for(int j=0;j < cov.getColumnDimension();j++){
				if(i == j) {
					cov.addToEntry(i, j, eps);
				}
			}
		}
//		System.out.println(cov.toString());
		mps.invS = new LUDecompositionImpl(cov).getSolver().getInverse();

		return mps;
	}

}
