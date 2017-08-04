 
package bostoncase.parts;

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
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import impl.GraphCreatorThread;
import impl.TimeLineCreatorThread;
import utils.HistogramEntry;
import utils.Lucene;
import utils.Lucene.TimeBin;


public class Histogram {
	

	private static Histogram INSTANCE;
	public static boolean isInitialized = false;
	
	private Object[][] initialData = null;
	
	@Inject
	public Histogram() {
		
	}
	
	Chart chart;
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
	
	
	// TODO  -- Comparative View .. getChildren() ... dispose(), setVisible()
	// TODO	 -- Switch to log-Scale
	/**
	 * Set up the initial Chart Settings
	 */
	private void prepareChart() {

		parent.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				org.eclipse.swt.widgets.Control[] children = parent.getChildren();

//				children[0].setVisible(false);
//				children[0].setEnabled(false);
				
				for (int i = 0; i < children.length; i++) {
					children[i].dispose();
				}

				chart = new Chart(parent, SWT.COLOR_BLACK);

				// set titles
				chart.getTitle().setText("");
				chart.getAxisSet().getYAxis(0).getTitle().setText("Count");
				chart.getAxisSet().getXAxis(0).getTitle().setText("Categories");
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
		
		ArrayList<HistogramEntry> arrEntry = new ArrayList<>(counter.size()); 
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
				// for every category a barSeries
				
//				for (HistogramEntry entry : arrEntry) {
//					HistogramEntry entry = counter.get(categories);
//					String[] none = {" "};
//					double[] val = {entry.getCount()};
//					double avgSen = entry.getAvgSentiment();
//					
//					chart.getAxisSet().getXAxis(0).setCategorySeries(none);
//					barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
//							SeriesType.BAR, entry.getName() );
//					
//					barSeries.setDescription(entry.getName());
//					barSeries.setYSeries(val);
//					barSeries.getLabel().setFormat("##.0");
////					barSeries.setBarColor(entry.getCategoryColor());
//					barSeries.setBarColor(entry.getAvgSentimentColor());
//				}
//				chart.setToolTipText(barSeries.getDescription());
//				
//				chart.getAxisSet().adjustRange();
//				chart.redraw();
				
//				barSeries.getLabel().setVisible(false);
//				chart.getPlotArea().addListener(SWT.Paint, event -> addLabelsToBars(arrEntry, event));
//				chart.addListener(SWT.Paint, event -> addLabelsToBars(arrEntry, event));
				
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
				if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY))
					chart.getPlotArea().addListener(SWT.Paint, event -> changeBarColors(arrEntry, event, true));
				else {
					chart.getPlotArea().addListener(SWT.Paint, event -> changeBarColors(arrEntry, event, false));
				}
				chart.getPlotArea().addListener(SWT.MouseDoubleClick, event -> mouseDoubleClicked(categories, event));
				
			}
			
		});
		
	}
	
	protected void mouseDoubleClicked(String[] categories, Event event) {
		GC gc = event.gc;
		
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
				ScoreDoc[] result = null;
				try {
					Query q = l.getParser().parse(query);
					result = l.query(q, type, true, true);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (result == null)
					return;
				
//				Color c1 = new Color(event.display, 50, 50, 200);		// selection color
//				gc.setBackground(c1);
//				gc.drawRectangle(barRec.x,barRec.y , barRec.width, barRec.height);
				
				TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
					@Override
					public void execute() {
						l.changeTimeLine(TimeBin.HOURS);
					}
				};
				lilt.start();
				
				GraphCreatorThread graphThread = new GraphCreatorThread(l) {
					
					@Override
					public void execute() {
						l.createGraphView();
					}
				};
				graphThread.start();
				
				// Show in MAP  --> Clear LIST = remove all Markers
				l.showInMap(result, true);
				l.changeHistogramm(result);
				
				l.createGraphML_Mention(result, true);
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


	protected void changeBarColors(ArrayList<HistogramEntry> arrEntry, Event event, boolean catColor) {
		GC gc = event.gc;
		ISeries series = chart.getSeriesSet().getSeries()[0];
		IBarSeries bars = (IBarSeries) series;
		Rectangle[] barRecs = bars.getBounds();
		
		int i = 0;
		for (Rectangle barRec : barRecs) {
			HistogramEntry entry = arrEntry.get(i++);
			if (catColor)
				gc.setBackground(getCategoryColor(entry.getName()));
			else
				gc.setBackground(entry.getAvgSentimentColor());
			gc.fillRectangle(barRec.x,barRec.y , barRec.width, barRec.height);
			
		}
	}


//	private void addLabelsToBars(ArrayList<HistogramEntry> arrEntry, Event event) {
//		int height = chart.getBounds().height;
//		int width = chart.getBounds().width;
//		int x = chart.getBounds().x;
//		int y = chart.getBounds().y;
//		
//		int areaWidth = chart.getPlotArea().getBounds().width;
//		Point plotAreaStartPoint = chart.getPlotArea().getLocation();
//		
//		GC gc = event.gc;
//		Color color = new Color(Display.getDefault(), 200, 200, 200);
//		int padding = barSeries.getBarPadding()-10;
//		int barwidth = barSeries.getBounds()[0].width-1;
//		
//		int i = 0;
//		
//		for (ISeries series : chart.getSeriesSet().getSeries()) {
//			IBarSeries bars = (IBarSeries) series;
//			Rectangle[] barRec = bars.getBounds();
//			
//			Color c1 = new Color(event.display, 50, 50, 200);
//			gc.setBackground(c1);
//			for (HistogramEntry e : arrEntry) {
//				if (e.getName().equals(series.getId())) {
//					gc.setBackground(e.getAvgSentimentColor());
//				}
//			}
//	        gc.fillRectangle(barRec[0].x,barRec[0].y , barRec[0].width, barRec[0].height);
//			
//		}
		
//		for (HistogramEntry e : arrEntry) {
//			i++;
////			gc.setForeground(e.getCategoryColor());
////			gc.setBackground(color);
//			gc.drawText(fitToBarWith(e.getName(), 13), (int) (barwidth*1.2)+padding+(i*(barwidth)), height - 20, SWT.CENTER);
////			System.out.println(e.getName() +" At position: ("+( (barwidth*1.2)+padding+(i*(barwidth)))+", "+(height-20)+")");
//			
//		}
//		
//	}
	
	
//	private String fitToBarWith(String name, int maxLetters) {
//		String out = "";
//		if (name.length() < maxLetters) {
//			return name;
//		} else {
//			out = name.substring(0, maxLetters-3)+"..";
//			return out;
//		}
//	}


	public static Histogram getInstance() {
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
				
				chart.getPlotArea().addListener(SWT.Paint, event -> changeBarToCatColors(categories, event));
				chart.getPlotArea().addListener(SWT.MouseDoubleClick, event -> mouseDoubleClicked(categories, event));
			}
		});
	}
	
	
	protected void changeBarToCatColors(String[] categories, Event event) {
		GC gc = event.gc;
		ISeries series = chart.getSeriesSet().getSeries()[0];
		IBarSeries bars = (IBarSeries) series;
		Rectangle[] barRecs = bars.getBounds();
		
		int i = 0;
		for (Rectangle barRec : barRecs) {
			String cat = categories[i++];
			gc.setBackground(getCategoryColor(cat));
			gc.fillRectangle(barRec.x,barRec.y , barRec.width, barRec.height);
			
		}
	}


	private Color getCategoryColor(String category) {
		Color color = new Color(Display.getDefault(), 0, 0, 0);
		
		Color pink = new Color(Display.getDefault(), 250, 22, 129);		// Default
		
		switch (category.toLowerCase()) {
		case "computers_technology":
			color = new Color(Display.getDefault(), 166,206,227);
			break;
		case "education":
			color = new Color(Display.getDefault(), 31,120,180);
			break;
		case "family":
			color = new Color(Display.getDefault(), 178,223,138);
			break;
		case "food":
			color = new Color(Display.getDefault(), 51,160,44);
			break;
		case "health":
			color = new Color(Display.getDefault(), 251,154,153);
			break;
		case "marketing":
			color = new Color(Display.getDefault(), 227,26,28);
			break;
		case "music":
			color = new Color(Display.getDefault(), 253,191,111);
			break;
		case "news_media":
			color = new Color(Display.getDefault(), 255,127,0);
			break;
		case "other":
			color = new Color(Display.getDefault(), 202,178,214);
			break;
		case "pets":
			color = new Color(Display.getDefault(), 106,61,154);
			break;
		case "politics":
			color = new Color(Display.getDefault(), 255,255,153);
			break;
		case "recreation_sports":
			color = new Color(Display.getDefault(), 177,89,40);
			break;

		default:
			color = pink;
			break;
		}
		
//		switch (category.toLowerCase()) {
//		case "computers_technology":
//			color = new Color(Display.getDefault(), 228, 26, 28);
//			break;
//		case "education":
//			color = new Color(Display.getDefault(), 102, 99, 14);
//			break;
//		case "family":
//			color = new Color(Display.getDefault(), 65, 148, 134);
//			break;
//		case "food":
//			color = new Color(Display.getDefault(), 91, 157, 90);
//			break;
//		case "health":
//			color = new Color(Display.getDefault(), 145, 87, 155);
//			break;
//		case "marketing":
//			color = new Color(Display.getDefault(), 218, 109, 59);
//			break;
//		case "music":
//			color = new Color(Display.getDefault(), 255, 174, 19);
//			break;
//		case "news_media":
//			color = new Color(Display.getDefault(), 247, 240, 50);
//			break;
//		case "other":
//			color = new Color(Display.getDefault(), 182, 117, 42);
//			break;
//		case "pets":
//			color = new Color(Display.getDefault(), 210, 109, 122);
//			break;
//		case "politics":
//			color = new Color(Display.getDefault(), 221, 136, 181);
//			break;
//		case "recreation_sports":
//			color = new Color(Display.getDefault(), 153, 153, 153);
//			break;
//
//		default:
//			color = pink;
//			break;
//		}
		
		return color;
	}
	
	
}