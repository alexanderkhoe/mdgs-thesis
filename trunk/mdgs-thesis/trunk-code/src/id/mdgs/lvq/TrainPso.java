/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.DatasetProfiler.PEntry;
import id.mdgs.lvq.LvqUtils.MinMax;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainPso {
	
	class Particle {
		public Dataset codebook;
		public Dataset velocity;
		public Best localBest;
		public Particle(){
			codebook = new Dataset();
			velocity = new Dataset();
			localBest = new Best();
		}
	}
	
	class Best {
		public Dataset codebook;
		public double coef;
		public Best(){
			codebook = new Dataset();
			coef = Double.MAX_VALUE;
		}
		public void initBest(Dataset particle){
			codebook.copyInfo(particle);
			codebook.addAll(particle.entries);
		}
		public void saveBest(Dataset particle, double score){
			codebook.set(particle);
			coef = score;
		}
	}
	
	public Lvq network;
	private int numParticle;
	Dataset training;
	public Particle[] particles;
	Best globalBest;
	public double vMax, xMin, xMax; 
	public final double cognitiveFactor = 0.5d;
	public final double socialFactor = 0.5d;
	public double inertiaW;
	public int maxEpoch;
	public int currEpoch;
	
	public TrainPso(final Lvq network, final Dataset training, final int numParticle){
		this.network 		= network;
		this.training		= training;
		this.numParticle 	= numParticle;
		this.particles		= new Particle[numParticle];
		this.inertiaW		= 0.9d;
		initParticles();
	}
	
	public void initParticles(){
		DatasetProfiler dp = new DatasetProfiler();
		dp.run(this.training);
		MinMax[] dataRange = new MinMax[training.numFeatures];
		for(int i=0;i<dataRange.length;i++) 
			dataRange[i] = new MinMax();
		
		/*find min max data training*/
		double min = Double.POSITIVE_INFINITY, 
		max = Double.NEGATIVE_INFINITY;
		for(Entry e: training){			
			for(int i = 0;i < e.size();i++){
				dataRange[i].setMinMax(e.data[i]);
			}
		}
		
		xMin = min;
		xMax = max;
		//vMax = max - min;
		vMax = 1;
		
		for(int i= 0;i < particles.length; i++){
			particles[i] = new Particle();
			particles[i].codebook.copyInfo(training);
			particles[i].velocity.copyInfo(training);

			for(Entry code: this.network.codebook){
				Entry v = new Entry(code.size());
				v.label = code.label;
				v.set(0);
				
				particles[i].velocity.add(v);
			}
			
			for(Entry code: this.network.codebook){
				Entry pe = new Entry(code.size());
				pe.label = code.label;
				
				for(int j=0;j < pe.size();j++){
					pe.data[j] = MathUtils.randomDouble(dataRange[j].min, dataRange[j].max);
				}
				particles[i].codebook.add(pe);
				
//				PEntry pe = dp.findPEntry(code.label);
//				
//				/*pick rand number*/
//				int pos = pe.get(MathUtils.randomInt(0, pe.size()-1));
//				this.particles[i].codebook.add(this.training.get(pos).clone());
			}
			
			particles[i].localBest.initBest(particles[i].codebook);
		}		

		/*init globalbest*/
		globalBest = new Best();
		globalBest.initBest(particles[0].codebook);
	}
	
	public void setNumParticle(int numParticle) {
		this.numParticle = numParticle;
	}

	public int getNumParticle() {
		return numParticle;
	}

	/**
	 * @param maxEpoch the maxEpoch to set
	 */
	public void setMaxEpoch(int maxEpoch) {
		this.maxEpoch = maxEpoch;
		this.currEpoch= 0;
	}

	public void iteration(){
		int localBestId = -1;
		
		/*iterate all particle*/
		for(int p=0;p<particles.length;p++){
			double err = train(particles[p].codebook, training);
			if(err < particles[p].localBest.coef){
				particles[p].localBest.saveBest(particles[p].codebook, err);
			}
		}

		this.currEpoch++;
		
		for(int i=0;i < particles.length;i++){
			if(particles[i].localBest.coef < globalBest.coef){
				localBestId = i;
			}
		}
		
		if(localBestId >= 0){
			globalBest.saveBest(particles[localBestId].localBest.codebook, 
					particles[localBestId].localBest.coef);
		}
		
		/*adjust velocity*/
		this.inertiaW = 0.9 - ((0.9 - 0.4) * this.currEpoch / this.maxEpoch);
		for(int i=0;i < this.particles.length;i++){
			Dataset v = this.particles[i].velocity;
			Dataset px = this.particles[i].codebook;
			Dataset lbs = this.particles[i].localBest.codebook;
			double cognitiveRand = Math.random();
			double socialRand = Math.random();
			
			for(int k=0;k < v.size();k++){
				Entry ve = v.get(k);
				Entry xe = px.get(k);
				Entry lb = lbs.get(k);
				Entry gb = globalBest.codebook.get(k);
				
				for(int j=0;j < ve.size();j++){
					ve.data[j] =  this.inertiaW * ve.data[j] + 
								  ( 0.5 * cognitiveRand * (lb.data[j] - xe.data[j]) ) +
								  ( 0.5 * socialRand * (gb.data[j] - xe.data[j]) );
					xe.data[j] += ve.data[j];
				}
			}
		}
//		System.out.println(this.inertiaW);
		
		network.codebook.set(globalBest.codebook);
	}
	
	public double train(Dataset particle, Dataset training){
		double err = 0;
		int N = 0, True = 0;;
		 
		for(Entry e: training){
				WinnerInfo wi;
				wi = LvqUtils.findWinner(particle, e);
				
				if(wi.winner.label == e.label){
					True++;
				}
				N++;
		}
		
		return (1 - ((double) True/N));
		/*
		 * averaging error and reformat distance to similarity
		 * sim = 1 / (1 + dist)
		 */
		//return (double) 1 / (1 + (err/N));
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Particles  = %d\n", particles.length));
		sb.append(String.format("vMax       = %f\n", vMax));
		sb.append(String.format("xMin:xMax  = %f:%f\n", xMin, xMax));
		
		return sb.toString();
	}
}
