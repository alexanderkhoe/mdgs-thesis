package id.mdgs.dataset;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils;
import id.mdgs.utils.utils;

import java.io.IOException;

import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.junit.Test;


public class TestDataset {
	
	@Test
	public void testDataset() throws IOException{
		LvqUtils.resetLabels();
		Dataset dataset = new Dataset(utils.getDefaultPath() + "/resources/trash/sample.dat");
		dataset.load();
		
		for(int i=0;i<dataset.numEntries;i++){
			Entry entry = dataset.entries.get(i);
			for(int j=0;j<entry.size();j++){
				 
				System.out.print(entry.data[j]);
				if(j == entry.size()-1)
					System.out.print(LvqUtils.getLabel(entry.label));
			}
		
			System.out.println();
		}
	}
	
	@Test
	public void testLotData() throws IOException{
		LvqUtils.resetLabels();
		Dataset data = new Dataset(utils.getDefaultPath() + "/resources/trash/wlet.train86.txt");
		
		long start = System.currentTimeMillis();
		data.load();
		utils.log(data.toString());
		long diff = System.currentTimeMillis() - start;
		utils.log(Long.toString(diff));
	}
	
	@Test
	public void testEncogRead(){
		long start = System.currentTimeMillis();
		NeuralDataSet data = new CSVNeuralDataSet(utils.getDefaultPath() + "/resources/ecgdata/wlet.train86.txt",
							86, 1, false, new CSVFormat('.','\t'));
		long diff = System.currentTimeMillis() - start;
		utils.log(Long.toString(diff));
		
	}
	
	@Test
	public void testDouble(){
		double n = 4.5998e-005;
		
		System.out.print(n);
		
		n = Double.parseDouble("4.5998e-005");
		
		System.out.print(n);
	}
}
