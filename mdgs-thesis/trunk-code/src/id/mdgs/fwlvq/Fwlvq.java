package id.mdgs.fwlvq;

import java.util.Iterator;

import id.mdgs.dataset.FCodeBook;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.FCodeBook.FEntry;
import id.mdgs.fnlvqold.Fnlvq;
import id.mdgs.lvq.LvqUtils.FWinnerInfo;
import id.mdgs.utils.MathUtils;

public class Fwlvq extends Fnlvq {

	public Fwlvq() {
	}

	@Override
	public int classify(Entry sample) {
		FWinnerInfo wi;
		
		wi = this.findWinner(codebook, sample);
		
		if(MathUtils.equals(wi.coef, wavelet(0)))
			return -1;
		else
			return wi.winner.label;
	}

	public double wavelet(double bobot){
		return Math.pow(2.0*Math.PI/Math.sqrt(3),-1.0/4) * (1.0-bobot) *
			   Math.exp((bobot*bobot)/2);
	}
	
	@Override
	public FWinnerInfo findWinner(FCodeBook codebook, Entry input) {
		FWinnerInfo[] wis;
		
		wis = findWinner(codebook, input, codebook.size());
		
		return wis[0];
	}

	@Override
	public FWinnerInfo[] findWinner(FCodeBook codebook, Entry input, int num) {
		return findWinner1(codebook, input, num);
	}

	
	public FWinnerInfo[] findWinner1(FCodeBook codebook, Entry input, int num) {
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = Double.MAX_VALUE;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		Iterator<Entry>  mIt = miu.iterator();
		while(eIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			Entry sim = mIt.next();
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(input.data[j]);
			}
			
			score = MathUtils.mean(sim.data);
			score = wavelet(score);
			
			/*sort asc*/
			for(i=0; (i < winner.length) && (score > winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
			}
		}
		
//		for(i=0;i < winner.length;i++)
//			System.out.println(winner[i].winner.label + " " + winner[i].coef);
		
		return winner;	
	}
	
	public FWinnerInfo[] findWinner2(FCodeBook codebook, Entry input, int num) {
		FWinnerInfo[] winner;
		int i;
		winner = new FWinnerInfo[num];
		for(i=0;i < winner.length;i++){
			winner[i] = new FWinnerInfo();
			//sort descending, set coef to minimum
			winner[i].coef = Double.MAX_VALUE;
		}
		
		Iterator<FEntry> eIt = codebook.iterator();
		Iterator<Entry>  mIt = miu.iterator();
		double totScore = 0;
		double[] scores = new double[codebook.size()];
		while(eIt.hasNext()) {
			double score = 0;
			FEntry code = eIt.next();
			Entry sim = mIt.next();
			
			for(int j=0;j < code.size();j++){
				sim.data[j] = code.data[j].getMaxIntersection(input.data[j]);
			}
			
			score  = MathUtils.min(sim.data);
			scores[code.label] = score;
			totScore += score;
		}
		
		for(int c=0;c < scores.length;c++){
			double score;
			if(MathUtils.equals(totScore, 0)) score = 0;
			else score = scores[c] / totScore;
			
			score = wavelet(score);
			FEntry code = codebook.findEntry(c);
			
			/*sort asc*/
			for(i=0; (i < winner.length) && (score > winner[i].coef); i++);
			
			if(i < winner.length){
				for(int j=winner.length - 1;j > i;j--){
					winner[j].copy(winner[j-1]);
				}
				winner[i].coef 	 = score;
				winner[i].winner = code;
			}
		}
		
//		for(i=0;i < winner.length;i++)
//			System.out.println(winner[i].winner.label + " " + winner[i].coef);
		
		return winner;	
	}
	
}
