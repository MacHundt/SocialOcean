 
package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
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
	
	
	public void outputConsole(String output) {
		console.setText(console.getText()+"\n"
				+ output);
	}
	
	
}