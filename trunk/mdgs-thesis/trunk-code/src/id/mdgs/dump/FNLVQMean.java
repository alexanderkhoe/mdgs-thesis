/**
 * 
 */
package id.mdgs.dump;

import id.mdgs.neural.fnlvq.crisp.fnlvq.crisp.FNLVQ;

/**
 * @author I Made Agus Setiawan
 *
 */
public class FNLVQMean extends FNLVQ {

	/**
	 * 
	 */
	private static final long serialVersionUID = -489000850053947142L;

	/**
	 * @param inCount
	 * @param outCount
	 */
	public FNLVQMean(int inCount, int outCount) {
		super(inCount, outCount);
	}

	@Override
	public double transferFunction(double[] clusterData){
		//find minimum for each cluster, save as output			
		double clusterOutput = 0;
		for(int c= 0;c < clusterData.length;c++)
			clusterOutput += clusterData[c];
		
		return clusterOutput/clusterData.length;
	}
}
