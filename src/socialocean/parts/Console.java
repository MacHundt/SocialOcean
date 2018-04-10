package socialocean.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
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
		console = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BOTTOM );
		console.setDoubleClickEnabled(false);
//		console.setScrollBars( false);
		console.setEditable(false);
		INSTANCE = this;
		isInitialized = true;
	}
	
	
	
	@Focus
	public void onFocus() {
		console.setFocus();
	}
	
	
	public void outputConsoleln(String output) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					console.setText(console.getText()+"\n"
							+ output);
					console.setTopIndex(console.getLineCount() - 1);
					console.update();
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(" Could not print to Console");
					// app.showStatus(t.getMessage());
				}
			}
		});
	}
	
	public void outputConsole(String output) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					console.setText(console.getText()+""
							+ output);
					console.setTopIndex(console.getLineCount() - 1);
					console.update();
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(" Could not print to Console");
					// app.showStatus(t.getMessage());
				}
			}
		});
	}
	
	
	
	public void clear() {
		console.setText("");
		console.update();
	}
	
	
	public static Console getInstance() {
         return INSTANCE;
	}
	
}