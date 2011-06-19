package id.mdgs.thesis.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
 
public class JIFDemo extends JFrame{
//public static void main(String [] args){
//	new JIFDemo();
//}
 
public JIFDemo()
  {
    // Create an internal frame
//    boolean resizable = false;
//    boolean closeable = false;
//    boolean maximizable  = false;
//    boolean iconifiable = false;
//    String title = "Code 1";
//    JInternalFrame iframe = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
// 
//    // Set an initial size
//    int width = 200;
//    int height = 50;
//    iframe.setSize(width, height);
// 
//    // By default, internal frames are not visible; make it visible
//    iframe.setVisible(true);
// 
//    // Add components to internal frame...
//    iframe.getContentPane().add(new JTextArea("Hello"));
 
    // Add internal frame to desktop
    JDesktopPane desktop = new JDesktopPane();
    desktop.setLayout(new GridLayout(4,3));
    
    for(int i=1;i< 15;i++){
        desktop.add(createIFrame("Code " + i, false, false, false, false));    	
    }

    
    
    // Display the desktop in a top-level frame
    getContentPane().add(desktop, BorderLayout.CENTER);
//    setBounds(100, 100, 800, 300);
   setExtendedState(MAXIMIZED_BOTH);
    setVisible(true);
 
  }
 
	public JInternalFrame createIFrame(String title, boolean resizable,
			boolean closeable, boolean maximizable, boolean iconifiable){
		JInternalFrame iframe = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
		 
	    // Set an initial size
//	    int width = 200;
//	    int height = 100;
//	    iframe.setSize(width, height);
	 
	    // By default, internal frames are not visible; make it visible
	    iframe.setVisible(true);
	 
	    // Add components to internal frame...
	    //iframe.getContentPane().add(new JTextArea("Hello"));
	    
	    return iframe;
	}
} 