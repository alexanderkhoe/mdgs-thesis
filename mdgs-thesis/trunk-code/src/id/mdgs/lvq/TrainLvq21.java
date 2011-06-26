/**
 * 
 */
package id.mdgs.lvq;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.FoldedDataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.lvq.LvqUtils.WinnerInfo;

/**
 * @author I Made Agus Setiawan
 *
 */
public class TrainLvq21 extends TrainLvq1 {

	protected double windowWidth;
	/**
	 * @param network
	 * @param training
	 * @param learningRate
	 */
	public TrainLvq21(Lvq network, Dataset training, double learningRate, double windowWidth) {
		this(network,  new FoldedDataset<Dataset, Entry>(training), learningRate, windowWidth);
	}
	
	public TrainLvq21(Lvq network, FoldedDataset<Dataset, Entry> foldedDs, double learningRate, double windowWidth) {
		super(network, foldedDs, learningRate);
		this.windowWidth = windowWidth;
	}
	
	public TrainLvq21(FoldedDataset<Dataset, Entry> foldedDs, double learningRate, double windowWidth){
		super(foldedDs, learningRate);
		this.windowWidth = windowWidth;
	}
	
	@Override
	protected WinnerInfo train(Dataset codebook, Entry input) {
		WinnerInfo[] wins;
		WinnerInfo top;
		
		wins = this.network.findWinner.function(codebook, input, 2);
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
		}
		
		return top;
	}
	

	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getClass().getSimpleName() + " -> ");
		sb.append("alpha: " + alphaStart + ","); 
		sb.append("window-width: " + windowWidth + ",");
		sb.append("epoch: " + maxEpoch);
		
		if(foldedDs != null)
			sb.append(", NdataTrain: " + foldedDs.size());
		
		return sb.toString();
	}
	
}
