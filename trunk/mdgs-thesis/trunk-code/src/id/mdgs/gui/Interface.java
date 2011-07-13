package id.mdgs.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class Interface {
	
	public static interface IMonitor {
		public JFreeChart getChart();
		public ChartPanel getPanel();
	}
	
	public static interface ICodeMonitor extends IMonitor {
		public void updateDataset(int epoch);		
	}

	public static interface IErrorMonitor extends IMonitor {
		public void updateError(int epoch, double error);
		public void updateError(int epoch, double[] error);	
	}
	
}
