package socialocean.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.swtchart.Chart;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;
import org.swtchart.ISeries.SeriesType;

import impl.GraphCreatorThread;
import impl.TimeLineCreatorThread;
import socialocean.model.Result;
import utils.HistogramEntry;
import utils.Lucene;
import utils.Lucene.TimeBin;

public class HashtagHistogram {
	
	private static HashtagHistogram INSTANCE;
	public static boolean isInitialized = false;
	
	private Object[][] initialData = null;
	
	@Inject
	public HashtagHistogram() {
		
	}
	
	Chart chart;
	ArrayList<HistogramEntry> arrEntry;
	IBarSeries barSeries;
	Color bar_color = new Color(Display.getDefault(), 164, 205, 253);
	Color bar_color_selected = new Color(Display.getDefault(), 32, 48, 89);	
	Color pink = new Color(Display.getDefault(), 250, 22, 129);
	Color black = new Color(Display.getDefault(), 0, 0, 0);
	Color gray = new Color(Display.getDefault(), 204, 204, 204);		// grey
	Composite parent;
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		this.parent = parent;
		INSTANCE = this;
		isInitialized = true;
	}
	private void prepareChart() {

		parent.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				org.eclipse.swt.widgets.Control[] children = parent.getChildren();

				for (int i = 0; i < children.length; i++) {
					children[i].dispose();
				}

				chart = new Chart(parent, SWT.COLOR_BLACK);

				// set titles
				chart.getTitle().setText("");
				chart.getAxisSet().getYAxis(0).getTitle().setText("Count");
				chart.getAxisSet().getXAxis(0).getTitle().setText("Hashtags");
				chart.getAxisSet().getXAxis(0).getTitle().setVisible(false);
				chart.getAxisSet().getXAxis(0).enableCategory(true);
				chart.setForeground(black);
				chart.getTitle().setForeground(black);
				chart.getAxisSet().getXAxis(0).getTitle().setForeground(black);
				chart.getAxisSet().getYAxis(0).getTitle().setForeground(black);
				chart.getAxisSet().getYAxis(0).getTitle().setVisible(true); 
//				Font font = chart.getAxisSet().getYAxis(0).getTitle().getFont();
//				System.out.println(font.getFontData().toString());
				IAxisTick xTick = chart.getAxisSet().getXAxis(0).getTick();
				Font font = new Font(Display.getDefault(), "Arial", 16, SWT.NORMAL);
				xTick.setFont(font);
				
				chart.getAxisSet().getXAxis(0).getGrid().setForeground(gray);
				chart.getAxisSet().getYAxis(0).getGrid().setForeground(gray);
				
				chart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
//				chart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.DASHDOTDOT);
				chart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);
				
				chart.getAxisSet().getXAxis(0).getTick().setForeground(black);
				chart.getAxisSet().getYAxis(0).getTick().setForeground(black);
				chart.getLegend().setForeground(black);

				// chart.getLegend().setVisible(true);
				chart.getLegend().setVisible(false);
				chart.getAxisSet().getYAxis(0).enableLogScale(true);

				// adjust the axis range
				chart.setRedraw(true);
				chart.setEnabled(true);
			}

		});

	}
	
	
	/**
	 * Update histogram, sort bars according to the average sentiment
	 * @param counter
	 */
	public void changeDataSet(HashMap<String, HistogramEntry> counter) {
		
		// SORT
		// java8
//		counter.values().stream().sorted();
		
		arrEntry = new ArrayList<>(counter.size()); 
		for (HistogramEntry e : counter.values())
			arrEntry.add(e);
		Collections.sort(arrEntry, Collections.reverseOrder());
		
		
		prepareChart();
		
		if (counter.size() < 1)
			return;
		
		double[] dataSeries = new double[arrEntry.size()];
		String[] categories = new String[arrEntry.size()];
		int i = 0;
		for (HistogramEntry entry : arrEntry) {
			dataSeries[i] = entry.getCount();
			categories[i] = entry.getName();
			i++;
		}
		
		parent.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {

				chart.getAxisSet().getXAxis(0).setCategorySeries(categories);
				barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
						SeriesType.BAR, "categories ");
				
				barSeries.setYSeries(dataSeries);
				barSeries.getLabel().setFormat("##.0");
				barSeries.setBarColor(gray);
				barSeries.getLabel().setVisible(false);
				chart.getAxisSet().adjustRange();
				chart.redraw();
				
				Lucene l = Lucene.INSTANCE;
			
				chart.getPlotArea().addListener(SWT.MouseDoubleClick, event -> mouseDoubleClicked(categories, event));
				
			}
			
		});
		
	}
		
	
	
	protected void mouseDoubleClicked(String[] categories, Event event) {
		
		Rectangle rec = event.getBounds();
		Point p = new Point(rec.x, rec.y);
		System.out.print("Mouse double clicked on .. ");
		ISeries series = chart.getSeriesSet().getSeries()[0];
		IBarSeries bars = (IBarSeries) series;
		Rectangle[] barRecs = bars.getBounds();
		int i = 0;
		for (Rectangle barRec : barRecs) {
			
			String cat = categories[i++];
			if (isPointinRec(p, barRec)) {
				System.out.println(cat);
				
				String type = "FUSE";
				Lucene l = Lucene.INSTANCE;
				while (!l.isInitialized) {
					return;
				}
				String query = "category:"+cat;
				try {
					Query q = l.getParser().parse(query);
					Result result = l.query(q, type, true, true);
					ScoreDoc[] data = result.getData();
					if (data == null)
						return;
					
					TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
						@Override
						public void execute() {
							result.setTimeCounter(l.createTimeBins(TimeBin.HOURS, data));
							l.showInTimeLine(result.getTimeCounter());
//							l.changeTimeLine(TimeBin.MINUTES);
						}
					};
					lilt.start();
					
					GraphCreatorThread graphThread = new GraphCreatorThread(l) {
						
						@Override
						public void execute() {
							l.createGraphView(data);
//							l.createSimpleGraphView(data);
						}
					};
					graphThread.start();
					
					// Show in MAP  --> Clear LIST = remove all Markers
//					l.initCountriesMap();
					l.createMapMarkers(data, true);
					l.changeHistogramm(result.getHistoCounter());
					
//					l.createGraphML_Mention(result, true);
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		}
		
	}


	private boolean isPointinRec(Point p, Rectangle barRec) {
		boolean out = false;
		if (p.x > barRec.x && p.x < (barRec.x + barRec.width) &&
				p.y > barRec.y  && p.y < (barRec.y + barRec.height ) ) {
			
			out = true;
		}
		
		return out;
	}




	public static HashtagHistogram getInstance() {
        return INSTANCE;
	}
	
	
	@Focus
	public void onFocus() {
		chart.setFocus();
	}


	
	public void setInitialData(Object[][] resulTable) {
		initialData = resulTable;
	}
	
	
	
	/**
	 * Reset histogram view, set back to initial dataSet.
	 */
	public void viewInitialDataSet() {
		prepareChart();
		double[] dataSeries = new double[initialData.length];
		String[] categories = new String[initialData.length];
		
		for (int j = 0; j < initialData.length; j++) {
			dataSeries[j] = (Integer) initialData[j][1];
			categories[j] = (String) initialData[j][0];
		}
		
		parent.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				
				chart = (Chart) parent.getChildren()[0];
				chart.getAxisSet().getXAxis(0).setCategorySeries(categories);
				barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
						SeriesType.BAR, "categories ");
				
				barSeries.setYSeries(dataSeries);
				barSeries.getLabel().setFormat("##.0");
				barSeries.setBarColor(gray);
				
				
				barSeries.getLabel().setVisible(false);
				
				chart.getAxisSet().adjustRange();
				chart.redraw();
				chart.getPlotArea().addListener(SWT.MouseDoubleClick, event -> mouseDoubleClicked(categories, event));
			}
		});
	}
	

}
