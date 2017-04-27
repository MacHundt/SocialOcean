 
package bostoncase.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import utils.HistogramEntry;


public class Histogram {
	

	private static Histogram INSTANCE;
	public static boolean isInitialized = false;
	
	private Object[][] initialData = null;
	
	@Inject
	public Histogram() {
		
	}
	
//	Composite comp;
//	DefaultCategoryDataset dataset ;
	
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
				chart.getAxisSet().getXAxis(0).getGrid().setForeground(gray);
				chart.getAxisSet().getYAxis(0).getGrid().setForeground(gray);
				
				chart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
				chart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.DOT);
				
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
	
	
	public void changeDataSet(HashMap<String, HistogramEntry> counter) {
		
		// SORT
		ArrayList<HistogramEntry> arrEntry = new ArrayList<>(counter.size()); 
		for (HistogramEntry e : counter.values())
			arrEntry.add(e);
		Collections.sort(arrEntry);
		
		prepareChart();
		
		parent.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				// for every category a barSeries
				
				for (HistogramEntry entry : arrEntry) {
//					HistogramEntry entry = counter.get(categories);
					String[] none = {" "};
					double[] val = {entry.getCount()};
					double avgSen = entry.getAvgSentiment();
					
					chart.getAxisSet().getXAxis(0).setCategorySeries(none);
					barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
							SeriesType.BAR, entry.getName() );
					System.out.println(entry.getName());
					barSeries.setYSeries(val);
					barSeries.getLabel().setFormat("##.0");
//					barSeries.setBarColor(entry.getCategoryColor());
					barSeries.setBarColor(entry.getAvgSentimentColor());
				}
				
				barSeries.getLabel().setVisible(false);
				
				chart.getAxisSet().adjustRange();
				chart.redraw();
				
				
				
//				int i = 0;
//				for (String cat : categories) {
//					String[] none = {" "};
//					double[] val = {dataSeries[i]};
//					chart.getAxisSet().getXAxis(0).setCategorySeries(none);
//					// set Color for Cat
//					barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
//							SeriesType.BAR, cat );
//					barSeries.setYSeries(val);
//					barSeries.getLabel().setFormat("##.0");
//					
//					barSeries.setBarColor(getColor(cat));
//					barSeries.setBarPadding(5);
//					
//					i++;
//					
//				}
//				chart.getAxisSet().adjustRange();
//				chart.redraw();
				
//				chart.getAxisSet().getXAxis(0).setCategorySeries(categories);
//				
//				barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
//						SeriesType.BAR, "categories ");
//				
//				barSeries.setYSeries(dataSeries);
//				barSeries.getLabel().setFormat("##.0");
////				barSeries.setBarColor(bar_color);
//				barSeries.setBarColor(pink);
//				
//				barSeries.getLabel().setVisible(false);
//				
//				chart.getAxisSet().adjustRange();
//				chart.redraw();
			}
		});
		
//		comp.getDisplay().asyncExec(new Runnable() {
//			
//			@Override
//			public void run() {
//				for (int i = 0; i < resulTable.length; i++) {
//					Integer val = (Integer) resulTable[i][1];
//					String col = (String) resulTable[i][0];
//					dataset.addValue(val.doubleValue() , "category" , col  );
//				}
//			}
//		});
		
	}
	
	
	
	public static Histogram getInstance() {
        return INSTANCE;
	}
	
	
	@Focus
	public void onFocus() {
		chart.setFocus();
//		comp.setFocus();
	}


	
	public void setInitialData(Object[][] resulTable) {
		initialData = resulTable;
	}
	
	
	
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
				
//				int i = 0;
//				for (String cat : categories) {
//					String[] none = {" "};
//					double[] val = {dataSeries[i]};
//					chart.getAxisSet().getXAxis(0).setCategorySeries(none);
//					// set Color for Cat
//					barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
//							SeriesType.BAR, cat );
//					barSeries.setYSeries(val);
//					barSeries.getLabel().setFormat("##.0");
//					
//					barSeries.setBarColor(getColor(cat));
//					barSeries.setBarPadding(5);
//					
//					i++;
//					
//				}
//				chart.getAxisSet().adjustRange();
//				chart.redraw();
				
				
				chart.getAxisSet().getXAxis(0).setCategorySeries(categories);
				barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
						SeriesType.BAR, "categories ");
				
				barSeries.setYSeries(dataSeries);
				barSeries.getLabel().setFormat("##.0");
				barSeries.setBarColor(gray);
				
				barSeries.getLabel().setVisible(false);
				
				chart.getAxisSet().adjustRange();
				chart.redraw();
			}
		});
	}
	
	
}