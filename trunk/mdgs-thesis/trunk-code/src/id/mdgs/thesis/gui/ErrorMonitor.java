/**
 * 
 */
package id.mdgs.thesis.gui;

import id.mdgs.dataset.Dataset.Entry;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 * @author I Made Agus Setiawan
 *
 */
public class ErrorMonitor implements Interface.IErrorMonitor {

	public XYSeriesCollection dataset;
	public JFreeChart chart;
	public ChartPanel chartPanel;
	public double rMin = -0.0001, 
				  rMax =  1;
	public int maxEpoch;
	
	protected ErrorMonitor(){
	}
	
	public ErrorMonitor(int maxEpoch){
		this(maxEpoch, 1);
	}
	
	public ErrorMonitor(int maxEpoch, int numSeries){
		this.maxEpoch = maxEpoch; 
		dataset = createDataset(numSeries);
		chart = createChart(dataset);
		chartPanel = createChartPanel(chart);
		
		/*set color*/
		Color[] colorTable = {Color.BLUE, Color.RED, Color.CYAN, Color.YELLOW, Color.GREEN };
		for(int i=0;i < numSeries;i++){
			ChartUtils.setSeriesColor(chart, i, colorTable[i % colorTable.length]);
		}
	}
	
	
	private XYSeriesCollection createDataset(int num) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for(int i=0;i < num;i++){
			XYSeries series = new XYSeries("series" + i);
			series.setMaximumItemCount(this.maxEpoch);

			dataset.addSeries(series);
		}
		
		return dataset;
	}
	
	public void updateError(int epoch, double[] error) {
		
		for(int i=0;i < dataset.getSeriesCount();i++){
			XYSeries series = dataset.getSeries(i);
			series.add(epoch, error[i]);
		}
		
		((TextTitle)chart.getSubtitle(0)).setText(String.format("Epoch: #%d, Error: %7.4f", epoch, error[0]));
	}

	public void updateError(int epoch, double error) {
		XYSeries series = dataset.getSeries(0);
		series.add(epoch, error);
		
		((TextTitle)chart.getSubtitle(0)).setText(String.format("Epoch: #%d, Error: %7.4f", epoch, error));
	}
	
	protected JFreeChart createChart(XYSeriesCollection dataset) {
		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		null, //"Java Standard Class Library", // chart title
		null, //"Release", // domain axis label
		null, //"Class Count", // range axis label
		dataset, // data
		PlotOrientation.VERTICAL, // orientation
		false, // include legend
		true, // tooltips
		false // urls
		);
		
		/*title*/
		TextTitle title = new TextTitle("Error Rate");
		title.setFont(new Font("SansSerif", Font.PLAIN, 14));
		chart.setTitle(title);
		
		/*set color*/
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);
		
		// customise the range axis...
		/*label*/
		Font fnLabel = new Font("SansSerif", Font.PLAIN, 12);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setLabel("Features");
		domainAxis.setLabelFont(fnLabel);
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		domainAxis.setRange(0, this.maxEpoch);
		
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabel("Amplitude");
		rangeAxis.setLabelFont(fnLabel);
		//rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		//set fix range
		rangeAxis.setRange(rMin, rMax);
		
		
		/*Epoch label*/
		TextTitle source = new TextTitle("Epoch: #");
		source.setFont(new Font("SansSerif", Font.PLAIN, 10));
		source.setPosition(RectangleEdge.BOTTOM);
		source.setHorizontalAlignment(HorizontalAlignment.LEFT);
		chart.addSubtitle(source);				
		
		// customise the renderer...
		XYLineAndShapeRenderer renderer
		= (XYLineAndShapeRenderer) plot.getRenderer();
//		renderer.setShapesVisible(true);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
//		renderer.setFillPaint(Color.white);
		return chart;
	}
	
	public ChartPanel createChartPanel(JFreeChart chart){
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createEmptyBorder(4, 4, 4, 4),
		BorderFactory.createLineBorder(Color.black)));
		
		return chartPanel;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public ChartPanel getPanel() {
		return chartPanel;
	}


}
