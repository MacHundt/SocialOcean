 
package bostoncase.parts;

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


public class Histogram {
	

	private static Histogram INSTANCE;
	public static boolean isInitialized = false;
	
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
	
	
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		chart = new Chart(parent, SWT.COLOR_BLACK);
		
		 // set titles
        chart.getTitle().setText("");
        chart.getAxisSet().getYAxis(0).getTitle().setText("Count");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Categories");
		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.setForeground(black);
		chart.getTitle().setForeground(black);
		chart.getAxisSet().getXAxis(0).getTitle().setForeground(black);
		chart.getAxisSet().getYAxis(0).getTitle().setForeground(black);
		chart.getAxisSet().getXAxis(0).getGrid().setForeground(black);
		chart.getAxisSet().getYAxis(0).getGrid().setForeground(black);
		chart.getAxisSet().getXAxis(0).getTick().setForeground(black);
		chart.getAxisSet().getYAxis(0).getTick().setForeground(black);
		chart.getLegend().setForeground(black);
        
        chart.getLegend().setVisible(true);
		
        // adjust the axis range
		chart.setRedraw(true);
		chart.setEnabled(true);

		INSTANCE = this;
		isInitialized = true;
	}
	
	
	public void chnageDataSet(Object[][] resulTable) {
		
		double[] dataSeries = new double[resulTable.length];
		String[] categories = new String[resulTable.length];
		
		for (int i = 0; i < resulTable.length; i++) {
			dataSeries[i] = (Integer) resulTable[i][1];
			categories[i] = (String) resulTable[i][0];
		}
		
		chart.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				// for every category a barSeries
				int i = 0;
				for (String s : categories) {
					String[] cat = {s};
					double[] val = {dataSeries[i]};
					chart.getAxisSet().getXAxis(0).setCategorySeries(cat);
					// set Color for Cat
					barSeries = (IBarSeries) chart.getSeriesSet().createSeries(
							SeriesType.BAR, s );
					barSeries.setYSeries(val);
					barSeries.getLabel().setFormat("##.0");
					
					barSeries.setBarColor(getColor(s));
					barSeries.setBarPadding(5);
					
					i++;
					
				}
				chart.getAxisSet().adjustRange();
				chart.redraw();
				
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
//				barSeries.setBarPadding(35);
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
	
	
	private Color getColor(String catName) {
		Color color = new Color(Display.getDefault(), 0, 0, 0);
		
		switch (catName.toLowerCase()) {
		case "computers_technology":
			color = new Color(Display.getDefault(), 228, 26, 28);
			break;
		case "education":
			color = new Color(Display.getDefault(), 102, 99, 14);
			break;
		case "family":
			color = new Color(Display.getDefault(), 65, 148, 134);
			break;
		case "food":
			color = new Color(Display.getDefault(), 91, 157, 90);
			break;
		case "health":
			color = new Color(Display.getDefault(), 145, 87, 155);
			break;
		case "marketing":
			color = new Color(Display.getDefault(), 218, 109, 59);
			break;
		case "music":
			color = new Color(Display.getDefault(), 255, 174, 19);
			break;
		case "news_media":
			color = new Color(Display.getDefault(), 247, 240, 50);
			break;
		case "other":
			color = new Color(Display.getDefault(), 182, 117, 42);
			break;
		case "pets":
			color = new Color(Display.getDefault(), 210, 109, 122);
			break;
		case "politics":
			color = new Color(Display.getDefault(), 221, 136, 181);
			break;
		case "recreation_sports":
			color = new Color(Display.getDefault(), 153, 153, 153);
			break;

		default:
			color = pink;
			break;
		}
		
		return color;
	}
	
	
	
	public static Histogram getInstance() {
        return INSTANCE;
	}
	
	
	
	
	@Focus
	public void onFocus() {
		chart.setFocus();
//		comp.setFocus();
	}
	
	
}