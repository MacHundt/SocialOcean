 
package socialocean.parts;

import javax.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;

public class TimeLineStoryPart {
	
	private Browser browser;
	
	@Inject
	public TimeLineStoryPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		 
         browser = new Browser(parent, SWT.NONE);
         browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
         browser.setUrl("http://timelinecurator.org");
	}
	
	
	
	@Focus
	public void onFocus() {
		browser.setFocus();
	}
	
	
	@Persist
	public void save() {
		// TODO
	}
	
}