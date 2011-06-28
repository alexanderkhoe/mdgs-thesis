package id.mdgs.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import id.mdgs.dataset.*;
import id.mdgs.dataset.KFoldedDataset.KFoldedIterator;
import id.mdgs.dataset.Dataset.*;
import id.mdgs.fnlvq.Fnlvq;
import id.mdgs.fnlvq.Fpglvq;
import id.mdgs.glvq.Glvq;
import id.mdgs.lvq.Lvq;
import id.mdgs.master.IClassify;
import id.mdgs.master.ITrain;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

public class KFoldCrossValidation {

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
		
		public void add(List<double[]> tbl, double pA, double pB){
			double[] pacc = new double[4];
			pacc[0] = pA;
			pacc[1]	= pB;
			pacc[2]	= pA - pB;
			pacc[3] = 0;
			
			tbl.add(pacc);
		}
		
		public double getPBar(){
			double pbar = 0;
			for(int i=0;i < table.size();i++)
				pbar += table.get(i)[2];
			
			return pbar / table.size();
		}
		
		//reference to Combining pattern classifier p19
		public double getT(){
			double pbar = getPBar();
			for(int i=0;i < table.size();i++)
				table.get(i)[3] = table.get(i)[2] - pbar;
			
			double sum = 0;
			for(int i=0;i < table.size();i++)
				sum += Math.pow(table.get(i)[3], 2);
			
			sum /= table.size() - 1;
			sum = Math.sqrt(sum);
			
			double t = pbar * Math.sqrt(table.size()) / sum;
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
	
	public KFoldCrossValidation(Dataset dataset, int K, double portion, boolean random, String logname) {
		masterdata = new KFoldedDataset<Dataset, Entry>(dataset, K, portion, random);
		this.pairs = new ArrayList<Pair>();
		this.statistic = new KStatistic();
		this.logFName = logname;
		this.K = K;
		
		this.portion = portion;
		this.random	= random;
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
		
		KFoldedIterator kfit = masterdata.iterator();
		int iteration = 0;
		while(kfit.hasNext()){
			FoldedDataset<Dataset, Entry> train;
			FoldedDataset<Dataset, Entry> test;
			
			train 	= kfit.nextTrain();
			test 	= kfit.nextTest();
			kfit.next();

			double[] pacc = new double[2];
			double[] perr = new double[2];
			MathUtils.fills(pacc, 0);
			MathUtils.fills(perr, 0);
			
			for(int i=0;i < pairs.size();i++){
				IClassify<?, Entry> net = pairs.get(i).net;
				ITrain trainer = pairs.get(i).trainer;
				//net.loadCodebook(pairs.get(i).tag);
				
				if(net instanceof Lvq) ((Lvq) net).initCodes(train, 1);
				else if(net instanceof Fnlvq) ((Fnlvq) net).initCodes(train, 0.5d, true);
				
				trainer.reset();
				trainer.setNetwork(net);
				trainer.setTraining(train);
				
				do {
					trainer.iteration();
					perr[i] += trainer.getError();
				}while(!trainer.shouldStop());
				
				perr[i] /= trainer.getMaxEpoch(); 
					
				int TP = 0;
				for(Entry sample: test){
					int win = -1, target;
					
					target = sample.label;
					win = net.classify(sample);
					
					if(win == target) TP++;
				}
				
				pacc[i] = ((double) TP) / test.size();
			}
			
			iteration++;
			
			statistic.add(statistic.table, pacc[0], pacc[1]);
			statistic.add(statistic.errTable, perr[0], perr[1]);
			System.out.print(String.format("K=%d\t%7.4f\t%7.4f\t%7.4f\t%7.4f\n", iteration, pacc[0], pacc[1], perr[0], perr[1]));
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
		double t = 0, pbar = 0;;
		
		utils.initPath((new File(logFName)).getParent());
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File(logFName));
		
			t =  statistic.getT();
			pbar = statistic.getPBar();
			
			pw.write(String.format("-- K FOLD CROSS VALIDATION PAIRED T-TEST -------\n"));
			pw.write(String.format("Parameter -> K:%d, train portion:%5.2f, random:%s\n",this.K, this.portion, this.random ? "true" : "false"));
			pw.write(String.format("pA:%s, pB:%s\n", pairs.get(0).name, pairs.get(1).name));
			pw.write(String.format("pA:%s,\n pB:%s\n", pairs.get(0).trainer.information(), pairs.get(1).trainer.information()));
			pw.write(String.format("%5s%s","K",sep));
			pw.write(String.format("%7s%s","pA",sep));
			pw.write(String.format("%7s%s","pB",sep));
			pw.write(String.format("%7s%s","pI",sep));
			pw.write(String.format("%7s%s","pI-Mn",sep));
			pw.write("\n");
	
			for(int i=0;i < statistic.table.size();i++){
				pw.write(String.format("%5s%s", "K:" + Integer.toString(i),sep));
				pw.write(String.format("%7.4f%s", statistic.table.get(i)[0],sep));
				pw.write(String.format("%7.4f%s", statistic.table.get(i)[1],sep));
				pw.write(String.format("%7.4f%s", statistic.table.get(i)[2],sep));
				pw.write(String.format("%7.4f%s", statistic.table.get(i)[3],sep));
				pw.write("\n");
			}
			pw.write("\n");
			
			pw.write(String.format("\npBar: %f\n", pbar));
			pw.write(String.format("t   : %f\n\n", t));
			pw.write(String.format("Note : have a look on the T-Table (two-tailed test),\n" +
					" degree of freedom (df) -> %d at level of significance 0.05.\n" +
					" state z = tabulated value, if t-value < -z or t-value > z then, H0(no significance) rejected", statistic.table.size()));
			
			pw.write("\n\n");
			pw.write(String.format("-- ERROR RATE EACH CROSS -------\n"));
			pw.write(String.format("%5s%s","K",sep));
			pw.write(String.format("%7s%s","pErrA",sep));
			pw.write(String.format("%7s%s","pErrB",sep));
			pw.write(String.format("%7s%s","pI",sep));
			pw.write(String.format("%7s%s","pI-Mn",sep));
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
