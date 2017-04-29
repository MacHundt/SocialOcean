 
package bostoncase.parts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
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
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import utils.TimeLineHelper;

public class Time {
	
	private static Time INSTANCE;
	public static boolean isInitialized = false;
	
    private Marker marker;
    private Double markerStart = Double.NaN;
    private Double markerEnd = Double.NaN;
	@Inject
	public Time() {
		
	}
	
	Composite comp;
	TimeSeriesCollection dataset;
	ChartPanel panel;
	
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
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(plotTitle, xaxis, yaxis, dataset, show, toolTips, urls );
		
		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
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
		panel.setDomainZoomable(true);
		panel.setRangeZoomable(false);
		
		panel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
				markerEnd = getPosition(e, panel);

//				if (marker != null) {
//					plot.removeDomainMarker(marker, Layer.BACKGROUND);
//				}
//				if (!(markerStart.isNaN() && markerEnd.isNaN())) {
//					if (markerEnd > markerStart) {
//						marker = new IntervalMarker(markerStart, markerEnd);
//						marker.setPaint(new Color(0xDD, 0xFF, 0xDD, 0x80));
//						marker.setAlpha(0.5f);
//						plot.addDomainMarker(marker, Layer.BACKGROUND);
//					}
//				}
//
//				String type = panel.getChart().getPlot().getPlotType();
//				double up = panel.getChart().getXYPlot().getDomainAxis().getRange().getUpperBound();
//				Number maximum = DatasetUtilities.findMaximumDomainValue(dataset);
				
//				System.out.println();
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				 markerStart = getPosition(e, panel);				
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
	
	 private Double getPosition(MouseEvent e, ChartPanel panel){
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
		
	}
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
}