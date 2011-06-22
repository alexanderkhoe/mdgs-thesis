package id.mdgs.dataset;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.stat.descriptive.MultivariateSummaryStatistics;

import id.mdgs.dataset.Dataset.Entry;

public class ZScoreNormalization extends DataNormalization {
	public Entry mean;
	public Entry std;
	
	public ZScoreNormalization(Dataset dset) {
		super(dset);
		
		mean = new Entry(dset.numFeatures);
		std  = new Entry(dset.numFeatures);
		calcParameter();
	}

	private void calcParameter(){
		/*calculate mean*/
		MultivariateSummaryStatistics stats = new 
			MultivariateSummaryStatistics(dset.numFeatures, true);
		
		for(Entry en: dset){
			try {
				stats.addValue(en.data);
			} catch (DimensionMismatchException e1) {
				e1.printStackTrace();
			}
		}
		
		mean = new Entry(stats.getMean());
		std  = new Entry(stats.getStandardDeviation());
	}
	
	@Override
	public void normalize(Entry sample) {
		assert(this.dset.numFeatures == sample.size());
		
		for(int i=0;i<sample.size();i++){
			sample.data[i] = normalize(sample.data[i], mean.data[i], std.data[i]);
		}
	}

	public double normalize(double x, double mean, double std){
		return (x - mean) / std;
	}
	
	@Override
	public double normalize(double x, double dataLow, double dataHigh,
			double normLow, double normHigh) {
		throw new RuntimeException("Not Supported");
	}
	
	

}
