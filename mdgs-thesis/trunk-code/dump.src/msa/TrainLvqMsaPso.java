/**
 * 
 */
package id.mdgs.lvq.msa;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvqMsaPso {

	public class Particle {
		public Lvq netRef;
		public Lvq pNet;
		public TrainLvq21Msa trainer;
		public Dataset velocity;
		public DatasetProfiler dp;
		public Dataset training;
		public WinnerInfo[] localBest;
		
		public Particle(Lvq network, Dataset training, double learningRate, double windowWidth){
			this.netRef = network;
			this.training = training;
			init();

			this.pNet = new Lvq();
			this.pNet.initCodes(this.training);
			this.trainer = new TrainLvq21Msa(this.pNet, training, learningRate, windowWidth);
		}
		
		public void init(){
//			this.pNet.codebook.copyInfo(this.training);
//			
//			for(Entry code: this.netRef.codebook){
//				PEntry pe = dp.findPEntry(code.label);
//				
//				/*pick rand number*/
//				int pos = pe.get(MathUtils.randomInt(0, pe.size()-1));
//				this.pNet.codebook.add(this.training.get(pos).clone());
//			}
			
			this.localBest = new WinnerInfo[this.netRef.codebook.size()];
			for(int i=0;i < this.localBest.length;i++){
				WinnerInfo wi = new WinnerInfo();
				wi.coef = -1;
				wi.winner = new Entry(this.netRef.codebook.numFeatures);
				wi.winner.set(0);
				this.localBest[i] = wi;
			}
			
			/*init velocity*/
			this.velocity = new Dataset();
			this.velocity.copyInfo(this.training);
			
			for(Entry code: this.netRef.codebook){
				Entry v = new Entry(code.size());
				v.label = code.label;
				
				for(int j=0;j < v.size();j++){
					v.data[j] = 0;
				}
				
				this.velocity.add(v);
			}			
		}	
		
		public void iteration(){
			this.trainer.iterationOption3();
			
			/*find local best*/
			for(int i=0;i < this.localBest.length;i++){
				if(this.localBest[i].coef < this.trainer.msa.similarities[i]){
					this.localBest[i].coef = this.trainer.msa.similarities[i];
					Entry best = this.pNet.codebook.findEntry(i);
					this.localBest[i].winner.copy(best);
				}
			}
			
//			double totA = 0, totB = 0;
//			for(int i=0;i < this.localBest.length;i++){
//				totA += this.localBest[i].coef;
//				totB += this.trainer.msa.similarities[i];
//			}
//			
//			if(totA < totB){
//				for(int i=0;i < this.localBest.length;i++){
//					this.localBest[i].coef = this.trainer.msa.similarities[i];
//					Entry best = this.pNet.codebook.findEntry(i);
//					this.localBest[i].winner.copy(best);
//				}				
//			}
		}
		
		public int getParticleBest(){
			return MathUtils.maxIndex(this.trainer.msa.similarities);
		}
		
		public double getParticleBestScore(){
			return MathUtils.max(this.trainer.msa.similarities);
		}
	}
	
	/*Class TrainLvqMsaPso*/
	public Lvq network;
	public Dataset training;
	public Particle[] particles;
	public WinnerInfo[] globalBest;
	public int maxEpoch;
	public int currEpoch;
	public double inertiaW;
	public int stagnanCounter;
	
	public TrainLvqMsaPso(Lvq network, Dataset training, int numParticles,
			double learningRate, double windowWidth){
		this.network = network;
		this.training = training;
		
		this.particles = new Particle[numParticles];
		for(int i=0;i<numParticles;i++){
			this.particles[i] = new Particle(network, training, 
								learningRate, windowWidth);
		}
		
		this.globalBest = new WinnerInfo[network.codebook.size()];
		for(int i=0;i < this.globalBest.length;i++){
			WinnerInfo wi = new WinnerInfo();
			wi.coef = -1;
			wi.winner = new Entry(network.codebook.numFeatures);
			wi.winner.set(0);
			this.globalBest[i] = wi;
		}
	}
	
	public void setMaxEpoch(int maxEpoch){
		for(int i=0;i < this.particles.length;i++){
			this.particles[i].setMaxEpoch(maxEpoch);
		}
		
		this.maxEpoch = maxEpoch;
		this.currEpoch = 0;
		this.stagnanCounter = 0;
	}
	
	public void iteration(){
		for(int i=0;i < this.particles.length;i++){
			this.particles[i].iteration();
			
//			for(int j=0;j < this.particles[i].localBest.length;j++){
//				System.out.format("%4.3f\t",this.particles[i].localBest[j].coef);
//			}
//			System.out.println();
		}
		
		/*opsi1*/
//		/*find global best*/
//		globalBest = 0;
//		double globalBestScore = this.particles[globalBest].getParticleBestScore();
//		for(int i=1;i < this.particles.length;i++){
//			if (globalBestScore < this.particles[i].getParticleBestScore()){
//				globalBest = i;
//				globalBestScore = this.particles[globalBest].getParticleBestScore();
//			}
//		}
		
		
		/*update bobot velocity*/
//		for(int i=0;i < this.particles.length;i++){
//			if(globalBest == i) continue;
//			
//			Particle gb = this.particles[globalBest];
//			Entry gbcode = gb.pNet.codebook.findEntry(gb.getParticleBest());
//			
//			Particle p = this.particles[i];
//			Entry code = p.pNet.codebook.findEntry(gb.getParticleBest());
//			
//			code.copy(gbcode);
//		}
//		
		/*opsi2*/
		/*find global*/
		for(int i=0;i < this.particles.length;i++){
			for(int c=0;c < this.network.codebook.size();c++){
				if(globalBest[c].coef < this.particles[i].localBest[c].coef){
					globalBest[c].coef = this.particles[i].localBest[c].coef;
					globalBest[c].winner.copy(
							this.particles[i].localBest[c].winner);
				}
			}
		}
		
		/*opsi3*/
//		double totA = 0, totB = 0;
//		for(int i=0;i < this.globalBest.length;i++){
//			totA += this.globalBest[i].coef;
//		}
//
//		for(int i=0;i < this.particles.length;i++){
//			totB = 0;
//			for(int c=0;c < this.network.codebook.size();c++){
//				totB += this.particles[i].localBest[c].coef;
//			}
//			
//			if(totA < totB){
//				for(int j=0;j < this.globalBest.length;j++){
//					this.globalBest[j].coef = this.particles[j].localBest[j].coef;
//					Entry best = this.particles[i].localBest[j].winner;
//					this.globalBest[j].winner.copy(best);
//				}	
//			}
//		}

		for(int j=0;j < this.globalBest.length;j++){
			System.out.format("%4.3f\t",this.globalBest[j].coef);
		}
		System.out.println();
		/**/
		
		
		/*adjust velocity*/
		for(int i=0;i < this.particles.length;i++){
			Dataset v = this.particles[i].velocity;
			Dataset px = this.particles[i].pNet.codebook;
			WinnerInfo[] lbs = this.particles[i].localBest;
			double cognitiveRand = Math.random();
			double socialRand = Math.random();
			
			for(int k=0;k < v.size();k++){
				Entry ve = v.get(k);
				Entry xe = px.get(k);
				Entry lb = lbs[k].winner;
				Entry gb = globalBest[k].winner;
				
				for(int j=0;j < ve.size();j++){
					ve.data[j] += ( 0.5 * cognitiveRand * (lb.data[j] - xe.data[j]) ) +
								  ( 0.5 * socialRand * (gb.data[j] - xe.data[j]) );
					xe.data[j] += ve.data[j];
				}
			}
		}
		
		for(int i=0;i < globalBest.length;i++){
			Entry code = network.codebook.findEntry(i);
			code.copy(globalBest[i].winner);
		}
		
	}
}
