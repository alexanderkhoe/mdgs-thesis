package id.mdgs.thesis.gui;

import java.awt.Color;

import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import id.mdgs.fnlvq.FCodeBook.FEntry;

public class FuzzyCodeMonitor extends CodeMonitor {
	FEntry code;
	
	public FuzzyCodeMonitor(FEntry code) {
		this.id = code.label;
		this.code = code;
		this.dataset = createDataset();
		chart = createChart(dataset);
		chartPanel = createChartPanel(chart);
		
		
		/*set style*/
//		setSeriesStyle(chart, 0, STYLE_LINE);
//		setSeriesStyle(chart, 2, STYLE_LINE);
		
		/*set color*/
		setSeriesColor(chart, 0, Color.RED);
		setSeriesColor(chart, 1, Color.BLUE);
		setSeriesColor(chart, 2, Color.MAGENTA);
	}

	protected XYSeriesCollection createDataset() {
		XYSeries seriesMin 	= new XYSeries("Min");
		XYSeries seriesMean = new XYSeries("Mean");
		XYSeries seriesMax 	= new XYSeries("Max");

		seriesMin.setMaximumItemCount(code.size());
		seriesMean.setMaximumItemCount(code.size());
		seriesMax.setMaximumItemCount(code.size());
		for(int i=0;i < code.size();i++){
			seriesMin.add(i, code.data[i].min);
			seriesMean.add(i, code.data[i].mean);
			seriesMax.add(i, code.data[i].max);
		}

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(seriesMin);
		dataset.addSeries(seriesMean);
		dataset.addSeries(seriesMax);
		return dataset;
	}

	@Override
	public void updateDataset(int epoch) {
		XYSeries seriesMin 	= dataset.getSeries(0);
		XYSeries seriesMean = dataset.getSeries(1);
		XYSeries seriesMax 	= dataset.getSeries(2);		
		for(int i=0;i < code.size();i++){
			seriesMin.updateByIndex(i, code.data[i].min);
			seriesMean.updateByIndex(i, code.data[i].mean);
			seriesMax.updateByIndex(i, code.data[i].max);
		}			
		
		((TextTitle)chart.getSubtitle(0)).setText(String.format("Epoch: #%d", epoch));		
	}

}
