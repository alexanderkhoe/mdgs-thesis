package id.mdgs.dataset;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.utils.Parameter;

import org.junit.Test;

public class TestFoldedDataset {
	
	@Test
	public void testFolded() {
		Dataset trainset = new Dataset(Parameter.DATA_IRIS[0]);
		trainset.load();
		
		FoldedDataset<Dataset, Entry> fd = new FoldedDataset<Dataset, Entry>(trainset);
		fd.makeRoundRobin(5);
		
		for(int i=0;i < fd.folded.size();i++)
			System.out.print(fd.folded.get(i) + ",");
		
		System.out.println();
		
		for(Entry en: fd){
			System.out.print(en.label + ",");
		}
	}
}
