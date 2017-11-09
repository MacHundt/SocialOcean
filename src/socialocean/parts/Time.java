 
package socialocean.parts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
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
import org.jfree.data.time.TimeSeriesDataItem;

import impl.GraphCreatorThread;
import socialocean.model.Result;
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
	protected Point endPoint;
	
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
			
			private Point startingPoint;

			@Override
			public void mouseReleased(MouseEvent e) {
				
				Plot p = chart.getPlot();
				endPoint = e.getPoint();
				System.out.println("GET Domain Range:");
				Number low = plot.getDomainAxis().getRange().getLowerBound();
				Number up = plot.getDomainAxis().getRange().getUpperBound();
				
				// CONVERT UTC to Timestamp
//				Date date = new Date(low.longValue());
//				Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
//				String from =  format.format(date);
//				date = new Date(up.longValue());
//				String to = format.format(date);
				
				boolean zoom = false;
				int range1 = (int) startingPoint.getX();
				int range2 = (int) endPoint.getX();
			
				if (startingPoint.getX() == endPoint.getX()) {
					zoom = true;
				}
				
				if (low.longValue() > last_lowerBound && low.longValue() < last_upperBound 
						&& up.longValue() > last_lowerBound && up.longValue() < last_upperBound	)
					zoom = true;
				
				last_lowerBound = low.longValue();
				last_upperBound = up.longValue();
				
				// TimeSearch From low to up!
				Lucene l = Lucene.INSTANCE;
				while (!l.isInitialized) {
					return;
				}

//				result = l.searchTimeRange(low.longValue(), up.longValue(), true, true);
//				if (l.getLastResult() != null && zoom) {
				if (!zoom) {
					Result  result = l.searchTimeRange(low.longValue(),  up.longValue(), true,  true);
					ScoreDoc[] data = result.getData();
					l.changeHistogramm(result.getHistoCounter());
					
					l.createMapMarkers(data, true);
					GraphCreatorThread graphThread = new GraphCreatorThread(l) {
						
						@Override
						public void execute() {
							l.createGraphView(result.getData());
							
						}
					};
					graphThread.start();
					
//					l.createGraphML_Mention(result, true);
				} else {
					// back --> == zoom out
					l.showLastResult();
				}
				
			}
			

			@Override
			public void mousePressed(MouseEvent e) {
				
				startingPoint = e.getPoint();
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
					
//					if (date.length != 3 || time.length != 3) {
//						System.out.println(">>> no full timestamp: "+timeString);
//					}
						
					int year = Integer.parseInt(date[0]);
					int month = Integer.parseInt(date[1]);
					int day = Integer.parseInt(date[2]);
					
					int hour = Integer.parseInt(time[0]);
					int min = Integer.parseInt(time[1]);
//					int sec = Integer.parseInt(time[2]);
					
					Hour h = new Hour(hour, day, month, year);
//					Minute m = new Minute(min, h);
					
					TimeSeriesDataItem test = series.getDataItem(h);
					if (test != null) {
						System.out.println("same period");
						if (freq > 0) {
							series.addOrUpdate(h, (double) freq);
//							series.addOrUpdate(m, (double) freq);
						}
					}
					else {
						series.add(h, (double) freq);
//						series.add(m, (double) freq);
					}
				}
				dataset.addSeries(series);	
//				dataset.setAutoWidth(true);
				panel.restoreAutoBounds();
				panel.revalidate();
				panel.repaint();
			}
		});
		
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