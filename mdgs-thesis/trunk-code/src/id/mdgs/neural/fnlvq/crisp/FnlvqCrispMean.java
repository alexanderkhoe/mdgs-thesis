/**
 * 
 */
package id.mdgs.neural.fnlvq.crisp;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FnlvqCrispMean extends FnlvqCrisp {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3416036995051623917L;

	/**
	 * @param inCount
	 * @param outCount
	 */
	public FnlvqCrispMean(int inCount, int outCount) {
		super(inCount, outCount);
	}

	@Override
	public double transferFunction(double[] clusterData){
		//find mean for each cluster, save as output			
		double clusterOutput = 0;
		for(int c= 0;c < clusterData.length;c++)
			clusterOutput += clusterData[c];
		
		return clusterOutput/clusterData.length;
	}	
}
