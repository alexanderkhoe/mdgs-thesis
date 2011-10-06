/**
 * 
 */
package id.mdgs.dump;

import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.fnlvq.crisp.TrainFnlvqCrisp;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQ;
import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQTrain;
import id.mdgs.neural.utils.MSACalculation;
import id.mdgs.utils.DataSetUtils;

import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.util.logging.EncogLogging;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvqMsa extends TrainFnlvqCrisp {

	protected MSACalculation msa;
	/**
	 * @param network
	 * @param trainData
	 * @param learningRate
	 */
	public TrainFnlvqMsa(FNLVQ network, NeuralDataSet trainData,
			double learningRate) {
		this(network, trainData, learningRate, MSACalculation.DEFAULT_THRESHOLD);
	}
	
	public TrainFnlvqMsa(FNLVQ network, NeuralDataSet trainData,
			double learningRate, double threshold) {
		super(network, trainData, learningRate);
		
		msa = new MSACalculation(this, threshold);
	}

	@Override
	public void iteration(){
		EncogLogging.log(EncogLogging.LEVEL_INFO,"Performing FNLVQ Training iteration.");

		preIteration();

		// Reset weight correction 
		this.errorCalculation.reset();
		this.msa.reset();
		
		this.preservedWeights();
		
		for (final NeuralDataPair pair : getTraining()) {
				
			this.train(this.network, pair);
			
			updateError(this.network.getOutput(), pair.getIdeal());
			
			//
			int category = DataSetUtils.getTarget(pair.getIdeal());
			int win = this.network.findWinner(this.network.getOutput());
			//update only if network recognize the input
			if(win >= 0){
				msa.updateSimilarities(category, win, this.network.getOutput().getData()[win]);
			}
		}

		//calculate MSA and adjust the weight
		msa.calculateSimilarities();
		
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
	
	public void reloadPreviousWeights(int cluster) {
		//cluster = COLS
		for(int r = 0; r < this.inputCount; r++){
			FuzzyNode fn = this.network.getWeights().get(r, cluster);
			FuzzyNode fnOld = this.previousWeights.get(r, cluster);
			
			fn.copy(fnOld);
		}
	}	
	
	public MSACalculation getMSA(){
		return this.msa;
	}
	
	@Override
	public boolean isTrainingDone() {
		return this.msa.shouldStop();
	}
}
