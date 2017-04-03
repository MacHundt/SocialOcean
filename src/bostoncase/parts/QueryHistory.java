package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class QueryHistory {
	
	private static QueryHistory INSTANCE;
	public static boolean isInitialized = false;
	
	private StyledText history;
	
	@Inject
	public QueryHistory() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		history = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		history.setDoubleClickEnabled(false);
//		console.setScrollBars( false);
		history.setEditable(false);
		INSTANCE = this;
		isInitialized = true;
	}
	
	
	
	@Focus
	public void onFocus() {
		history.setFocus();
	}
	
	
//	@Inject
//	@Optional
//	public void receiveActivePart(@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
//		if (activePart != null) {
//			outputConsole("Active part changed " + activePart.getLabel());
//		}
//	}
	
	
	public void addQuery(String output) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					history.setText(history.getText()+"\n"
							+ output);
					history.update();
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(" Could not print to Console");
					// app.showStatus(t.getMessage());
				}
			}
		});
	}
	
	
	public void clearHistory() {
		history.setText("");
	}
	
	
	public static QueryHistory getInstance() {
         return INSTANCE;
	}
}
