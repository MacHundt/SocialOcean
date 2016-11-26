 
package bostoncase.parts;

import java.awt.Color;
import java.awt.Frame;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;


public class Histogram {
	

	private static Histogram INSTANCE;
	public static boolean isInitialized = false;
	
	@Inject
	public Histogram() {
		
	}
	
	Composite comp;
	DefaultCategoryDataset dataset ;
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {

		comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(comp);

		JApplet rootContainer = new JApplet();

		dataset = new DefaultCategoryDataset();
		
		String plotTitle = "";
		String xaxis = "Category";
		String yaxis = "Frequency";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = true;
		boolean urls = false;
//		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis, dataset, orientation, show, toolTips,
//				urls);
		
		JFreeChart chart = ChartFactory.createBarChart(
				plotTitle, 
				xaxis, yaxis, 
		         dataset,
		         orientation, 
		         show, toolTips, urls);
		
		final Plot plot = chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		
		
		ChartPanel panel = new ChartPanel(chart);
		panel.addChartMouseListener(new ChartMouseListener() {
			
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				ChartEntity enti = event.getEntity();
				if (enti instanceof CategoryItemEntity) {
					CategoryItemEntity cie = (CategoryItemEntity) enti;
					String selectedCat = (String) cie.getCategory();
					System.out.println(selectedCat);
					
					// TODO .. do something with it.
					
				}
			}
		});

		rootContainer.add(panel);
		rootContainer.validate();

		frame.add(rootContainer);

		INSTANCE = this;
		isInitialized = true;
	}
	
	
	public void chnageDataSet(Object[][] resulTable) {
		
		comp.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < resulTable.length; i++) {
					Integer val = (Integer) resulTable[i][1];
					String col = (String) resulTable[i][0];
					dataset.addValue(val.doubleValue() , "category" , col  );
				}
			}
		});
		
	}
	
	
	
	public static Histogram getInstance() {
        return INSTANCE;
	}
	
	
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
}