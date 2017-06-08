 
package bostoncase.parts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;

import org.apache.lucene.search.ScoreDoc;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javafx.util.converter.NumberStringConverter;
import utils.Lucene;
import utils.TimeLineHelper;

public class Time {
	
	private static Time INSTANCE;
	public static boolean isInitialized = false;
	
    private Marker marker;
    private Long markerStart;
    private Long markerEnd;
    
    private XYPlot plot;
    
    long last_lowerBound;
    long last_upperBound;
    
	@Inject
	public Time() {
		
	}
	
	Composite comp;
	TimeSeriesCollection dataset;
	ChartPanel panel;
	JFreeChart chart;
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(comp);
		
		JApplet rootContainer = new JApplet();
		
		TimeSeries series = new TimeSeries("Timeline");
		dataset = new TimeSeriesCollection();
		
		String plotTitle = "";
		String xaxis = "Time";
		String yaxis = "Docs";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = true;
		boolean urls = false;
		
		chart = ChartFactory.createTimeSeriesChart(plotTitle, xaxis, yaxis, dataset, show, toolTips, urls );
		
		// get a reference to the plot for further customisation...
		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setOutlinePaint(Color.white);
		plot.getRangeAxis().setLabel("");
		plot.getDomainAxis().setLabel("");
		ValueAxis y_axis = plot.getRangeAxis();		// Y
		ValueAxis x_axis = plot.getDomainAxis(); 	// X
		Font font = new Font("Veranda", Font.PLAIN, 12);
		y_axis.setTickLabelFont(font);
		x_axis.setTickLabelFont(font);
		x_axis.setTickLabelPaint(Color.black);
		y_axis.setTickLabelPaint(Color.black);
		plot.getDomainAxis().setAxisLineVisible(false);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		// renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesVisible(0, false);
		plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
//		panel.setDomainZoomable(false);
		panel.setDomainZoomable(true);
		panel.setRangeZoomable(false);
		
		panel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
//				markerEnd = getPosition(e).longValue();
//				Number maximum = DatasetUtilities.findMaximumRangeValue(dataset);
//
//				XYPlot plot = chart.getXYPlot();
//
//				// Number lower =
//				// plot.getDomainAxis().getRange().getLowerBound();
//				// Number upper =
//				// plot.getDomainAxis().getRange().getUpperBound();
//
//				if (marker != null) {
//					plot.removeDomainMarker(marker, Layer.BACKGROUND);
//				}
//				if (markerStart != null && markerEnd != null) {
//					if (markerEnd > markerStart) {
//						marker = new IntervalMarker(markerStart.longValue(), markerEnd.longValue());
//						// marker.setPaint(new Color(0xDD, 0xFF, 0xDD,
//						// 0x80));
//
//					} else {
//						marker = new IntervalMarker(markerEnd.longValue(), markerStart.longValue());
//					}
//
//					marker.setPaint(Color.blue);
//					marker.setAlpha(0.6f);
//					marker.setLabel("1");
//					marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
//					plot.addDomainMarker(marker, Layer.BACKGROUND);
//				}
				
				Plot p = chart.getPlot();
				System.out.println("GET Domain Range:");
				Number low = plot.getDomainAxis().getRange().getLowerBound();
				Number up = plot.getDomainAxis().getRange().getUpperBound();
				
				boolean zoom = false;
				if (low.longValue() > last_lowerBound && low.longValue() < last_upperBound 
						&& up.longValue() > last_lowerBound && up.longValue() < last_upperBound	)
					zoom = true;
				
				last_lowerBound = low.longValue();
				last_upperBound = up.longValue();
				
				// TimeSearch From low to up!
				Lucene l = Lucene.INSTANCE;
				ScoreDoc[] result = null;
				while (!l.isInitialized) {
					return;
				}

