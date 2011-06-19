package id.mdgs.thesis.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.jfree.ui.RefineryUtilities;
import org.jfree.util.WaitingImageObserver;

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
