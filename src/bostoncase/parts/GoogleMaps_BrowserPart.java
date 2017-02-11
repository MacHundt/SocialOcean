 
package bostoncase.parts;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class GoogleMaps_BrowserPart {
	
    private Browser browser;
	
	public GoogleMaps_BrowserPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		 parent.setLayout(new GridLayout(1, false));
		 
         browser = new Browser(parent, SWT.NONE);
         browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
         try {
			browser.setUrl("https://www.google.com/maps/place/"
			         + URLEncoder.encode("Boston", "UTF-8")
			         + "/&output=embed");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//         browser.setUrl("https://www.google.com/maps/");
		 
		 // not supported at the moment by Google
//        try {
//			browser.setUrl("http://maps.google.com/maps?q="
//			+ URLEncoder.encode("Boston", "UTF-8")
//			+ "&output=embed");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		
	}
	
	
	@Focus
	public void onFocus() {
		browser.setFocus();
	}
	
	
}