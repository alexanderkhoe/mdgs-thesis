package id.mdgs.dataset;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset.KFoldedIterator;
import id.mdgs.utils.Parameter;

import org.junit.Test;

public class TestFolding {
	
//	@Test
	public void testFoldedDataset() {
		Dataset trainset = new Dataset(Parameter.DATA_IRIS[0]);
		trainset.load();
		
		FoldedDataset<Dataset, Entry> fd = new FoldedDataset<Dataset, Entry>(trainset, true);
		fd.makeRoundRobin(5);
		
		for(int i=0;i < fd.size();i++)
			System.out.print(fd.folded.get(i) + ",");
		
		System.out.println();
		
		for(Entry en: fd){
			System.out.print(en.label + ",");
		}
	}
	
	@Test
	public void testKFoldedDataset() {
		Dataset trainset = new Dataset(Parameter.DATA_IRIS[0]);
		Dataset tmp = new Dataset(Parameter.DATA_IRIS[1]);
		trainset.load();
//		tmp.load();
		
//		trainset.join(tmp);
		int K = 10;
		KFoldedDataset<Dataset, Entry> kfold = new KFoldedDataset<Dataset, Entry>(trainset, K, 0.9d, false);


		for(int i=0;i<kfold.size(); i++){
			FoldedDataset<Dataset, Entry> ds = kfold.get(i);
			
			for(int j=0;j < ds.size();j++)
				System.out.print(ds.folded.get(j) + ",");
			
			System.out.println();
			
			for(Entry en: ds){
				System.out.print(en.label + ",");
			}
			System.out.println();
			System.out.println();
		}
		
//		KFoldedIterator it = kfold.iterator();
//		while(it.hasNext()){
//			FoldedDataset<Dataset, Entry> train = it.nextTrain();
//			FoldedDataset<Dataset, Entry> test = it.nextTest();
//			it.next();
//			
//			System.out.println("Train");
//			for(int i=0;i < train.size();i++)
//				System.out.print(train.folded.get(i) + ",");
//			
//			System.out.println();
//			
//			for(Entry en: train){
//				System.out.print(en.label + ",");
//			}
//
//			System.out.println();
//			
//			System.out.println("Test");
//			for(int i=0;i < test.size();i++)
//				System.out.print(test.folded.get(i) + ",");
//			
//			System.out.println();
//			
//			for(Entry en: test){
//				System.out.print(en.label + ",");
//			}
//
//			System.out.println();
//			System.out.println();
//		}
		
	}
}
