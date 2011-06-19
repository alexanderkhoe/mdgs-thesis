package id.mdgs.thesis.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public interface IMonitor {
	public void updateDataset(int epoch);
	public JFreeChart getChart();
	public ChartPanel getPanel();
}
