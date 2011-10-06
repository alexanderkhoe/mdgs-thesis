package id.mdgs.neural.fnlvq.crisp;
import junit.framework.Assert;
import id.mdgs.matrices.WeightMatrix;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.utils.DataSetUtils;
import id.mdgs.utils.Loging;

import org.encog.engine.util.EngineArray;
import org.encog.engine.util.ErrorCalculation;
import org.encog.engine.util.ErrorCalculationMode;
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
 * @author madeagus
 * @version 1.0
 * @created 27-Apr-2011 12:36:32 PM
 */
public class TrainFnlvqCrisp extends BasicTraining implements LearningRate {

	protected final double ALPHA_MONOTONICALLY_DECREASING_FACTOR = 0.9999D;
	
	protected double delta = 1.003D;	
	protected double alpha;	
	protected double beta;
	protected double gamma;
	protected double eta;
	protected double kappa;

	protected boolean modifiedByVariable;
	
	protected FnlvqCrisp network;
	protected int inputCount;
	protected int outputCount;
	
	protected final WeightMatrix previousWeights;
	
	protected final ErrorCalculation errorCalculation = new ErrorCalculation();
	
	public static void setErrorMode(){
		ErrorCalculation.setMode(ErrorCalculationMode.RMS);
	}
	
	public TrainFnlvqCrisp(final FnlvqCrisp network, NeuralDataSet trainData, 
			final double learningRate){
		super(TrainingImplementationType.Iterative);
		super.setError(0);
		setTraining(trainData);
		setLearningRate(learningRate);				
		setFuzzinessFactorByConstant(0.05D, 0.05D);
		
		this.network = network;
		this.inputCount = network.getInputCount();
		this.outputCount = network.getOutputCount();

		this.previousWeights = new WeightMatrix(this.network.getInputCount(), 
				this.network.getOutputCount());
		
		TrainFnlvqCrisp.setErrorMode();
	}

	/**
	 * @return The learning rate.
	 */
	public double getLearningRate(){
		return alpha;
	}

	/**
	 * Perform one iteration of training.
	 */
	public void iteration(){
		preIteration();

		// Reset weight correction 
		this.errorCalculation.reset();
		
		//keep previous weight
		this.preservedWeights();
		
		for (final NeuralDataPair pair : getTraining()) {
				
			this.train(this.network, pair);
			
			postProcess(this.network.getOutput(), pair.getIdeal());
		}

		//update error
		setError(this.errorCalculation.calculate());
		
		//monotonically decreasing
		this.updateLearningRate();
		
		//update eta and kappa if in modifiedByVariable Mode
		if(this.isModifiedByVariable()){
			this.updateEtaKappa();
		}
		
		postIteration();
	}

	protected void postProcess(NeuralData output, NeuralData ideal){
		//calculate margin error
		double[] targetIdeal;
		if(ideal.getData().length == 1){
			targetIdeal = new double[network.getOutputCount()];
			int target = (int) ideal.getData()[0]; 
			EngineArray.fill(targetIdeal, 0);
			targetIdeal[target] = 1;
		} else {
			targetIdeal = ideal.getData();
		}
				
		this.errorCalculation.updateError(output.getData(), targetIdeal);
	}
	
	protected void preservedWeights() {
		//copy current weights
		this.previousWeights.set(this.network.getWeights());		
	}

	protected void train(FnlvqCrisp network, NeuralDataPair pair) {
		NeuralData input = pair.getInput();
		NeuralData ideal = pair.getIdeal();
		
		//find winner
		int win = network.winner(input);
		
		//find ideal 
		int target = DataSetUtils.getTarget(ideal);
		
		this.adjustWeights(network.getWeights(),
				network.getMembershipDegrees(), input, win, target);
	}


