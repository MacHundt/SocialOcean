 
package socialocean.parts;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import impl.MapPanelCreator;
import socialocean.model.Result;
import utils.Lucene;

public class SettingsPart {
	
	Group group;
	Button follows;
	Button mentions;
	Display display = Display.getCurrent();
	org.eclipse.swt.graphics.Color blue = display.getSystemColor(SWT.COLOR_BLUE);
	org.eclipse.swt.graphics.Color red = display.getSystemColor(SWT.COLOR_RED);
	org.eclipse.swt.graphics.Color white = display.getSystemColor(SWT.COLOR_WHITE);
	
	private static Button countries;
	
	@Inject
	public SettingsPart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(10, false));
		
		mentions = new Button(parent, SWT.CHECK );
		mentions.setText("mentions");
		mentions.setSelection(true);
		
		mentions.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(mentions.getText() + " is turned to: "+mentions.getSelection());
				
				Lucene l = Lucene.INSTANCE;
				l.setWithMentions(mentions.getSelection());
				Result result = l.getLastResult();
				if (result != null) {
					l.createGraphView(l.getLastResult().getData());
//					l.createSimpleGraphView(result.getData());
					l.createMapMarkers(result.getData(), true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		mentions.setEnabled(true);
		
		
		follows = new Button(parent, SWT.CHECK );
		follows.setText("follows");
		follows.setSelection(false);
		follows.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println(follows.getText() + " is turned to: "+follows.getSelection());
				
				Lucene l = Lucene.INSTANCE;
				l.setWithFollows(follows.getSelection());
				Result result = l.getLastResult();
				if (result != null) {
					l.createGraphView(l.getLastResult().getData());
//					l.createSimpleGraphView(result.getData());
					l.createMapMarkers(result.getData(), true);
				}
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		follows.setEnabled(true);
		
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
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		combo.add("Sentiment");
		combo.add("Category");
		combo.select(0);
		
		combo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Lucene l = Lucene.INSTANCE;
				l.setColorScheme(combo.getText());
				Histogram histo = Histogram.getInstance();
				histo.changeBarColor();
				l.changeEdgeColor();
				
//				Result r = l.getLastResult();
//				if (r == null)
//					return;
//				l.createMapMarkers(l.getLastResult().getData(), true);
//				l.changeHistogramm(l.getLastResult().getHistoCounter());
//				l.showInTimeLine(l.getLastResult().getTimeCounter());
//				l.createGraphView();					// see ColorScheme
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				combo.select(0);
			}
		});
		
		
		group = new Group(parent, SWT.NONE);
		group.setEnabled(true);
		group.setLayout(new GridLayout(2,true));
		
		
		Button tweets = new Button(group, SWT.CHECK);
		tweets.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		tweets.setBackground(blue);
		tweets.setForeground(white);
		tweets.setText("Tweets");
		tweets.setSelection(Lucene.SHOWTweet);
		tweets.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				System.out.println(tweets.getText() + " is turned to: "+tweets.getSelection());
				Lucene.SHOWTweet = tweets.getSelection();
				Lucene l = Lucene.INSTANCE;
				
				// TODO
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		
		Button users = new Button(group, SWT.CHECK);
		users.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		users.setBackground(red);
		users.setForeground(white);
		users.setText("Users");
		users.setSelection(Lucene.SHOWUser);
		users.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				System.out.println(users.getText() + " is turned to: "+users.getSelection());
				Lucene.SHOWUser = users.getSelection();
				Lucene l = Lucene.INSTANCE;
				
				// TODO
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		
		
		Button heatmap = new Button(parent, SWT.CHECK);
		heatmap.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		heatmap.setText("Heatmap");
		heatmap.setSelection(Lucene.SHOWHeatmap);
		heatmap.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
//				if (heatmap.getSelection() == false) {
//					if (!Lucene.INITCountries) {
//						heatmap.setSelection(true);
//						Lucene l = Lucene.INSTANCE;
//						l.printlnToConsole(">> Countries not yet ready ... ");
//						return;
//					}
//				}
				
				System.out.println(heatmap.getText() + " is turned to: "+heatmap.getSelection());
				Lucene l = Lucene.INSTANCE;
				Lucene.SHOWHeatmap = heatmap.getSelection();
				MapPanelCreator.showHeatmapMenu();
				Result r = l.getLastResult();
				if (r != null)
					l.createMapMarkers(l.getLastResult().getData(), true);
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		
		countries = new Button(parent, SWT.CHECK);
		countries.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		countries.setText("Countries");
		countries.setSelection(Lucene.SHOWCountries);
		countries.setEnabled(Lucene.SHOWCountries);
		countries.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
//				if (countries.getSelection() == false) {
//					if (!Lucene.INITCountries) {
//						countries.setEnabled(false);
//						Lucene l = Lucene.INSTANCE;
//						l.printlnToConsole(">> Countries not yet ready ... ");
//						return;
//					}
//				}
				
				System.out.println(countries.getText() + " is turned to: "+countries.getSelection());
				Lucene l = Lucene.INSTANCE;
				Lucene.SHOWCountries = countries.getSelection();
				Lucene.INITCountries = countries.getSelection();
				MapPanelCreator.dataChanged();
				
//				Result r = l.getLastResult();
//				if (r != null)
//					l.createMapMarkers(l.getLastResult().getData(), true);
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
	}
	
	public static void selectCountries(boolean b) {
		countries.setSelection(b);
	}
	
	
	public static void enableCountries(boolean b) {
		countries.setEnabled(b);
	}
	
	
	public void setFollows(boolean enabled) {
		follows.setEnabled(enabled);
	}
	
	
	
	@Focus
	public void onFocus() {
		
	}
	
	
}