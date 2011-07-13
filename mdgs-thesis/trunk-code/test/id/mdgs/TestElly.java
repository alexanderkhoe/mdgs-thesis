package id.mdgs;

import id.mdgs.dataset.Dataset;
import id.mdgs.glvq.Glvq;
import id.mdgs.glvq.TrainGlvq;
import id.mdgs.master.ITrain;
import id.mdgs.utils.utils;

public class TestElly {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Dataset ds = new Dataset(utils.getDefaultPath() + "/resources/ecgdata/with-header/data.12.86.2000Samples/12.86f_2000_nonoutliers.bwr_300_gabung.txt");
		ds.load();
		
		Glvq net = new Glvq();
		net.initCodes(ds);
		
		ITrain train = new TrainGlvq(net, ds, 0.05);
		train.setMaxEpoch(150);
		
		do{
			train.iteration();
			System.out.println(train.getCurrEpoch() + ": " + train.getError());
		}while(!train.shouldStop());
		
		
		
		
	}

}
