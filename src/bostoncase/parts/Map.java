 
package bostoncase.parts;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.geobuf.Geobuf.Data.Feature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.geometry.Geometry;

import com.vividsolutions.jts.io.WKTReader;

import bostoncase.widgets.MapWidget;
import bostoncase.widgets.MapWidget.PointD;

public class Map {
	
	private MapWidget mapvis;
	
	@Inject
	public Map() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		// Boston Coordinates
		mapvis = new MapWidget(parent, SWT.NONE | SWT.CENTER,
				new Point(MapWidget.lon2position(-71.18596, 11), MapWidget.lat2position(42.4355, 11)), 
				11);
		mapvis.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// load Boston Border
		
//		FileDialog fsd = new FileDialog(parent.getShell(), SWT.MULTI);
//		fsd.setFilterExtensions(new String[] { "*.csv", "*.shp","*.json" });
//		fsd.setText("Select Files...");
//		fsd.open();
//		String path = fsd.getFilterPath();
//		String[] file_selection = fsd.getFileNames();
		
		
//		System.out.println("loaded: "+ f.getPath());
		java.util.Map<String, URL> map = new HashMap<String, URL>();      
		try {
//		File f = new File("./shapes/Boundary/Boundary.shp");
			URL url = null;
			try {
				url = new URL("platform:/plugin/"
						+ "BostonCase/"
						+ "shapes/Boundary/Boundary.shp");
				
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			URL file_url = FileLocator.toFileURL(url);
			File f = new File(file_url.getPath());
			
			map.put("url", file_url.toURI().toURL());
			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];
			
//			System.out.println("Reading content " + typeName);
			
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);        
			SimpleFeatureCollection collection = featureSource.getFeatures();
			
//			Filter filter = CQL.toFilter("CONTAINS(geom, POINT(42.22788726529499 -70.92346936852225))");
//			SimpleFeatureCollection features = featureSource.getFeatures(filter);
//			
//			 SimpleFeatureIterator iterator = features.features();
//			    try {
//			        while (iterator.hasNext()) {
//			            SimpleFeature feature = iterator.next();
//			            Geometry geom = (Geometry) feature.getDefaultGeometry();
//			           /*... do something here */
//			        }
//			    } finally {
//			        iterator.close(); // IMPORTANT
//			    }
			 
			FeatureIterator iterator = collection.features();

			try {
				while (iterator.hasNext()) {
					org.opengis.feature.Feature feature = iterator.next();
					Collection<Property> prop = feature.getProperties();
					GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
//					System.out.println(sourceGeometry.getValue().toString());
				}
			} finally {
				iterator.close();
			}
			
			// BBOX of shapefile
		    ReferencedEnvelope env = collection.getBounds();
		    double left = env.getMinX();
		    double right = env.getMaxX();
		    double top = env.getMaxY();
		    double bottom = env.getMinY();
		    
		    System.out.println("loaded shape file: "+ f.toPath());
			System.out.println(env.toString());
			
			mapvis.addPoint(new PointD(left, top));
			
			// Paint a point on may
//			mapvis.addPaintListener(new PaintListener() {
//				
//				@Override
//				public void paintControl(PaintEvent pe) {
//					Rectangle area = mapvis.getClientArea();
//					PointD p = mapvis.getLongitudeLatitude(new Point(0,0));
//					
//					int zoom = mapvis.getZoom();
//					int x = mapvis.lon2position(left, zoom);  // max
//					int y = mapvis.lat2position(top, zoom);
//					pe.gc.drawPoint(x, y);
////					pe.gc.drawPolygon(new int[] {  }); 
//					
//				}
//			});
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
		mapvis.setFocus();
	}
	
	
}