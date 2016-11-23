 
package bostoncase.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import bostoncase.widgets.gmaps.LatLng;

import org.eclipse.e4.ui.di.Focus;

public class GMapPart {
	
	private bostoncase.widgets.gmaps.GMap map;
	
	@Inject
	public GMapPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		map = new bostoncase.widgets.gmaps.GMap(parent, SWT.None);
		map.setCenter(new LatLng(42.4355, -71.18596));
		map.setZoom( 11 );
		map.setType( map.TYPE_ROADMAP);
	}
	
	
	
	@Focus
	public void onFocus() {
		
	}
	
	
}