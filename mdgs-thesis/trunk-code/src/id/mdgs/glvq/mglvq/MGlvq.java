/**
 * 
 */
package id.mdgs.glvq.mglvq;

import java.util.Iterator;
import org.apache.commons.math.linear.RealMatrix;

import id.mdgs.dataset.Dataset;
import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.WinnerBySquaredEuc;
import id.mdgs.glvq.mglvq.WinnerByMahalanobis.MParam;
import id.mdgs.lvq.LvqUtils.WinnerInfo;
import id.mdgs.utils.MathUtils;

/**
 * @author I Made Agus Setiawan
 *
 */

/**
 * Note :
 * 
 * 2011.08.12
 * - using global covariance matrix
 * 
 * 2011.06.15
 * - Mahalanobis distance, support unique Covariance matrix for each class 
 *   category
 */
public class MGlvq extends Glvq {
	
	public MGlvq(MParam mps){
		this.findWinner = new WinnerByMahalanobis(mps);
	}
	
	

	
}
