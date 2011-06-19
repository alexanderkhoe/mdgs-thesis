/**
 * 
 */
package id.mdgs.neural.lvq;

import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.utils;

import org.encog.mathutil.matrices.Matrix;
import org.encog.ml.MLMethod;
import org.encog.ml.TrainingImplementationType;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.networks.training.BasicTraining;
import org.encog.neural.networks.training.LearningRate;
import org.encog.neural.networks.training.Strategy;
import org.encog.neural.networks.training.propagation.TrainingContinuation;
import org.encog.neural.networks.training.strategy.SmartLearningRate;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq extends BasicTraining implements LearningRate {
	protected final double ALPHA_MONOTONICALLY_DECREASING_FACTOR = 0.9999D;
	protected Lvq1 network;
	private double alpha;
	
	protected BestMatchingUnit bmuUtil;
	protected int inputCount;
	protected int outputCount;
	/**
	 * 
	 */
	public TrainLvq(final Lvq1 network, NeuralDataSet training, double learningRate) {
		super(TrainingImplementationType.Iterative);
		this.network = network;
		setTraining(training);
		setLearningRate(learningRate);
		
		this.inputCount = network.getInputCount();
		this.outputCount = network.getOutputCount();
		
		this.bmuUtil = new BestMatchingUnit(network);
	}

	@Override
	public void iteration() {
		preIteration();
		
		bmuUtil.reset();
		
		for(NeuralDataPair pair: getTraining()){
			train(this.network, pair);
		}
		
		// update the error
		setError(this.bmuUtil.getWorstDistance()/100.0);
		
		this.updateLearningRate();
		
		postIteration();
	}

	protected void train(Lvq1 network, NeuralDataPair pair) {
		NeuralData input = pair.getInput();
		NeuralData ideal = pair.getIdeal();
		
		//find winner
		int win = this.bmuUtil.calculateBMU(input);
		
		//find ideal 
		int target = DataSetUtils.getTarget(ideal);
		
		this.adjustWeights(network.getWeights(), input, win, target);
	}
	
	private void adjustWeights(Matrix weights, NeuralData input, int win,
			int target) {
//		utils.log(input.toString());
		//LVQ1 Rules
		//move toward input vector
		if(win == target){
			for(int i=0;i<this.inputCount;i++){
				double wi = weights.get(i, win);
				double in = input.getData(i);
				
				double newWi = wi + this.alpha * (in - wi);
				
				weights.set(i, win, newWi);
			}
		}
		
		//move away from input vector
		else {
			for(int i=0;i<this.inputCount;i++){
				double wi = weights.get(i, win);
				double in = input.getData(i);
				
				double newWi = wi - this.alpha * (in - wi);
				
				weights.set(i, win, newWi);
			}
		}
	}


	@Override
	public boolean canContinue() {
		return false;
	}

	@Override
	public TrainingContinuation pause() {
		return null;
	}

	@Override
	public void resume(TrainingContinuation state) {
	}

	/**
	 * LearningRate is monotonically decreasing scalar
	 * gain factor 0 < alpha <= 1
	 * 
	 * alpha = 0.9999 * alpha
	 * 
	 * IF training use smartLearningRate Strategy, skip it
	 * SmartLearningRate Strategy initiate 
	 * alpha = 1 / number of training record
	 */
	protected void updateLearningRate(){
		boolean isUseSmartLearningRate = false;
		for(Strategy s: this.getStrategies()){
			if(s instanceof SmartLearningRate){
				//skip manual update
				isUseSmartLearningRate = true;
			}
		}
		
		if(!isUseSmartLearningRate)
			this.alpha *= ALPHA_MONOTONICALLY_DECREASING_FACTOR;
	}
	
	@Override
	public void setLearningRate(double rate) {
		this.alpha = rate;
	}

	@Override
	public double getLearningRate() {
		return this.alpha;
	}


	@Override
	public MLMethod getNetwork() {
		return this.network;
	}

}
