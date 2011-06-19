/**
 * 
 */
package id.mdgs.thesis.gui;

import id.mdgs.fnlvq.FCodeBook;
import id.mdgs.lvq.Dataset;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ApplicationFrame;

/**
 * @author I Made Agus Setiawan
 *
 */
public class CodebookMonitor extends ApplicationFrame implements ActionListener {
		
	private static final long serialVersionUID = 3340716339457233003L;
	/**/
	public IMonitor[] cms;
	public static int npanel = 10;
	public CodebookMonitor(String title, Dataset codebook) {
		super(title);
		init(codebook);
		createMenu();
	}
	
	public CodebookMonitor(String title, FCodeBook codebook) {
		super(title);
		init(codebook);
		createMenu();
	}	
	
	public void init(Dataset ds) {
	    // Add internal frame to desktop
	    JDesktopPane desktop = new JDesktopPane();
	    desktop.setLayout(new GridLayout(4,3));
	    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	    cms = new CodeMonitor[ds.numEntries];
	    for(int i=0;i< ds.numEntries;i++){
	    	cms[i] = new CodeMonitor(ds.get(i));
	    	desktop.add(cms[i].getPanel());
	    }
	    
	    // Display the desktop in a top-level frame
	    setContentPane(desktop);
	    setExtendedState(MAXIMIZED_BOTH);
	    setVisible(true);
	 
	  }
	
	public void init(FCodeBook ds) {
	    // Add internal frame to desktop
	    JDesktopPane desktop = new JDesktopPane();
	    desktop.setLayout(new GridLayout(4,3));
	    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	    cms = new FuzzyCodeMonitor[ds.numEntries];
	    for(int i=0;i< ds.numEntries;i++){
	    	cms[i] = new FuzzyCodeMonitor(ds.get(i));
	    	desktop.add(cms[i].getPanel());
	    }
	    
	    // Display the desktop in a top-level frame
	    setContentPane(desktop);
	    setExtendedState(MAXIMIZED_BOTH);
	    setVisible(true);
	 
	  }
	
	public void createMenu(){
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		
		menuBar = new JMenuBar();
		
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menu = new JMenu("Option");
		menu.setMnemonic(KeyEvent.VK_O);
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Change Vertical Scale", KeyEvent.VK_C);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("AutoRange Range Axis", KeyEvent.VK_A);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
	
	public void update(int epoch){
		for(int i=0;i < cms.length;i++){
			cms[i].getChart().setNotify(false);
		}
		
		for(int i=0;i < cms.length;i++){
			cms[i].updateDataset(epoch);
		}

		for(int i=0;i < cms.length;i++){
			cms[i].getChart().setNotify(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equalsIgnoreCase("Exit")){
			System.out.println("Exit");	
			System.exit(0);
		} else if (e.getActionCommand().equalsIgnoreCase("Change Vertical Scale")){
//			System.out.println("Change Vertical Scale");
			String min = JOptionPane.showInputDialog("Lower Bound:");
			String max = JOptionPane.showInputDialog("Upper Bound:");
			
			double rMin = Double.parseDouble(min);
			double rMax = Double.parseDouble(max);
			
			if(rMax < rMin) {
				JOptionPane.showMessageDialog(null, "Max < Min !!!", "CodebookMonitor", 1);
				return;
			}
			
			for(int i=0;i < cms.length;i++){
				XYPlot plot = (XYPlot) cms[i].getChart().getPlot();
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setRange(rMin, rMax);
			}
		} else if(e.getActionCommand().equalsIgnoreCase("AutoRange Range Axis")){
			for(int i=0;i < cms.length;i++){
				XYPlot plot = (XYPlot) cms[i].getChart().getPlot();
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setAutoRange(true);
			}
		}
		
		
	}
}
