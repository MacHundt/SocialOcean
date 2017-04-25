 
package bostoncase.parts;

import java.awt.Color;
import java.awt.Frame;
import java.time.ZoneOffset;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import utils.TimeLineHelper;

public class Timeline {
	
	private static Timeline INSTANCE;
	public static boolean isInitialized = false;
	@Inject
	public Timeline() {
		
	}
	
	Composite comp;
	XYSeriesCollection dataset;
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(comp);
		
		JApplet rootContainer = new JApplet();
		
		dataset = new XYSeriesCollection();
		
		String plotTitle = "";
		String xaxis = "Time";
		String yaxis = "Docs";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = true;
		boolean urls = false;
		
		JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis, dataset, orientation, show, toolTips,
				urls);


		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.black);
		plot.setRangeGridlinePaint(Color.black);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		// renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesVisible(0, false);
		plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		

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
	
	public static Timeline getInstance() {
        return INSTANCE;
	}
	
	
	
	public void chnageDataSet(ArrayList<TimeLineHelper> resulTable) {
		
		comp.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				
				final XYSeries series = new XYSeries("Documents");
				for (int i = 0; i < resulTable.size(); i++) {
					int freq = resulTable.get(i).getFrequency();
					series.add((double) resulTable.get(i).getTime().toEpochSecond(ZoneOffset.UTC), (double) freq);
				}
				dataset.addSeries(series);
				dataset.setAutoWidth(true);
			}
		});
		
	}
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
}