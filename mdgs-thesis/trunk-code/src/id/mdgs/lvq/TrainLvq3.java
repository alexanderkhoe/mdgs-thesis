/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.lvq.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.utils;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq3 extends TrainLvq21 {

	protected double epsilon;
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 * @param windowWidth
	 */
	public TrainLvq3(Lvq network, Dataset training, double learningRate,
			double windowWidth, double epsilon) {
		super(network, training, learningRate, windowWidth);
		
		this.epsilon = epsilon;
	}
	
	/* (non-Javadoc)
	 * @see id.mdgs.lvq.TrainLvq2#train(id.mdgs.lvq.Dataset, id.mdgs.lvq.Dataset.Entry)
	 */
	@Override
	protected WinnerInfo train(Dataset codebook, Entry input) {
		WinnerInfo[] wins;
		WinnerInfo top;
		
		wins = this.findWinner.function(codebook, input, 2);
		top = wins[0];
		if(wins[0].winner.label != wins[1].winner.label){
			if( wins[0].winner.label == input.label ||
				wins[1].winner.label == input.label ) {
				
				if((wins[0].coef/wins[1].coef) > ((1 - windowWidth)/(1 + windowWidth))){
					if(wins[1].winner.label == input.label){
						WinnerInfo wi;
						
						wi = wins[0];
						wins[0] = wins[1];
						wins[1] = wi;
					}
					
					/*adjust code vector*/
					this.adjustWeights(wins[0].winner, input, this.alpha);
					this.adjustWeights(wins[1].winner, input, this.alpha);
				}
			}
		} else {utils.log("masuk");
			if( wins[0].winner.label == input.label ){
				/*adjust code vector*/
				this.adjustWeights(wins[0].winner, input, this.alpha * this.epsilon);
				this.adjustWeights(wins[1].winner, input, this.alpha * this.epsilon);
			}
		}
		
		return top;
	}

}
