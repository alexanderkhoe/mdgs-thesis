package id.mdgs.dataset;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.MinMax;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */

public class DataNormalization {
	
	public Dataset dset;
	private double normLow;
	private double normHigh;
	private MinMax[] minMax; 
	
	public DataNormalization(Dataset dset){
		this(dset, 0, 1);
	}
	
	public DataNormalization(Dataset dset, double normLow, double normHigh){
		this.dset = dset;
		this.normLow = normLow;
		this.normHigh= normHigh;
		
		this.minMax	= new MinMax[dset.numFeatures];
		for(int i=0;i<minMax.length;i++) minMax[i] = new MinMax();
		
		calcParameter();
	}
	
	private void calcParameter(){
		for(Entry en: dset){
			for(int i=0;i<en.size();i++){
				minMax[i].setMinMax(en.data[i]);
			}
		}
	}
	
	public void normalize(Dataset dset){
		assert(this.dset.numFeatures == dset.numFeatures);
		for(Entry en: dset){
			normalize(en);
		}
	}
	
	public void normalize(Entry sample){
		assert(this.dset.numFeatures == sample.size());
		
		for(int i=0;i<sample.size();i++){
			sample.data[i] = normalize(sample.data[i], minMax[i].min, minMax[i].max, normLow, normHigh);
		}
	}
	
	public double normalize(double x, double dataLow, double dataHigh, double normLow, double normHigh) {
		return ((x - dataLow) / (dataHigh - dataLow))
				* (normHigh - normLow) + normLow;
	}
}
