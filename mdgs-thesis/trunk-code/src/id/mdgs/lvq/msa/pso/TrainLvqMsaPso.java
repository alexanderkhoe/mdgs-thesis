/**
 * 
 */
package id.mdgs.lvq.msa.pso;

import java.util.ArrayList;
import java.util.List;

import id.mdgs.lvq.Dataset;
import id.mdgs.lvq.Lvq;
import id.mdgs.lvq.msa.TrainLvq1Msa;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvqMsaPso {

	public int numOfParticle;
	public List<Lvq> particles;
	public Dataset training;
	
	public TrainLvqMsaPso(Dataset training, int numOfParticle){
		this.numOfParticle = numOfParticle;
		this.training = training;
		
		this.particles = new ArrayList<Lvq>();
		
		for(int i = 0; i < numOfParticle; i++)
			this.particles.add(new Lvq());
	}
	
	/**
	 * @return the training
	 */
	public Dataset getTraining() {
		return training;
	}

	/**
	 * @param training the training to set
	 */
	public void setTraining(Dataset training) {
		this.training = training;
	}

	public void initRandom(){
		for(Lvq particle: particles){
			particle.initCodes(getTraining());
		}
	}
	
	

}
