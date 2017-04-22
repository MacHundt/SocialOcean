 
package bostoncase.parts;

import java.awt.Frame;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;


public class CategoriesPart {
	

	private static CategoriesPart INSTANCE;
	public static boolean isInitialized = false;
	
	@Inject
	public CategoriesPart() {
		
	}
	
	Composite comp;
//	DefaultCategoryDataset dataset ;
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		comp = new Composite(parent, SWT.NONE | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(comp);

		JApplet rootContainer = new JApplet();

		String plotTitle = "";
		String xaxis = "Category";
		String yaxis = "Frequency";
		
	    CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();

		
//		rootContainer.add(panel);
		rootContainer.validate();

		frame.add(rootContainer);


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
		
		
		comp.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < resulTable.length; i++) {
					Integer val = (Integer) resulTable[i][1];
					String col = (String) resulTable[i][0];
					
//					dataset.addValue(val.doubleValue() , "category" , col  );
				}
			}
		});
		
	}
	
	
	private Color getColor(String catName) {
		Color back = new Color(Display.getDefault(), 0, 0, 0);
		
		switch(catName) {
		}
		
		return back;
	}
	
	
	
	public static CategoriesPart getInstance() {
        return INSTANCE;
	}
	
	
	
	
	@Focus
	public void onFocus() {
		comp.setFocus();
	}
	
	
}
