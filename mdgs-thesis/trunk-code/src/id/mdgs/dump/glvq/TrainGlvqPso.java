/**
 * 
 */
package id.mdgs.dump.glvq;

import java.util.Iterator;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.evaluation.ConfusionMatrix;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainGlvqPso {

	public class Particle {
		public Lvq netRef;
		public Lvq pNet;
		public TrainGlvq1 trainer;
		public Dataset velocity;
		public DatasetProfiler dp;
		public Dataset training;
		public WinnerInfo[] localBest;
		
		public Particle(Lvq network, Dataset training, double learningRate){
			this.netRef = network;
			this.training = training;
			init();

			this.pNet = new Lvq();
			this.pNet.initCodes(this.training);
			this.trainer = new TrainGlvq1(this.pNet, training, learningRate);
		}
		
		public void init(){
			this.localBest = new WinnerInfo[this.netRef.codebook.size()];
			for(int i=0;i < this.localBest.length;i++){
				WinnerInfo wi = new WinnerInfo();
				wi.coef = Double.MAX_VALUE;
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
		public void setMaxEpoch(int maxEpoch){
			this.trainer.setMaxEpoch(maxEpoch);
		}
		
		public void iteration(){
			this.trainer.iteration();
			
			/*find local best*/
			if(this.localBest[0].coef > this.trainer.getError()){
				for(int i=0;i < this.localBest.length;i++){
					this.localBest[i].coef = this.trainer.getError();
					Entry best = this.pNet.codebook.get(i);
					this.localBest[i].winner.copy(best);
				}
			}
		}
		
		public double testing(Dataset testset, int nclass){
			ConfusionMatrix cm = new ConfusionMatrix(nclass);
			Iterator<Entry> it = testset.iterator();
			while(it.hasNext()){
				Entry sample = it.next(); 
				
				int win = pNet.classify(sample);
				int target = sample.label;
				
				cm.feed(win, target);
			}
			return cm.getAccuracy();
		}
	}
	
	/*Class TrainLvqMsaPso*/
	public final int MAX_STAGNAN = 10;
	public Lvq network;
	public Dataset training;
	public Particle[] particles;
	public WinnerInfo[] globalBest;
	public int maxEpoch;
	public int currEpoch;
	public double inertiaW;
	public int stagnanCounter;
	
	public TrainGlvqPso(Lvq network, Dataset training, int numParticles,
			double learningRate){
		this.network = network;
		this.training = training;
		
		this.particles = new Particle[numParticles];
		for(int i=0;i<numParticles;i++){
			this.particles[i] = new Particle(network, training, 
								learningRate);
		}
		
		this.globalBest = new WinnerInfo[network.codebook.size()];
		for(int i=0;i < this.globalBest.length;i++){
			WinnerInfo wi = new WinnerInfo();
			wi.coef = Double.MAX_VALUE;
			wi.winner = new Entry(network.codebook.numFeatures);
			wi.winner.set(0);
			this.globalBest[i] = wi;
		}
		
		this.inertiaW = 0.9d;
	}
	
	public void setMaxEpoch(int maxEpoch){
		for(int i=0;i < this.particles.length;i++){
			this.particles[i].setMaxEpoch(maxEpoch);
		}
		
		this.maxEpoch = maxEpoch;
		this.currEpoch = 0;
		this.stagnanCounter = 0;
	}
	
	public boolean shouldStop(){
		return (currEpoch >= maxEpoch) || (stagnanCounter > MAX_STAGNAN);
	}
	
	public void iteration(){
		//System.out.print(this.currEpoch + ". ");	
		for(int i=0;i < this.particles.length;i++){
			this.particles[i].iteration();
			
			//.format("%4.3f\t",this.particles[i].trainer.getError());
		}
		//System.out.println();		
		/**/
		this.currEpoch++;
		
		/*opsi2*/
		/*find global*/
		double oldGlobal = this.globalBest[0].coef;
		for(int i=0;i < this.particles.length;i++){
			if(this.globalBest[0].coef > this.particles[i].localBest[0].coef){
				for(int j=0;j < this.globalBest.length;j++){
					this.globalBest[j].coef = this.particles[i].localBest[j].coef;
					this.globalBest[j].winner.copy(
							this.particles[i].localBest[j].winner);
				}
			}			
		}
		
		if(oldGlobal ==  this.globalBest[0].coef){
			stagnanCounter++;
		} else {
			stagnanCounter = 0;
		}
		//System.out.format("GBest: %4.3f\n",this.globalBest[0].coef);
		/**/
		
		
		/*adjust velocity*/
		this.inertiaW = 0.9 - ((0.9 - 0.4) * this.currEpoch / this.maxEpoch);
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
					ve.data[j] =  this.inertiaW * ve.data[j] + 
								  ( 0.5 * cognitiveRand * (lb.data[j] - xe.data[j]) ) +
								  ( 0.5 * socialRand * (gb.data[j] - xe.data[j]) );
					xe.data[j] += ve.data[j];
				}
			}
		}
		
		for(int i=0;i < globalBest.length;i++){
			Entry code = network.codebook.get(i);
			code.copy(globalBest[i].winner);
		}
		
//		for(int i=0;i < particles[0].pNet.codebook.size();i++){
//			Entry code = network.codebook.findEntry(i);
//			code.copy(particles[0].pNet.codebook.findEntry(i));
//		}
		
	}
}
