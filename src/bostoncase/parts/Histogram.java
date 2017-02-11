 
package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jfree.data.category.DefaultCategoryDataset;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;


public class Histogram {
	

	private static Histogram INSTANCE;
	public static boolean isInitialized = false;
	
	@Inject
	public Histogram() {
		
	}
	
//	Composite comp;
//	DefaultCategoryDataset dataset ;
	
	Chart chart;
	IBarSeries barSeries;
	Color bar_color = new Color(Display.getDefault(), 164, 205, 253);
	Color bar_color_selected = new Color(Display.getDefault(), 32, 48, 89);
	
	Color pink = new Color(Display.getDefault(), 250, 22, 129);
	Color black = new Color(Display.getDefault(), 0, 0, 0);
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		chart = new Chart(parent, SWT.COLOR_BLACK);
		
		 // set titles
        chart.getTitle().setText("");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Categories");
        chart.getAxisSet().getYAxis(0).getTitle().setText("Frequency");
		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.setForeground(black);
		chart.getTitle().setForeground(black);
		chart.getAxisSet().getXAxis(0).getTitle().setForeground(black);
		chart.getAxisSet().getYAxis(0).getTitle().setForeground(black);
		chart.getAxisSet().getXAxis(0).getGrid().setForeground(black);
		chart.getAxisSet().getYAxis(0).getGrid().setForeground(black);
		chart.getAxisSet().getXAxis(0).getTick().setForeground(black);
		chart.getAxisSet().getYAxis(0).getTick().setForeground(black);
		chart.getLegend().setForeground(black);
        
        chart.getLegend().setVisible(false);
		
        // adjust the axis range
        
		chart.setRedraw(true);
		chart.setEnabled(true);

      

//		comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
//		Frame frame = SWT_AWT.new_Frame(comp);
//
//		JApplet rootContainer = new JApplet();
//
//		dataset = new DefaultCategoryDataset();
//		
//		String plotTitle = "";
//		String xaxis = "Category";
//		String yaxis = "Frequency";
//		PlotOrientation orientation = PlotOrientation.VERTICAL;
//		boolean show = false;
//		boolean toolTips = true;
//		boolean urls = false;
//		
//		JFreeChart chart = ChartFactory.createBarChart(
//				plotTitle, 
//				xaxis, yaxis, 
//		         dataset,
//		         orientation, 
//		         show, toolTips, urls);
//		
////		JFreeChart chart = ChartFactory.createXYBarChart(plotTitle, xaxis, false,
////				yaxis, dataset, orientation, show, toolTips, urls);
//		
//		Plot plot = chart.getPlot();
//		chart.setBorderVisible(false);
//		chart.setBackgroundPaint(java.awt.Color.WHITE);
//		chart.getTitle().setPaint(java.awt.Color.BLACK);
//		plot.setBackgroundPaint(java.awt.Color.white);
//		
////		final XYPlot plot = chart.getXYPlot();
////		plot.setBackgroundPaint(java.awt.Color.white);
////		plot.setDomainGridlinePaint(java.awt.Color.black);
////		plot.setRangeGridlinePaint(java.awt.Color.black);
//		
////		BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
////		barRenderer.setBarPainter(new BarPainter);
////		
////		barRenderer.setSeriesPaint(0, bar_color);
////		plot.setBackgroundPaint(Color.lightGray);
//		
//		
//		ChartPanel panel = new ChartPanel(chart);
//		panel.addChartMouseListener(new ChartMouseListener() {
//			
//			@Override
//			public void chartMouseMoved(ChartMouseEvent event) {
//				// TODO Auto-generated method stub
//			}
//			
//			@Override
//			public void chartMouseClicked(ChartMouseEvent event) {
//				ChartEntity enti = event.getEntity();
//				if (enti instanceof CategoryItemEntity) {
//					CategoryItemEntity cie = (CategoryItemEntity) enti;
//					String selectedCat = (String) cie.getCategory();
//					System.out.println(selectedCat);
//					
//					// TODO .. do something with it.
//					
//				}
//			}
//		});
//
//		rootContainer.add(panel);
//		rootContainer.validate();
//
//		frame.add(rootContainer);


		INSTANCE = this;
		isInitialized = true;
	}
	
	
	public void chnageDataSet(Object[][] resulTable) {
		
		double[] dataSeries = new double[resulTable.length];
		String[] categories = new String[resulTable.length];
		
		for (int i = 0; i < resulTable.length; i++) {
			dataSeries[i] = (Integer) resulTable[i][1];
			categories[i] = (String) resulTable[i][0];
		}
		
		chart.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				chart.getAxisSet().getXAxis(0).setCategorySeries(categories);
				
				barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
						SeriesType.BAR, "bar series");
				barSeries.setYSeries(dataSeries);
				barSeries.getLabel().setFormat("##.0");
//				barSeries.setBarColor(bar_color);
				barSeries.setBarColor(pink);
				barSeries.setBarPadding(35);
				chart.getAxisSet().adjustRange();
				chart.redraw();
			}
		});
		
		
		
//		comp.getDisplay().asyncExec(new Runnable() {
//			
//			@Override
//			public void run() {
//				for (int i = 0; i < resulTable.length; i++) {
//					Integer val = (Integer) resulTable[i][1];
//					String col = (String) resulTable[i][0];
//					dataset.addValue(val.doubleValue() , "category" , col  );
//				}
//			}
//		});
		
	}
	
	
	
	public static Histogram getInstance() {
        return INSTANCE;
	}
	
	
	
	
	@Focus
	public void onFocus() {
		chart.setFocus();
//		comp.setFocus();
	}
	
	
}