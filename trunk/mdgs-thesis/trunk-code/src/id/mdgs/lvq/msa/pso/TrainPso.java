/**
 * 
 */
package id.mdgs.lvq.msa.pso;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.DatasetProfiler;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.dataset.DatasetProfiler.PEntry;
import id.mdgs.lvq.Lvq;
import id.mdgs.utils.MathUtils;
import id.mdgs.utils.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainPso {

	public Lvq network;
	public Dataset[] particles;
	private int numParticle;
	Dataset globalBest;
	double  globalBestScore;
	Dataset training;
	Dataset[]	velocities;
	//double[] velocities;
	public double vMax, xMin, xMax; 
	public final double cognitiveFactor = 0.5d;
	public final double socialFactor = 0.5d;
	public Msa[] msas;
	
	public TrainPso(final Lvq network, final Dataset training, final int numParticle){
		this.network 		= network;
		this.training		= training;
		this.numParticle 	= numParticle;
		this.globalBestScore= Double.MIN_VALUE;
//		this.globalBest		= new Dataset();
		this.particles		= new Dataset[numParticle];
		//this.velocities		= new double[numParticle];
		this.velocities		= new Dataset[numParticle];
		this.msas			= new Msa[numParticle];
		initVelocities();
		initParticles();
	}
	
	
	/*init random between -vMax ... vMax*/	
	public void initVelocities(){
		/*find min max data training*/
		double min = Double.POSITIVE_INFINITY, 
		max = Double.NEGATIVE_INFINITY;
		for(Entry e: training){			
			for(int i = 0;i < e.size();i++){
				if(min > e.data[i]) min = e.data[i];
				if(max < e.data[i]) max = e.data[i];
			}
		}
		
		xMin = min;
		xMax = max;
		//vMax = max - min;
		vMax = 1;
//		for(int p=0;p < this.particles.length;p++){
//			this.velocities[p] =  MathUtils.randomDouble(-vMax, vMax);
//		}
		
		for(int i= 0;i < numParticle; i++){
			this.velocities[i] = new Dataset();
			this.velocities[i].copyInfo(training);

			for(Entry code: this.network.codebook){
				Entry v = new Entry(code.size());
				v.label = code.label;
				
				for(int j=0;j < v.size();j++){
					//v.data[j] = MathUtils.randomDouble(0, vMax);
					v.data[j] = 0;
				}
				
				this.velocities[i].add(v);
			}
		}
	}
	
	public void initParticles(){
		DatasetProfiler dp = new DatasetProfiler();
		dp.run(this.training);

		for(int i= 0;i < numParticle; i++){
			this.particles[i] = new Dataset();
			this.particles[i].copyInfo(training);
			
			for(Entry code: this.network.codebook){
//				PEntry pe = dp.findPEntry(code.label);
//				
//				/*pick rand number*/
//				int pos = pe.get(MathUtils.randomInt(0, pe.size()-1));
//				this.particles[i].add(this.training.get(pos).clone());
				Entry pe = new Entry(code.size());
				pe.label = code.label;
				for(int j=0;j < pe.size();j++){
					pe.data[j] = MathUtils.randomDouble(xMin, xMax);
				}
				
				this.particles[i].add(pe);				
			}
			
			//init Msa
			this.msas[i]	= new Msa(dp.size());
			this.msas[i].resetAll();
		}
		
		/*init globalbest*/
		this.globalBest = this.particles[0].clone();
	}
	
	public void setNumParticle(int numParticle) {
		this.numParticle = numParticle;
	}

	public int getNumParticle() {
		return numParticle;
	}

	public void iteration(){
		double localBestScore = -1;
		int localBestId = -1;
		
		/*iterate all particle*/
		for(int p=0;p<particles.length;p++){
			double sim = train(particles[p], training);
			if(sim > localBestScore){
				localBestScore = sim;
				localBestId = p;
			}
//			System.out.println(sim);
		}
		
		if(localBestScore > globalBestScore){
			globalBestScore = localBestScore;
			globalBest.set(particles[localBestId]);
		}
		
//		System.out.println(localBestScore);
		
		adjustParticles(particles[localBestId]);
	}
	
	public void adjustParticles(Dataset localBest){
		/*calc velocity*/
		
		for(int p=0;p < particles.length;p++){
			Dataset v = this.velocities[p];
			Dataset particle = this.particles[p];
			double cognitiveRand = Math.random();
			double socialRand = Math.random();
			
			for(int i=0;i < v.size();i++){
				Entry ve = v.get(i);
				Entry xe = particle.get(i);
				Entry lb = localBest.get(i);
				Entry gb = this.globalBest.get(i);
				
				for(int j=0;j < ve.size();j++){
					ve.data[j] += ( cognitiveFactor * cognitiveRand * (lb.data[j] - xe.data[j]) ) +
								  ( socialFactor * socialRand * (gb.data[j] - xe.data[j]) );
					xe.data[j] += ve.data[j];
 
					if ( ve.data[j] < -vMax ) ve.data[j] = -vMax;
					if ( ve.data[j] > vMax ) ve.data[j] = vMax;
					if ( xe.data[j] < xMin ) xe.data[j] = xMin;
					if ( xe.data[j] > xMax ) xe.data[j] = xMax;
					
					//System.out.println(ve.data[j]);
				}
			}
		}
	}
	
	public double train(Dataset particle, Dataset training){
		double err = 0;
		int N = 0;
		 
		for(Entry e: training){
			for(Entry pc: particle){
				if(pc.label == e.label){
					err += MathUtils.euclideDistance(e.data, pc.data);
					N++;
				}
			}
		}
		
		/*
		 * averaging error and reformat distance to similarity
		 * sim = 1 / (1 + dist)
		 */
		return (double) 1 / (1 + (err/N));
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Particles  = %d\n", particles.length));
		sb.append(String.format("vMax       = %f\n", vMax));
		sb.append(String.format("xMin:xMax  = %f:%f\n", xMin, xMax));
		
		return sb.toString();
	}
}
