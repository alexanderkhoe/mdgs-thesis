package id.mdgs.thesis.gui;

import id.mdgs.lvq.Dataset.Entry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

public class CodeMonitor implements IMonitor {
	public XYSeriesCollection dataset;
	public JFreeChart chart;
	public ChartPanel chartPanel;
	public int id;
	private Entry code;
	public double rMin = -2, 
				  rMax =  2;
	
	protected CodeMonitor(){
	}
	
	public CodeMonitor(Entry code){
		this.id = code.label;
		this.code = code;
		dataset = createDataset();
		chart = createChart(dataset);
		chartPanel = createChartPanel(chart);
		
		/*set color*/
		setSeriesColor(chart, 0, Color.BLUE);
	}
	
//	public void updateEntry(Entry e){
//		for(int i=0;i<e.size();i++){
//			e.data[i] = MathUtils.randomDouble(-5, 5);
//		}
//	}
	
	private XYSeriesCollection createDataset() {
		XYSeries series = new XYSeries("series1");
		series.setMaximumItemCount(code.size());
		for(int i=0;i < code.size();i++){
			series.add(i, code.data[i]);
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		return dataset;
	}
	
	public void updateDataset(int epoch) {
		XYSeries series = dataset.getSeries(0);
		for(int i=0;i < code.size();i++){
			series.updateByIndex(i, code.data[i]);
		}			
		
		((TextTitle)chart.getSubtitle(0)).setText(String.format("Epoch: #%d", epoch));
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
		TextTitle title = new TextTitle("Vector Reference #" + this.id);
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
	
  /**
    * Set color of series.
    * 
    * @param chart JFreeChart.
    * @param seriesIndex Index of series to set color of (0 = first series)
    * @param color New color to set.
    */
   public static void setSeriesColor(JFreeChart chart, int seriesIndex, Color color) {
        if (chart != null) {
            Plot plot = chart.getPlot();
            try {
                if (plot instanceof CategoryPlot) {
                    CategoryPlot categoryPlot = chart.getCategoryPlot();
                    CategoryItemRenderer cir = categoryPlot.getRenderer();
                    cir.setSeriesPaint(seriesIndex, color);
                } else if (plot instanceof PiePlot) {
                    PiePlot piePlot = (PiePlot) chart.getPlot();
                    piePlot.setSectionPaint(seriesIndex, color);
                } else if (plot instanceof XYPlot) {
                    XYPlot xyPlot = chart.getXYPlot();
                    XYItemRenderer xyir = xyPlot.getRenderer();
                    xyir.setSeriesPaint(seriesIndex, color);
                } else {
                    System.out.println("setSeriesColor() unsupported plot: "+plot);
                }
            } catch (Exception e) { //e.g. invalid seriesIndex
                System.err.println("Error setting color '"+color+"' for series '"+seriesIndex+"' of chart '"+chart+"': "+e);
            }
        }//else: input unavailable
    }//setSeriesColor()
   
   /** Line style: line */
   public static final String STYLE_LINE = "line";
   /** Line style: dashed */
   public static final String STYLE_DASH = "dash";
   /** Line style: dotted */
   public static final String STYLE_DOT = "dot";

   /**
   * Convert style string to stroke object.
   * 
   * @param style One of STYLE_xxx.
   * @return Stroke for <i>style</i> or null if style not supported.
   */
   private static BasicStroke toStroke(String style) {
       BasicStroke result = null;
       
       if (style != null) {
           float lineWidth = 0.2f;
           float dash[] = {5.0f};
           float dot[] = {lineWidth};
   
           if (style.equalsIgnoreCase(STYLE_LINE)) {
               result = new BasicStroke(lineWidth);
           } else if (style.equalsIgnoreCase(STYLE_DASH)) {
               result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
           } else if (style.equalsIgnoreCase(STYLE_DOT)) {
               result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
           }
       }//else: input unavailable
       
       return result;
   }//toStroke()

   /**
    * Set color of series.
    * 
    * @param chart JFreeChart.
    * @param seriesIndex Index of series to set color of (0 = first series)
    * @param style One of STYLE_xxx.
    */
   public static void setSeriesStyle(JFreeChart chart, int seriesIndex, String style) {
       if (chart != null && style != null) {
           BasicStroke stroke = toStroke(style);
           
           Plot plot = chart.getPlot();
           if (plot instanceof CategoryPlot) {
               CategoryPlot categoryPlot = chart.getCategoryPlot();
               CategoryItemRenderer cir = categoryPlot.getRenderer();
               try {
                   cir.setSeriesStroke(seriesIndex, stroke); //series line style
               } catch (Exception e) {
                   System.err.println("Error setting style '"+style+"' for series '"+seriesIndex+"' of chart '"+chart+"': "+e);
               }
           } else if (plot instanceof XYPlot) {
               XYPlot xyPlot = chart.getXYPlot();
               XYItemRenderer xyir = xyPlot.getRenderer();
               try {
                   xyir.setSeriesStroke(seriesIndex, stroke); //series line style
               } catch (Exception e) {
                   System.err.println("Error setting style '"+style+"' for series '"+seriesIndex+"' of chart '"+chart+"': "+e);
               }
           } else {
               System.out.println("setSeriesColor() unsupported plot: "+plot);
           }
       }//else: input unavailable
   }//setSeriesStyle() 
}
