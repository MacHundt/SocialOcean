 
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

public class Time {
	
	private static Time INSTANCE;
	public static boolean isInitialized = false;
	@Inject
	public Time() {
		
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
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

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
	
	public static Time getInstance() {
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
	
	
//	private XYDataset createDataset() {
//		
////		final XYSeries series1 = new XYSeries("Twitter Activity");
////		final TimeSeries series2 = new TimeSeries("Twitter Activity", LocalDateTime.class);
//		
//		  TimePeriodValues series = new TimePeriodValues("Test Series");
//	     
//
//		
////		for (int i = 0; i < merged_bins.size(); i++) {
////			Bin bin = merged_bins.get(i);
//////			series1.add(i, bin.getCount());
////			
////			// circular Chart  --> MonthDay
//////			series1.add(bin.getFrom().getDayOfMonth(), merged_bins.get(i-1).getCount());
////			
////			// circular Chart  --> Weekday
//////			series1.add(bin.getFrom().getDayOfWeek().getValue(), merged_bins.get(i-1).getCount());
////			
////			// Overall activity --> binning size, take the FROM / Start datetime from each BIN
////			long utc = bin.getFrom().toEpochSecond(ZoneOffset.UTC);
////			series1.add(utc, merged_bins.get(i).getCount());
////			
////			
//////			RegularTimePeriod jan1st2002 = new Day(bin.getFrom().getDate());
//////		    series.add(jan1st2002, new Integer(42));
////			
//////			series2.add(new TimeSeriesDataItem(period, (double) bin.getCount()));
////			
////		}
//		
////		series1.getData().add(new XYChart.Data("Jan", 23));
//		
//		final XYSeries series2 = new XYSeries("Second");
//		series2.add(1.0, 5.0);
//		series2.add(2.0, 7.0);
//		series2.add(3.0, 6.0);
//		series2.add(4.0, 8.0);
//		series2.add(5.0, 4.0);
//		series2.add(6.0, 4.0);
//		series2.add(7.0, 2.0);
//		series2.add(8.0, 1.0);
//
//		final XYSeries series3 = new XYSeries("Third");
//		series3.add(3.0, 4.0);
//		series3.add(4.0, 3.0);
//		series3.add(5.0, 2.0);
//		series3.add(6.0, 3.0);
//		series3.add(7.0, 6.0);
//		series3.add(8.0, 3.0);
//		series3.add(9.0, 4.0);
//		series3.add(10.0, 3.0);
//
//		XYSeriesCollection dataset = new XYSeriesCollection();
////		dataset.addSeries(series1);
//		dataset.addSeries(series2);
//		dataset.addSeries(series3);
//
//		return dataset;
////		return null;
//		
//	}
	
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
}