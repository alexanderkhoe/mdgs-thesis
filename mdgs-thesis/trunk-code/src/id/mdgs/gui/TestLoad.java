package id.mdgs.gui;

import javax.swing.JFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class TestLoad {
	public static void main(String [] args){
//		JIFDemo demo = new JIFDemo();
		MultiChart demo = new MultiChart("Codebook Monitor");
//		demo.init();
		
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setExtendedState(JFrame.MAXIMIZED_BOTH);
        demo.setVisible(true);
        
        demo.new DataGenerator(1000).start();
	}
	
	
}
