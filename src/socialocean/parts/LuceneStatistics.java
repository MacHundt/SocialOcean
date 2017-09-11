package socialocean.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class LuceneStatistics {
	
	private static LuceneStatistics INSTANCE;
	public static boolean isInitialized = false;
	
	private StyledText console;
	
	@Inject
	public LuceneStatistics() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		console = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
		console.setDoubleClickEnabled(false);
//		console.setBar  AlwaysShowScrollBars(false);
		console.setEditable(false);
		INSTANCE = this;
		isInitialized = true;
	}
	
	
	
	@Focus
	public void onFocus() {
		console.setFocus();
	}
	
	
	public void printLuceneStatistics(String output) {
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			public void run() {
				try {
					
					console.setTabs(8);
					console.append(output+"\n");
					
//					String [] stat = output.split(":");
//					if (stat.length != 2)
//						return;
//					int first = 0;
//					int indexOfColon = output.indexOf(":");
//					StyleRange styleRange = new StyleRange();
//					styleRange.start = first;
//					styleRange.length = first+indexOfColon;
//					styleRange.fontStyle = SWT.BOLD;
////					styleRange.foreground = display.getSystemColor(SWT.ITALIC );
//					console.setStyleRange(styleRange);
					
					
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(" Could not print to Console");
					// app.showStatus(t.getMessage());
				}
			}
		});
	}
	
	
	public static LuceneStatistics getInstance() {
         return INSTANCE;
	}
	
}