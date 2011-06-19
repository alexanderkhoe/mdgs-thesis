/**
 * 
 */
package id.mdgs.fnlvq;

import id.mdgs.fnlvq.FCodeBook.FEntry;
import id.mdgs.fnlvq.FCodeBook.FWinnerInfo;
import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.DatasetProfiler;
import id.mdgs.lvq.DatasetProfiler.PEntry;
import id.mdgs.utils.MathUtils;

import java.util.Iterator;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fnlvq {

	//codebook vector
	public FCodeBook codebook;
	public Dataset miu;
	
	public Fnlvq(){
		codebook = new FCodeBook();
		miu = new Dataset();
	}
	
	public int classify(Entry sample){
		FWinnerInfo wi;
		
		wi = this.findWinner(codebook, sample);
		
		if(MathUtils.equals(wi.coef, 0))
			return -1;
		else
			return wi.winner.label;
	}
	
	/**
	 * 
	 * @param data
	 * @param num    number of data to be agregated
	 */
	public void initCodes(Dataset data, int num){
		//pick random (every class) from dataset
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		/**/
		for(PEntry pe: profiler){
			FEntry code = new FEntry(data.numFeatures);
			code.label = pe.label;
			
			/*need to be optimized, not handle data lest then 2*/
			int max = num;
			if(pe.size() < max) max = pe.size();
			
			int[] rpos = MathUtils.randPerm(max, pe.size());
			double[][] fz = new double[data.numFeatures][3];
			for(int i=0;i<data.numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int i=0;i<rpos.length;i++){
				Entry sample = data.get(pe.get(rpos[i]));
				
				for(int j=0;j < sample.size();j++){
					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
					fz[j][1] += sample.data[j];
				}
			}
			
			for(int j=0;j < data.numFeatures;j++) {
				fz[j][1] /= max;
				if(max == 1){
					fz[j][0] -= 0.5*fz[j][0];
					fz[j][2] += 0.5*fz[j][2];
				}
			}

			
			for(int i=0;i < code.size();i++){
				code.data[i].set(fz[i]);
			}
			
			codebook.add(code);
			
			
			/*init miu*/
			Entry m = new Entry(data.numFeatures);
			m.label = pe.label;
			m.set(0);
			miu.add(m);
		}
	}
	
	/*ambil porsi sebesar portion dari tiap data kelas*/
	public void initCodes(Dataset data, double portion){
		//pick random (every class) from dataset
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		/**/
		for(PEntry pe: profiler){
			FEntry code = new FEntry(data.numFeatures);
			code.label = pe.label;
			
			int max = (int) portion * pe.size();
			if(max < 3) max = pe.size();
			
			int[] rpos = MathUtils.randPerm(max, pe.size());
			double[][] fz = new double[data.numFeatures][3];
			for(int i=0;i<data.numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int i=0;i<rpos.length;i++){
				Entry sample = data.get(pe.get(rpos[i]));
				
				for(int j=0;j < sample.size();j++){
					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
					fz[j][1] += sample.data[j];
				}
			}
			
			for(int j=0;j < data.numFeatures;j++) {
				fz[j][1] /= max;
				if(pe.size() < 3){
					fz[j][0] = -0.5*fz[j][1];
					fz[j][2] = 0.5*fz[j][1];
				}
			}

			
			for(int i=0;i < code.size();i++){
				code.data[i].set(fz[i]);
			}
			
			codebook.add(code);
			
			
			/*init miu*/
			Entry m = new Entry(data.numFeatures);
			m.label = pe.label;
			m.set(0);
			miu.add(m);
		}
	}
	
	public void initCodes(Dataset data){
		//ambil dari semua data
		DatasetProfiler profiler = new DatasetProfiler();
		profiler.run(data);

		/**/
		for(PEntry pe: profiler){
			FEntry code = new FEntry(data.numFeatures);
			code.label = pe.label;
			
			/*need to be optimized, not handle data lest then 2*/
			int max = pe.size();
			
//			int[] rpos = MathUtils.randPerm(max, pe.size());
			double[][] fz = new double[data.numFeatures][3];
			for(int i=0;i<data.numFeatures;i++){
				fz[i][0] = Double.POSITIVE_INFINITY; //min
				fz[i][1] = 0;   //mean
				fz[i][2] = Double.NEGATIVE_INFINITY;//max
			}
			
			for(int i=0;i<pe.size();i++){
				Entry sample = data.get(pe.get(i));
				
				for(int j=0;j < sample.size();j++){
					if(fz[j][0] > sample.data[j]) fz[j][0] = sample.data[j];
					if(fz[j][2] < sample.data[j]) fz[j][2] = sample.data[j];
					fz[j][1] += sample.data[j];
				}
			}
			
			for(int j=0;j < data.numFeatures;j++) {
				fz[j][1] /= max;
				if(max == 1){
					fz[j][0] -= 0.5*fz[j][0];
					fz[j][2] += 0.5*fz[j][2];
				}
			}

			for(int i=0;i < code.size();i++){
				code.data[i].set(fz[i]);
			}
			
			codebook.add(code);
			
			
			/*init miu*/
			Entry m = new Entry(data.numFeatures);
			m.label = pe.label;
			m.set(0);
			miu.add(m);
		}
	}
	
	public void initCodes(FCodeBook codebook){
		this.codebook.copyInfo(codebook);
		for(FEntry fe : codebook){
			this.codebook.add(fe);
			
			/*init miu*/
			Entry m = new Entry(fe.size());
			m.label = fe.label;
			m.set(0);
			miu.add(m);			
		}
	}

	public FWinnerInfo findWinner(FCodeBook codebook, Entry input){
		FWinnerInfo[] wis;
		
		wis = findWinner(codebook, input, 1);
		
		return wis[0];
	}
	
	public FWinnerInfo[] findWinner(FCodeBook codebook, Entry input, int num){
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = Double.NEGATIVE_INFINITY;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		Iterator<Entry>	mIt = miu.iterator();
		while(eIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			Entry sim = mIt.next();//miu.findEntry(code.label);
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(input.data[j]);
			}
			
			score = MathUtils.min(sim.data);
			
			/*sort desc*/
			for(i=0; (i < winner.length) && (score < winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
			}
		}
		
		return winner;
	}
	
	/*aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa*/
	
	
}
