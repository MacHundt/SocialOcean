package interfaces;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class Console {
	
	private static Console INSTANCE;
	public static boolean isInitialized = false;
	
	private StyledText console;
	
	@Inject
	public Console() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		console = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		console.setDoubleClickEnabled(false);
		console.setAlwaysShowScrollBars(false);
		console.setEditable(false);
		INSTANCE = this;
		isInitialized = true;
	}
	
	
	
	@Focus
	public void onFocus() {
		console.setFocus();
	}
	
	
//	@Inject
//	@Optional
//	public void receiveActivePart(@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
//		if (activePart != null) {
//			outputConsole("Active part changed " + activePart.getLabel());
//		}
//	}
	
	
	public void outputConsole(String output) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					console.setText(console.getText()+"\n"
							+ output);
					console.update();
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(" Could not print to Console");
					// app.showStatus(t.getMessage());
				}
			}
		});
	}
	
	
	public static Console getInstance() {
         return INSTANCE;
	}
	
}