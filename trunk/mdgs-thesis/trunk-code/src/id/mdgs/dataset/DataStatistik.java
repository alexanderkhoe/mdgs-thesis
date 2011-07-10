package id.mdgs.dataset;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.dataset.HitList.HitEntry;

public class DataStatistik {
	DatasetProfiler dp;
	
	
	public DataStatistik(){
		dp = new DatasetProfiler();
	}
	
	public void setData(Dataset dset){
		dp.reset();
		dp.run(dset);
	}
	
	public void setData(FCodeBook dset){
		dp.reset();
		dp.run(dset);
	}
	
	public void setData(FoldedDataset<Dataset, Entry> dset){
		dp.reset();
		dp.run(dset);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int total = 0;
		
		sb.append(String.format("#Class\t Freq\n"));
		for(PEntry pe: dp){
			sb.append(String.format("%2d\t %d\n", pe.label, pe.size()));
			total += pe.size();
		}
		sb.append(String.format("Total\t %d\n", total));
		
		return sb.toString();
	}
}
