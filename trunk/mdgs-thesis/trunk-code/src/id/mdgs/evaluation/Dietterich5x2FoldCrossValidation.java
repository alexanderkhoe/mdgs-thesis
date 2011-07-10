/**
 * 
 */
package id.mdgs.evaluation;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.KFoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.KFoldedDataset.KFoldedIterator;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Dietterich5x2FoldCrossValidation {

	class Pair {
		public String name;
		public IClassify<?, Entry> net;
		public ITrain trainer;
		public String tag;
		
		public void set(String name,IClassify<?, Entry> net, ITrain trainer){
			this.name	= name;
			this.net 	= net;
			this.trainer= trainer;
			
			//save initial codebook to file
			tag = name + Integer.toString(net.hashCode());
			this.net.saveCodebook(this.tag); //ini untuk KFoldCrossValidation
		}
	};

	class  KStatistic {
		public List<double[]> table;
		public List<double[]> errTable;
		
		public KStatistic(){
			table = new ArrayList<double[]>();
			errTable = new ArrayList<double[]>();
		}
		
		public void reset(){
			table.clear();
			errTable.clear();
		}
		
		public void add(List<double[]> tbl, double[] p){
			double pA1 = p[0];
			double pB1 = p[1];
			double pA2 = p[2];
			double pB2 = p[3];
			double p1  = pA1 - pB1;
			double p2  = pA2 - pB2;
			
			double[] prow = new double[8];
			prow[0] 	= pA1;
			prow[1] 	= pB1;
			prow[2] 	= pA2;
			prow[3] 	= pB2;
			prow[4] 	= p1;
			prow[5] 	= p2;
			prow[6] 	= (p1 + p2) / 2;
			prow[7] 	= 0;	//s2
			
			tbl.add(prow);
		}
		
		//reference to Combining pattern classifier p19
		public double getT(){
			
			//calc S2
			for(int i=0;i < table.size();i++){
				double p1 = table.get(i)[4];
				double p2 = table.get(i)[5];
				double pbar = table.get(i)[6];
				table.get(i)[7] = Math.pow(p1 - pbar, 2) + Math.pow(p2 - pbar, 2);
			}
				
			double sum = 0;
			for(int i=0;i < table.size();i++)
				sum += table.get(i)[7];
			
			sum /= table.size();
			sum = Math.sqrt(sum);
			
			double p11 = table.get(0)[4];
			double t = p11 / sum;
			return t;
		}
	};
	
	private List<Pair> pairs;
	public KFoldedDataset<Dataset, Entry> masterdata;
	KStatistic statistic;
	public String logFName;
	public int K;
	public double portion;
	public boolean random;
	public int ATTEMPT = 5; 
	
	public Dietterich5x2FoldCrossValidation(Dataset dataset, boolean random, String logname) {
		this.K = 2;
		this.portion = 0.5d;
		this.random	= random;
		
		masterdata = new KFoldedDataset<Dataset, Entry>(dataset, K, portion, random);
		this.pairs = new ArrayList<Pair>();
		this.statistic = new KStatistic();
		this.logFName = logname;
	}
	
	public void reset(){
		this.pairs.clear();
	}
	
	public void registerClassifier(IClassify<?, Entry> net, ITrain trainer){
		if(this.pairs.size() == 2){
			throw new RuntimeException("Can only handle 2 classifier");
		}
		
		//init bobot awal, harus sama untuk semua K iterasi cross validation,
		//bobot akan disimpan diawal
		// TODO pending dulu
//		if(net instanceof Lvq) ((Lvq) net).initCodes(trainer, 1);
//		else if(net instanceof Fnlvq) ((Fnlvq) net).initCodes(trainer, 0.5d, true);

		
		Pair pair = new Pair();
		pair.set(trainer.getClass().getSimpleName(), net, trainer);
		pairs.add(pair);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double run(){
		if(this.pairs.size() < 2){
			throw new RuntimeException("Need 2 classifier");
		}
		
		int iteration = 0;
		for(int att=0;att < ATTEMPT; att++){
			double[] pacc = new double[4];
			double[] perr = new double[4];
			MathUtils.fills(pacc, 0);
			MathUtils.fills(perr, 0);

			KFoldedIterator kfit = masterdata.iterator();
			int k = 0;
			while(kfit.hasNext()){
				FoldedDataset<Dataset, Entry> train;
				FoldedDataset<Dataset, Entry> test;
				
				train 	= kfit.nextTrain();
				test 	= kfit.nextTest();
				kfit.next();

				for(int i=0;i < pairs.size();i++){
					IClassify<?, Entry> net = pairs.get(i).net;
					ITrain trainer = pairs.get(i).trainer;
					net.loadCodebook(pairs.get(i).tag);
					
					if(net instanceof Lvq) ((Lvq) net).initCodes(train, 1);
					else if(net instanceof Fnlvq) ((Fnlvq) net).initCodes(train, 0.5d, true);
					
					trainer.reset();
					trainer.setNetwork(net);
					trainer.setTraining(train);
					
					do {
						trainer.iteration();
						perr[(2*k)+i] += trainer.getError();
					}while(!trainer.shouldStop());
					
					perr[(2*k)+i] /= trainer.getMaxEpoch(); 
						
					int TP = 0;
					for(Entry sample: test){
						int win = -1, target;
						
						target = sample.label;
						win = net.classify(sample);
						
						if(win == target) TP++;
					}
					
					pacc[(2*k)+i] = ((double) TP) / test.size();
				}
				
				k++;
			}

			iteration++;
			
			statistic.add(statistic.table, pacc);
			statistic.add(statistic.errTable, perr);
			System.out.print(String.format("K=%2d\t%7.4f\t%7.4f\t%7.4f\t%7.4f\t|\t", iteration, pacc[0], pacc[1], pacc[2], pacc[3]));
			System.out.print(String.format("%7.4f\t%7.4f\t%7.4f\t%7.4f\n", perr[0], perr[1], perr[2], perr[3]));
		}


		//delete log codebook, dipake di KFoldCross
		for(int i=0;i < pairs.size();i++){
			File f = new File(pairs.get(i).tag);
			f.delete();
		}
		
		return logAll();
	}
	
	private double logAll(){
		String sep = "\t";
		double t = 0;
		
		utils.initPath((new File(logFName)).getParent());
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File(logFName));
		
			t =  statistic.getT();
			
			pw.write(String.format("-- K FOLD CROSS VALIDATION PAIRED T-TEST -------\n"));
			pw.write(String.format("Parameter -> K:%d, train portion:%5.2f, random:%s\n",this.K, this.portion, this.random ? "true" : "false"));
//			pw.write(String.format("pA:%s, pB:%s\n", pairs.get(0).name, pairs.get(1).name));
			pw.write(String.format("DA:%s,\n DB:%s\n", pairs.get(0).trainer.information(), pairs.get(1).trainer.information()));
			pw.write(String.format("%5s%s","K",sep));
			pw.write(String.format("%7s%s","pA1",sep));
			pw.write(String.format("%7s%s","pB1",sep));
			pw.write(String.format("%7s%s","pA2",sep));
			pw.write(String.format("%7s%s","pB2",sep));
			pw.write(String.format("%7s%s","p1",sep));
			pw.write(String.format("%7s%s","p2",sep));
			pw.write(String.format("%7s%s","pbar",sep));
			pw.write(String.format("%7s%s","S2",sep));
			pw.write("\n");
	
			for(int i=0;i < statistic.table.size();i++){
				pw.write(String.format("%5s%s", "K:" + Integer.toString(i),sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[0],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[1],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[2],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[3],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[4],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[5],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[6],sep));
				pw.write(String.format("%f%s", statistic.table.get(i)[7],sep));
				pw.write("\n");
			}
			pw.write("\n");
			
			pw.write(String.format("t   : %f\n\n", t));
			pw.write(String.format("Note : have a look on the T-Table (two-tailed test),\n" +
					" degree of freedom (df) -> 5 at level of significance 0.05.\n" +
					" state z = tabulated value, if t-value < (-z) or t-value > (z) then, H0(no significance) rejected"));
			
			pw.write("\n\n");
			pw.write(String.format("-- ERROR RATE EACH CROSS -------\n"));
			pw.write(String.format("%5s%s","K",sep));
			pw.write(String.format("%7s%s","pErrA1",sep));
			pw.write(String.format("%7s%s","pErrB1",sep));
			pw.write(String.format("%7s%s","pErrA2",sep));
			pw.write(String.format("%7s%s","pErrB2",sep));
			pw.write("\n");
	
			for(int i=0;i < statistic.errTable.size();i++){
				pw.write(String.format("%5s%s", "K:" + Integer.toString(i),sep));
				pw.write(String.format("%7.4f%s", statistic.errTable.get(i)[0],sep));
				pw.write(String.format("%7.4f%s", statistic.errTable.get(i)[1],sep));
				pw.write(String.format("%7.4f%s", statistic.errTable.get(i)[2],sep));
				pw.write(String.format("%7.4f%s", statistic.errTable.get(i)[3],sep));
				pw.write("\n");
			}
			pw.write("\n");			
			pw.flush();
			pw.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return t;
	}
}
