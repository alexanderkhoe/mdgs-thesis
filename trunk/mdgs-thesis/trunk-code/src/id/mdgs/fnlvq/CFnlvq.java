package id.mdgs.fnlvq;

public class CFnlvq extends Fnlvq {

	public CFnlvq(){
		findWinner = new WinnerByFuzzy(WinnerByFuzzy.TRANSFER.MEAN);
	}
	
}