	/**
	 * 
	 * @param weights
	 * @param miu
	 * @param input
	 * @param win
	 * @param target
	 */
	protected void adjustWeights(WeightMatrix weights, 
			Matrix mius, NeuralData input, int win, int target) {
		
		//unknown class (-1)
		if (win == -1){

			for(int cluster = 0; cluster < this.outputCount; cluster++){
				for(int i = 0; i < this.inputCount; i++){
					FuzzyNode fn = (FuzzyNode) weights.get(i, cluster);
					
					this.delta = 1.0003d;
					fn.min  = fn.mean - this.delta * (fn.mean - fn.min);
					fn.max  = fn.mean + this.delta * (fn.max - fn.mean);
					Assert.assertTrue("win==-1 , " + i + cluster,fn.max > fn.min);
				}
			}
		}
		
		//equals
		else if (win == target){
			//shift winning cluster toward the target
			for (int r=0; r<weights.getRows();r++){
				FuzzyNode fn = (FuzzyNode) weights.get(r, win);
				double miu 	 = mius.get(r, win);
				double in 	 = input.getData(r);
				
				final double lRange = fn.mean - fn.min;
				final double rRange = fn.max - fn.mean;
				fn.mean = fn.mean + this.alpha * (1 - miu) * (in - fn.mean);
				fn.min  = fn.mean - lRange;
				fn.max  = fn.mean + rRange;
				 			
				//increase fuzziness (extend fuzziness)
				//adjust by variable
				if(this.isModifiedByVariable()){
					fn.min = fn.min - (1 - miu) * (1 + this.eta) * lRange;
					fn.max = fn.max + (1 - miu) * (1 + this.eta) * rRange;
				}
				
				//adjust by constant
				else {
					this.beta = 0.005d;
					fn.min = fn.min - (this.beta) * lRange;
					fn.max = fn.max + (this.beta) * rRange;
				}					
				
				Assert.assertTrue("win==ideal, " + r + "," + win ,fn.max > fn.min);
			}
		}
		
		//not equals
		else {
			//shift false winner cluster away from target
			for (int r=0; r<weights.getRows();r++){
				FuzzyNode fn = (FuzzyNode) weights.get(r, win);
				double miu 	 = mius.get(r, win);
				double in 	 = input.getData(r);
				
				final double lRange = fn.mean - fn.min;
				final double rRange = fn.max - fn.mean;
				fn.mean = fn.mean - this.alpha * (1 - miu) * (in - fn.mean);
				fn.min  = fn.mean - lRange;
				fn.max  = fn.mean + rRange;
				
				//decrease fuzziness (narrow fuzziness)
				//adjust by variable
				if(this.isModifiedByVariable()){
					fn.min = fn.min + (1 - miu) * (1 + this.kappa) * lRange;
					fn.max = fn.max - (1 - miu) * (1 + this.kappa) * rRange;
				}
				
				//adjust by constant
				else {
					this.gamma = 0.005d;
					fn.min = fn.min + (this.gamma) * lRange;
					fn.max = fn.max - (this.gamma) * rRange;				
				}
				
				Assert.assertTrue("win!=ideal, " + r + "," + win +
						fn.toString()
						,fn.max > fn.min);
			}
		}
	}

	/**
	 * Perform a number of training iterations.
	 * 
	 * @param count    The number of iterations to perform.
	 */
	public void iteration(int count){

	}

	/**
	 * Set the learning rate.
	 * 
	 * @param rate    The new learning rate
	 */
	public void setLearningRate(double rate){
		this.alpha = rate;
	}

	@Override
	public MLMethod getNetwork() {
		return this.network;
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
	public void resume(TrainingContinuation arg0) {
		//do nothing
	}

	public boolean isModifiedByVariable(){
		return this.modifiedByVariable;
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
	
	protected void updateEtaKappa(){
		this.kappa	= (1 - this.alpha);
		this.eta 	= 0.01 * this.kappa;
	}
	
	public void setFuzzinessFactorByVariable(){
		this.modifiedByVariable = true;
		this.updateEtaKappa();
	}
	
	public void setFuzzinessFactorByConstant(double beta, double gamma) {
		this.modifiedByVariable = false;
		this.beta = beta;
		this.gamma = gamma;
	}
}