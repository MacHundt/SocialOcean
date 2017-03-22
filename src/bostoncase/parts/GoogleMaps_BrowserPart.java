 
package bostoncase.parts;

import java.awt.image.BufferedImageFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class GoogleMaps_BrowserPart {
	
    private Browser browser;
    
    private Properties prop = null;
    
    private String api_key = "gm_api_key";
    private String api_key_value = "";
    
    
    private String html = "";
    
	
	public GoogleMaps_BrowserPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {

		loadProperties();
		api_key_value = prop.getProperty(api_key);

		parent.setLayout(new GridLayout(1, false));
		
		// Boston
		html = loadHTML(42.367391, -71.065063, 10);

		browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browser.setText(html);
//			browser.setUrl(
//					"https://www.google.com/maps/place/" + URLEncoder.encode("Boston", "UTF-8") + "/&output=embed");

		// not supported at the moment by Google
		// try {
		// browser.setUrl("http://maps.google.com/maps?q="
		// + URLEncoder.encode("Boston", "UTF-8")
		// + "&output=embed");
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}
	
	private String loadHTML(double lat, double lng, int zoom) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html> \n"
				+ "<html>\n"
				+ " <head>\n"
				+ " <meta name='viewport' content='initial-scale=1.0, user-scalable=no'>\n"
				+ "<meta charset='utf-8'>\n"
				+ "<title>Simple markers</title>\n"
				+ "<style>\n"
				+ "html, body {\n"
				+ "height: 100%;\n"
				+ "margin: 0;\n"
				+ "padding: 0;\n"
				+ "}\n"
				+ "#map {\n"
				+ "height: 100%;\n"
				+ "}\n"
				+ "</style>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<div id='map'></div>\n"
				+ "<script>\n"
				+ "function initMap() {\n"
				+ "var myLatLng = {lat: "+lat+", lng: "+lng+"}; \n"
				+ "var map = new google.maps.Map(document.getElementById('map'), {\n"
				+ "zoom: "+zoom+",\n"
				+ "center: myLatLng\n"
				+ "});"
				+ "var marker = new google.maps.Marker({\n"
				+ "position: myLatLng,\n"
				+ "map: map,\n"
				+ "title: 'Hello World!'\n"
				+ "});\n"
				+ "map.addListener('center_changed', function() {"
				+ "window.setTimeout(function() {"
				+ "map.panTo(marker.getPosition());"
				+ "}, 3000);"
				+ "});"
				+ "}\n");
				
		sb.append("</script>\n"
				+ "<script async defer \n"
				+ "src='https://maps.googleapis.com/maps/api/js?key="+api_key_value+"&signed_in=true&callback=initMap'>\n"
						+ "</script>\n"
				+ "</body>\n"
				+ "</html>\n");

		return sb.toString();
	}
	
	private void loadProperties() {
		prop = new Properties();
		File f = new File("/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/KEYs/keys.properties");
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				// Deal with the line
				String[] proper = line.split("[=:]");
				prop.put(proper[0], proper[1]);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@Focus
	public void onFocus() {
		browser.setFocus();
	}
	
	
}