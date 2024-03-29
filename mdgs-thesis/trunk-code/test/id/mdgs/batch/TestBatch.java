package id.mdgs.batch;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.master.IClassify;
import id.mdgs.utils.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class TestBatch {
	
	public static ConfusionMatrix doTest(IClassify<?, Entry> net, FoldedDataset<?, Entry> data, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		for(Entry sample: data){
			int win = -1, target;
			
			target = sample.label;
			win = net.classify(sample);
			
			cm.feed(win, target);
		}
		
		return cm;
	}
	
	public static PrintWriter createWriter(String tag) throws IOException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		return new PrintWriter(new BufferedWriter(new FileWriter(
				utils.getDefaultPath() + "/resources/report.v2/resume." + tag + "." +
				String.format(dateFormat.format(new Date())) + "." + 
				TestLvq.class.getSimpleName(), false)));	
	}
	
	public static  void closeWriter(PrintWriter writer){
		if(writer != null){
			writer.flush();
			writer.close();
		}		
	}
	
	public static ConfusionMatrix test1(Dataset codebook, Dataset testset, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		Lvq net = new Lvq();
		net.codebook.copyInfo(codebook);
		net.codebook.addAll(codebook.entries);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
		}
		
		return cm;
	}
	
	public static ConfusionMatrix test2(FCodeBook codebook, Dataset testset, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		Fpglvq net = new Fpglvq();
		net.codebook.copyInfo(codebook);
		net.codebook.addAll(codebook.entries);
		
		Iterator<Entry> it = testset.iterator();
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			
			cm.feed(win, target);
		}
		
		return cm;
	}
	
	public static <T> ConfusionMatrix testNetwork(IClassify<T, Entry> net, T codebook, FoldedDataset<?, Entry> testset, int numclass){
		ConfusionMatrix cm = new ConfusionMatrix(numclass);
		
		net.loadCodebook(codebook);
		Iterator<Entry> it = testset.iterator(); 
		
		int i = 0;
		while(it.hasNext()){
			Entry sample = it.next(); 
			
			int win = net.classify(sample);
			int target = sample.label;
			cm.feed(win, target);
			i++;
//			System.out.println(i);
		}		
		
		return cm;
	}
}
