 
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

import utils.Lucene;

public class SettingsPart {
	@Inject
	public SettingsPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(6, false));
		
		Button mentions = new Button(parent, SWT.CHECK);
		mentions.setText("mentions");
		mentions.setSelection(true);
		
		mentions.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(mentions.getText() + " is turned to: "+mentions.getSelection());
				
				Lucene l = Lucene.INSTANCE;
				l.setWithMentions(mentions.getSelection());
				l.createGraphView();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		Button follows = new Button(parent, SWT.CHECK);
		follows.setText("follows");
		follows.setSelection(true);
		follows.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(follows.getText() + " is turned to: "+follows.getSelection());
				
				Lucene l = Lucene.INSTANCE;
				l.setWithFollows(follows.getSelection());
				l.createGraphView();
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
//		Button btnPositiv = new Button(parent, SWT.CHECK);
//		btnPositiv.setText("positiv");
//		btnPositiv.setSelection(true);
//		btnPositiv.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//				System.out.println(btnPositiv.getText() + " is turned to: "+btnPositiv.getSelection());
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//			}
//		});
		
		
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
//				System.out.println(combo.getText());
				// TODO change colorShema
				// --> Color Tweets
				// --> Color Nodes
				// --> Color Histogram	-- DONE
				
				Lucene l = Lucene.INSTANCE;
				l.setColorScheme(combo.getText());
				l.showInMap(l.getLastResult(), true);
				l.changeHistogramm(l.getLastResult());
				
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