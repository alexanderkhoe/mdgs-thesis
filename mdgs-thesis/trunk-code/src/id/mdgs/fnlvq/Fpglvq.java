/**
 * 
 */
package id.mdgs.fnlvq;

/**
 * @author I Made Agus Setiawan
 *
 */
public class Fpglvq extends Fnlvq {

	/**
	 * 
	 */
	public Fpglvq() {
		findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MEAN);
	}

}
