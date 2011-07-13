package id.mdgs.fnlvq;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class CFnlvq extends Fnlvq {

	public CFnlvq(){
		findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MEAN);
	}
	
}