				Date date = new Date(low.longValue());
				Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
				String from =  format.format(date);
				date = new Date(up.longValue());
				String to = format.format(date);
				
//				result = l.searchTimeRange(low.longValue(), up.longValue(), true, true);
				if (l.getLastResult() != null && zoom) {
					result = l.searchTimeRange(low.longValue(),  up.longValue(), true,  true);
					l.showInMap(result, true);
					l.changeHistogramm(result);
					l.createGraphML_Mention(result, true);
				} else {
					// back --> == zoom out
					l.showLastResult();
				}
				
//				System.out.println(">>LOW: "+.getDomainAxis().getRange().getLowerBound());
					
//					ValueMarker maxX = new ValueMarker(markerEnd.longValue());
//					maxX.setPaint(Color.blue);
//					maxX.setLabel("Max");
//					maxX.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
//					plot.addDomainMarker(maxX, Layer.BACKGROUND);
//					
//					ValueMarker minX = new ValueMarker(markerStart.longValue());
//					minX.setPaint(Color.blue);
//					minX.setLabel("Min");
//					minX.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
//					plot.addDomainMarker(minX, Layer.BACKGROUND);
				
//				ValueMarker max = new ValueMarker(maximum.floatValue());
//				max.setPaint(Color.orange);
//				// max.setLabel("Highest Value");
//				max.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
//				plot.addRangeMarker(max, Layer.BACKGROUND);
//
//				Number xValue = DatasetUtilities.findMinimumDomainValue(dataset);
//				for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
//					for (int itemIndex = 0; itemIndex < dataset.getItemCount(seriesIndex); itemIndex++) {
//						Number yValue = dataset.getY(seriesIndex, itemIndex);
//						Number X = dataset.getX(seriesIndex, itemIndex);
//
//						Long xlong = X.longValue();
//
//						if (yValue.equals(maximum)) {
//							if (dataset.getX(seriesIndex, itemIndex).floatValue() > xValue.floatValue())
//								xValue = dataset.getX(seriesIndex, itemIndex);
//						}
//					}
//				}
//				ValueMarker maxX = new ValueMarker(xValue.floatValue());
//				maxX.setPaint(Color.orange);
//				maxX.setLabel("Maximum");
//				maxX.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
//				plot.addDomainMarker(maxX, Layer.BACKGROUND);
				
				
			

				
			}
			
//			private long normalizeDate(long date, int precision) {
//				String dateStr = ""+date;
//				dateStr = dateStr.substring(0, precision);
//				long out = Long.parseLong(dateStr);
//
//				return out;
//			}

			@Override
			public void mousePressed(MouseEvent e) {
//				EventQueue.invokeLater(new Runnable() {
//					public void run() {
//						markerStart = getPosition(e).longValue();
//
//					}
//				});
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		
		rootContainer.add(panel);
		rootContainer.validate();
 		
		frame.add(rootContainer);
		
		INSTANCE = this;
		isInitialized = true;
	}
	
	/**
	 * Get get position on the Domain Axis.
	 */
	 private Number getPosition(MouseEvent e){
		 
//			EventQueue.invokeLater(new Runnable() {
//				public void run() {
//
//				}
//			});
		 
         Point2D p = panel.translateScreenToJava2D( e.getPoint());
         Rectangle2D plotArea = panel.getScreenDataArea();
         XYPlot plot = (XYPlot) panel.getChart().getPlot();
         return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
     }
	 
	
	public static Time getInstance() {
        return INSTANCE;
	}
	
	
	
	public void changeDataSet(ArrayList<TimeLineHelper> resulTable) {
		
		dataset.removeAllSeries();			// doesn't work
		
		TimeSeries series = new TimeSeries("Timeline");
		comp.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				
//				final XYSeries series = new XYSeries("Documents");
				for (int i = 0; i < resulTable.size(); i++) {
					int freq = resulTable.get(i).getFrequency();
//					series.add((double) resulTable.get(i).getTime().toEpochSecond(ZoneOffset.UTC), (double) freq);
					LocalDateTime d = resulTable.get(i).getTime();
					String timeString = d.toString();
					String[] date = timeString.split("T")[0].split("-");
					String[] time = timeString.split("T")[1].split(":");
					
					int year = Integer.parseInt(date[0]);
					int month = Integer.parseInt(date[1]);
					int day = Integer.parseInt(date[2]);
					
					int hour = Integer.parseInt(time[0]);
					int min = Integer.parseInt(time[1]);
					int sec = Integer.parseInt(time[2]);
					
					series.add(new Hour(hour, day, month, year), (double) freq);
				}
				dataset.addSeries(series);	
//				dataset.setAutoWidth(true);
			}
		});
		panel.restoreAutoBounds();
		panel.revalidate();
		panel.updateUI();
		
		Number low = plot.getDomainAxis().getLowerBound();
		Number up = plot.getDomainAxis().getUpperBound();
		last_lowerBound = low.longValue();
		last_upperBound = up.longValue();
		
	}
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
}