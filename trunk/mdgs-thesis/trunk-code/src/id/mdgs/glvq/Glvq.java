package id.mdgs.glvq;

import id.mdgs.lvq.Lvq;

public class Glvq extends Lvq {

	public Glvq() {
		this.findWinner = new WinnerBySquaredEuc();
	}
}
