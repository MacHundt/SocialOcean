package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class Time {
	
	
	
	@Inject
	public Time() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setText("New Button");
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.setText("New Button");
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
	}
	
	
	@Persist
	public void save() {
		
	}
	
}