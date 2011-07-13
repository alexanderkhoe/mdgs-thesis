package id.mdgs.glvq;

import id.mdgs.lvq.Lvq;


/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class Glvq extends Lvq {

	public Glvq() {
		this.findWinner = new WinnerBySquaredEuc();
	}
}
