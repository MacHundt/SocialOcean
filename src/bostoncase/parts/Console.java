package bostoncase.parts;

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

public class Console {
	
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
	}
	
	
	
	@Focus
	public void onFocus() {
		console.setFocus();
	}
	
	
	@Inject
	@Optional
	public void receiveActivePart(
	        @Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
	        if (activePart != null) {
	                System.out.println("Active part changed "
	                                + activePart.getLabel());
	                outputConsole("Active part changed "
	                                + activePart.getLabel());
	        }
	}
	
	
	public void outputConsole(String output) {
		console.setText(console.getText()+"\n"
				+ output);
	}
	
	
}