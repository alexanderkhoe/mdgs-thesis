/**
 * 
 */
package id.mdgs.neural.fnlvq.crisp;

import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.utils.MSACalculation;
import id.mdgs.utils.DataSetUtils;

import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataSet;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainFnlvqCrispMsa extends TrainFnlvqCrisp {

	protected MSACalculation msa;
	
	/**
	 * @param network
	 * @param trainData
	 * @param learningRate
	 */
	public TrainFnlvqCrispMsa(FnlvqCrisp network, NeuralDataSet trainData,
			double learningRate) {
		this(network, trainData, learningRate, MSACalculation.DEFAULT_THRESHOLD);
	}
	
	public TrainFnlvqCrispMsa(FnlvqCrisp network, NeuralDataSet trainData,
			double learningRate, double threshold) {
		super(network, trainData, learningRate);
		
		this.setMsa(new MSACalculation(this));
	}
	
	public void setMsa(MSACalculation msa) {
		this.msa = msa;
	}
	
	public MSACalculation getMsa(){
		return this.msa;
	}
	
	@Override
	public boolean isTrainingDone() {
		return this.msa.shouldStop();
	}

	/**
	 * called in MSACalculation class
	 * 
	 * @param c
	 */
	public void reloadPreviousWeights(int c) {
		//cluster = COLS
		for(int r = 0; r < this.inputCount; r++){
			FuzzyNode fn = (FuzzyNode) this.network.getWeights().get(r, c);
			FuzzyNode fnOld = (FuzzyNode) this.previousWeights.get(r, c);
			
			fn.copy(fnOld);
		}
	}	
	
	@Override
	public void iteration(){
		//reset msa
		this.getMsa().reset();
		
		//call super iteration
		super.iteration();
		
		//calculate MSA and adjust the weights automatically
		msa.calculateSimilarities();
	}
	
	/**
	 * this method trigger on every sample iteration, NOT every epoch
	 */
	@Override
	public void postProcess(NeuralData output, NeuralData ideal){
		//call super postProcess
		super.postProcess(output, ideal);
		
		//trigger update msa similarity matrix
		int category = DataSetUtils.getTarget(ideal);
		int win = this.network.findWinner(output);
		//update only if network recognize the input
		if(win >= 0){
			msa.updateSimilarities(category, win, output.getData()[win]);
		}		
	}

}
