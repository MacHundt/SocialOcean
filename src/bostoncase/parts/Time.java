 
package bostoncase.parts;

import java.awt.Frame;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Time {
	@Inject
	public Time() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
//		Button btn = new Button(parent, SWT.FILL);
		Composite comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(comp);
		
		XYDataset dataset = createDataset();
//		JFreeChart chart = createLineChart(dataset);
//		ChartPanel chartPanel = new ChartPanel(chart);
//		chartPanel.setPreferredSize(new Dimension(800, 300));
//		frame.add(chartPanel);
	}
	
	
//	private JFreeChart createLineChart(XYDataset dataset) {
//		 // create the chart...
//       final JFreeChart chart = ChartFactory.createXYLineChart(
//           "Twitter Activity",      // chart title
//           "Time Bin",                      // x axis label
//           "Tweet_Count",                   // y axis label
//           dataset,                  // data
//           PlotOrientation.VERTICAL,
//           true,                     // include legend
//           true,                     // tooltips
//           false                     // urls
//       );
//
//
////       final StandardLegend legend = (StandardLegend) chart.getLegend();
// //      legend.setDisplaySeriesShapes(true);
//       
//       // get a reference to the plot for further customisation...
//       final XYPlot plot = chart.getXYPlot();
//       plot.setBackgroundPaint(Color.lightGray);
//       plot.setDomainGridlinePaint(Color.white);
//       plot.setRangeGridlinePaint(Color.white);
//       
//       final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
////       renderer.setSeriesLinesVisible(0, false);
//       renderer.setSeriesShapesVisible(0, false);
//       plot.setRenderer(renderer);
//
//       // change the auto tick unit selection to integer units only...
//       final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//       rangeAxis.setAutoRange(true);
////       rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//       // OPTIONAL CUSTOMISATION COMPLETED.
//               
//       return chart;
//	}

	
	private XYDataset createDataset() {
		
//		final XYSeries series1 = new XYSeries("Twitter Activity");
//		final TimeSeries series2 = new TimeSeries("Twitter Activity", LocalDateTime.class);
		
		  TimePeriodValues series = new TimePeriodValues("Test Series");
	     

		
//		for (int i = 0; i < merged_bins.size(); i++) {
//			Bin bin = merged_bins.get(i);
////			series1.add(i, bin.getCount());
//			
//			// circular Chart  --> MonthDay
////			series1.add(bin.getFrom().getDayOfMonth(), merged_bins.get(i-1).getCount());
//			
//			// circular Chart  --> Weekday
////			series1.add(bin.getFrom().getDayOfWeek().getValue(), merged_bins.get(i-1).getCount());
//			
//			// Overall activity --> binning size, take the FROM / Start datetime from each BIN
//			long utc = bin.getFrom().toEpochSecond(ZoneOffset.UTC);
//			series1.add(utc, merged_bins.get(i).getCount());
//			
//			
////			RegularTimePeriod jan1st2002 = new Day(bin.getFrom().getDate());
////		    series.add(jan1st2002, new Integer(42));
//			
////			series2.add(new TimeSeriesDataItem(period, (double) bin.getCount()));
//			
//		}
		
//		series1.getData().add(new XYChart.Data("Jan", 23));
		
		final XYSeries series2 = new XYSeries("Second");
		series2.add(1.0, 5.0);
		series2.add(2.0, 7.0);
		series2.add(3.0, 6.0);
		series2.add(4.0, 8.0);
		series2.add(5.0, 4.0);
		series2.add(6.0, 4.0);
		series2.add(7.0, 2.0);
		series2.add(8.0, 1.0);

		final XYSeries series3 = new XYSeries("Third");
		series3.add(3.0, 4.0);
		series3.add(4.0, 3.0);
		series3.add(5.0, 2.0);
		series3.add(6.0, 3.0);
		series3.add(7.0, 6.0);
		series3.add(8.0, 3.0);
		series3.add(9.0, 4.0);
		series3.add(10.0, 3.0);

//		XYSeriesCollection dataset = new XYSeriesCollection();
//		dataset.addSeries(series1);
//		dataset.addSeries(series2);
//		dataset.addSeries(series3);
//
//		return dataset;
		return null;
		
	}
	
	
	
	@Focus
	public void onFocus() {
		
	}
	
	
	@Persist
	public void save() {
		
	}
	
}