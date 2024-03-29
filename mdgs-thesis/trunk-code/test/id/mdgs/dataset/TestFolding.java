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
	
//	@Test
	public void testKFoldedDataset() {
		Dataset trainset = new Dataset(Parameter.DATA_IRIS[0]);
		Dataset tmp = new Dataset(Parameter.DATA_IRIS[1]);
		trainset.load();
		tmp.load();
		
		trainset.join(tmp);
		int K = 10;
		KFoldedDataset<Dataset, Entry> kfold = new KFoldedDataset<Dataset, Entry>(trainset, K, 0.9d, false);

		for(int i=0;i < kfold.size();i++){
			FoldedDataset<Dataset, Entry> set = kfold.get(i);
			
			System.out.println("Fold " + i);
			for(int j=0;j < set.size();j++)
				System.out.println(set.folded.get(j) + "," + set.get(j));
		}
		System.out.println();
		
		FoldedDataset<Dataset, Entry> train = kfold.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> test = kfold.getKFoldedForTest(0);
		System.out.println("Train " + 0);
		for(int j=0;j < train.size();j++){
			System.out.println(train.folded.get(j) + "," + train.get(j));
		}
		System.out.println();
		
		System.out.println("Test " + 0);
		for(int j=0;j < test.size();j++){
			System.out.println(test.folded.get(j) + "," + test.get(j));
		}
		
//		for(int i=0;i<kfold.size(); i++){
//			FoldedDataset<Dataset, Entry> ds = kfold.get(i);
//			
//			for(int j=0;j < ds.size();j++)
//				System.out.print(ds.folded.get(j) + ",");
//			
//			System.out.println();
//			
//			for(Entry en: ds){
//				System.out.print(en.label + ",");
//			}
//			System.out.println();
//			System.out.println();
//		}
		
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testKFoldedDataset2() {
		Dataset trainset = new Dataset(Parameter.DATA_IRIS[0]);
		Dataset tmp = new Dataset(Parameter.DATA_IRIS[1]);
		trainset.load();
		tmp.load();
		
		trainset.join(tmp);
		int K = 2;
		KFoldedDataset<Dataset, Entry> kfold = new KFoldedDataset<Dataset, Entry>(trainset, K, 0.5d, true);

		FoldedDataset<Dataset, Entry> train = kfold.getKFoldedForTrain(0);
		FoldedDataset<Dataset, Entry> test = kfold.getKFoldedForTest(0);
		
		int iter = 0;
		for(Entry e: train){
			System.out.print(train.folded.get(iter) + "\t");
			System.out.println(e.toString());
			iter++;
		}
		System.out.println("Test");
		iter = 0;
		for(Entry e: test){
			System.out.print(test.folded.get(iter) + "\t");
			System.out.println(e.toString());
			iter++;
		}
//		KFoldedIterator kfit = kfold.iterator();
//		int iteration = 0;
//		while(kfit.hasNext()){
//			FoldedDataset<Dataset, Entry> train = kfit.nextTrain();
//			FoldedDataset<Dataset, Entry> test  = kfit.nextTest();
//			kfit.next();
//			
//			System.out.println("Iteration " + (iteration + 1));
//			System.out.println("Train " + (iteration + 1));
//			for(int j=0;j < train.size();j++)
//				System.out.println(train.folded.get(j) + "," + train.get(j));
//			
//			System.out.println();
//			System.out.println("TEST " + (iteration + 1));
//			for(int j=0;j < test.size();j++)
//				System.out.println(test.folded.get(j) + "," + test.get(j));
//			
//			iteration++;
//		}
		
		System.out.println();
	}
}
