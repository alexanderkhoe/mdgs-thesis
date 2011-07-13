package id.mdgs.gui;

import id.mdgs.dataset.Dataset.Entry;
import id.mdgs.utils.MathUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 * 
 * @author I Made Agus Setiawan
 *
 */
public class MultiChart extends ApplicationFrame {
	
	public static class CodeMonitor  {
		public CategoryDataset dataset;
		public JFreeChart chart;
		public ChartPanel chartPanel;
		public int id;
		Entry code;
		
		public CodeMonitor(int id){
			this.id = id;
			code = new Entry(300);
			code.label = id;
			code.set(0);
			updateEntry(code);
			dataset = createDataset();
			chart = createChart(dataset);
			chartPanel = createChartPanel(chart);
//			dataset.addChangeListener(this);
		}
		
		public void updateEntry(Entry e){
			for(int i=0;i<e.size();i++){
				e.data[i] = MathUtils.randomDouble(-5, 5);
			}
		}
		
		private CategoryDataset createDataset() {
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for(int i=0;i < code.size();i++){
				dataset.addValue(code.data[i], String.format("%d",code.label), String.format("%d",i));
			}
			
//			dataset.addValue(212, "Classes", "JDK 1.0");
//			dataset.addValue(504, "Classes", "JDK 1.1");
//			dataset.addValue(1520, "Classes", "SDK 1.2");
//			dataset.addValue(1842, "Classes", "SDK 1.3");
//			dataset.addValue(2991, "Classes", "SDK 1.4");
			
			return dataset;
		}
		
		public void updateDataset() {
			
//			System.out.println("Triggered");
			DefaultCategoryDataset ds = (DefaultCategoryDataset) dataset;
			updateEntry(code);
			for(int i=0;i < code.size();i++){
				ds.setValue(code.data[i], String.format("%d",code.label), String.format("%d",i));
			}			
//			ds.setValue(MathUtils.randomDouble(-2, 2), "Classes", "JDK 1.0");
//			ds.setValue(MathUtils.randomDouble(-2, 2), "Classes", "JDK 1.1");
//			ds.setValue(MathUtils.randomDouble(-2, 2), "Classes", "SDK 1.2");
//			ds.setValue(MathUtils.randomDouble(-2, 2), "Classes", "SDK 1.3");
//			ds.setValue(MathUtils.randomDouble(-2, 2), "Classes", "SDK 1.4");
		}
		
		private JFreeChart createChart(CategoryDataset dataset) {
			// create the chart...
			JFreeChart chart = ChartFactory.createLineChart(
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
			
			chart.addSubtitle(new TextTitle("Number of Classes By Release"));
			TextTitle source = new TextTitle(
					"Source: Java In A Nutshell (4th Edition) "
					+ "by David Flanagan (O'Reilly)"
			);
			source.setFont(new Font("SansSerif", Font.PLAIN, 10));
			source.setPosition(RectangleEdge.BOTTOM);
			source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
			chart.addSubtitle(source);
			
			/*set color*/
			chart.setBackgroundPaint(Color.white);
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setBackgroundPaint(Color.lightGray);
			plot.setRangeGridlinePaint(Color.white);
			
			// customise the range axis...
			/*label*/
			Font fnLabel = new Font("SansSerif", Font.PLAIN, 12);
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setLabel("Features");
			domainAxis.setLabelFont(fnLabel);
			
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setLabel("Amplitude");
			rangeAxis.setLabelFont(fnLabel);
//			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			//set fix range
			rangeAxis.setRange(-1.5, 1.5);
			
			// customise the renderer...
			LineAndShapeRenderer renderer
			= (LineAndShapeRenderer) plot.getRenderer();
//			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
//			renderer.setFillPaint(Color.white);
			return chart;
		}
		
		public ChartPanel createChartPanel(JFreeChart chart){
			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(4, 4, 4, 4),
			BorderFactory.createLineBorder(Color.black)));
			
			return chartPanel;
		}
	}
	
	
	/**/
	public CodeMonitor[] cms;
	public static int npanel = 10;
	public MultiChart(String title) {
		super(title);
		init();
	}

	public void init() {
	    // Add internal frame to desktop
	    JDesktopPane desktop = new JDesktopPane();
	    desktop.setLayout(new GridLayout(4,3));
	    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	    cms = new CodeMonitor[npanel];
	    for(int i=0;i< npanel;i++){
	    	cms[i] = new CodeMonitor(i);
	    	desktop.add(cms[i].chartPanel);
//	    	MemoryUsageDemo panel = new MemoryUsageDemo(3000);
//	    	panel.new DataGenerator(100).start();
//	    	desktop.add(panel);
	    	
	    }

	    
	    
	    // Display the desktop in a top-level frame
//	    getContentPane().add(desktop, BorderLayout.CENTER);
//	    setBounds(100, 100, 800, 300);
	    setContentPane(desktop);
	    setExtendedState(MAXIMIZED_BOTH);
	    setVisible(true);
	 
	  }
	
	public void update(){
		for(int i=0;i < npanel;i++){
			cms[i].chart.setNotify(false);
		}
		
		for(int i=0;i < npanel;i++){
			cms[i].updateDataset();
		}

		for(int i=0;i < npanel;i++){
			cms[i].chart.setNotify(true);
		}

	}
	 
	
	class DataGenerator extends Timer implements ActionListener {
		/**
		* Constructor.
		*
		* @param interval the interval (in milliseconds)
		*/
		DataGenerator(int interval) {
			super(interval, null);
			addActionListener(this);
		}
		/**
		* Adds a new free/total memory reading to the dataset.
		*
		* @param event the action event.
		* */
		public void actionPerformed(ActionEvent event) {
			update();
		}
	}
	
	public JInternalFrame createIFrame(String title, boolean resizable,
			boolean closeable, boolean maximizable, boolean iconifiable){
		JInternalFrame iframe = new JInternalFrame(title, resizable, closeable, maximizable, iconifiable);
		 
	    // Set an initial size
//		    int width = 200;
//		    int height = 100;
//		    iframe.setSize(width, height);
//		iframe.setContentPane(createChartPanel());
	    // By default, internal frames are not visible; make it visible
//	    iframe.setVisible(true);
	 
	    // Add components to internal frame...
	    //iframe.getContentPane().add(new JTextArea("Hello"));

	    return iframe;
	}
		

		
		

		
}
