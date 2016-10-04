 
package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import bostoncase.widgets.MapWidget;

public class Map {
	
	private MapWidget mapvis;
	
	@Inject
	public Map() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		mapvis = new MapWidget(parent, SWT.NONE);
		mapvis.setLayoutData(new GridData(GridData.FILL_BOTH));
		
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
		mapvis.setFocus();
	}
	
	
}