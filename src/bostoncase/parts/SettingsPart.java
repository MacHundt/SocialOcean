 
package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SettingsPart {
	@Inject
	public SettingsPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(6, false));
		
		Button btnNegativ = new Button(parent, SWT.CHECK);
		btnNegativ.setText("negativ");
		btnNegativ.setSelection(true);
		
		btnNegativ.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(btnNegativ.getText() + " is turned to: "+btnNegativ.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		Button btnNeutrag = new Button(parent, SWT.CHECK);
		btnNeutrag.setText("neutral");
		btnNeutrag.setSelection(true);
		btnNeutrag.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(btnNeutrag.getText() + " is turned to: "+btnNeutrag.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		Button btnPositiv = new Button(parent, SWT.CHECK);
		btnPositiv.setText("positiv");
		btnPositiv.setSelection(true);
		btnPositiv.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(btnPositiv.getText() + " is turned to: "+btnPositiv.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		
		Label lblColor = new Label(parent, SWT.NONE);
		lblColor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblColor.setText("ColorScheme:");
		
		Combo combo = new Combo(parent, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.add("Sentiment");
		combo.add("Category");
		combo.select(0);
		
		combo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(combo.getText());
				// TODO change colorShema
				// --> Color Tweets
				// --> Color Nodes
				// --> Color Histogram
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}
	
	
	
	@Focus
	public void onFocus() {
		
	}
	
	
}