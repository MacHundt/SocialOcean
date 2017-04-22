 
package bostoncase.parts;

import java.awt.EventQueue;
import java.awt.Frame;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XChartPanel;


public class CategoriesPart {
	

	private static CategoriesPart INSTANCE;
	public static boolean isInitialized = false;
	
	@Inject
	public CategoriesPart() {
		
	}
	CategoryChart chart;
	Composite comp;
	XChartPanel<CategoryChart> chartPanel;
//	DefaultCategoryDataset dataset ;
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(comp);

		JApplet rootContainer = new JApplet();

		String plotTitle = "";
		String xaxis = "Category";
		String yaxis = "Count";
		
	    chart = new CategoryChartBuilder().xAxisTitle("Categories").yAxisTitle("Count").build();
	    chart.getStyler().setHasAnnotations(false);
	    chart.getStyler().setLegendVisible(true);
	  
	    chartPanel = new XChartPanel<CategoryChart>(chart);
	    
	    chart.addSeries(" ", Arrays.asList(new String[] { " " }), Arrays.asList(new Integer[] {0}));

	    
//	    chart.addSeries("Sport", Arrays.asList(new String[] { " " }), Arrays.asList(new Integer[] { 4 }));
//	    chart.addSeries("Health", Arrays.asList(new String[] { "B" }), Arrays.asList(new Integer[] { 5 }));
//	    chart.addSeries("News", Arrays.asList(new String[] { "C" }), Arrays.asList(new Integer[] { 9 }));
//	    chart.addSeries("Politics", Arrays.asList(new String[] { "D" }), Arrays.asList(new Integer[] { 6 }));
//	    chart.addSeries("Fun", Arrays.asList(new String[] { "E" }), Arrays.asList(new Integer[] { 5 }));
		
		rootContainer.add(chartPanel);
		rootContainer.validate();

		frame.add(rootContainer);


		INSTANCE = this;
		isInitialized = true;
	}
	
	
	public void chnageDataSet(Object[][] resulTable) {
		
		for (String key : chart.getSeriesMap().keySet()) {
			chart.removeSeries(key);
		}
		
		double[] dataSeries = new double[resulTable.length];
		String[] categories = new String[resulTable.length];
		
		for (int i = 0; i < resulTable.length; i++) {
			dataSeries[i] = (Integer) resulTable[i][1];
			categories[i] = (String) resulTable[i][0];
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					int i = 0;
					for (String cat : categories) {
						String[] cat_col = {cat};
						Integer[] val = {i++};
						
						CategorySeries series = chart.addSeries(cat, Arrays.asList(cat_col), Arrays.asList(val));
//						series.setFillColor(getColor(cat));
					}
					chartPanel.revalidate();
					chartPanel.repaint();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
//		comp.getDisplay().asyncExec(new Runnable() {
//			
//			@Override
//			public void run() {
//				int i = 0;
//				for (String cat : categories) {
//					String[] cat_col = {cat};
//					Integer[] val = {i++};
//					
//					CategorySeries series = chart.addSeries(cat, Arrays.asList(cat_col), Arrays.asList(val));
////					series.setFillColor(getColor(cat));
//
//				}
//				chartPanel.revalidate();
//			}
//		});
		
	}
	
	
	private java.awt.Color getColor(String catName) {
		java.awt.Color back = new java.awt.Color(0, 0, 0);
		
		switch(catName) {
		}
		
		return back;
	}
	
	
	
	public static CategoriesPart getInstance() {
        return INSTANCE;
	}
	
	
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
}
