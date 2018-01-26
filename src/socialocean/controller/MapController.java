package socialocean.controller;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Semaphore;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.eclipse.swt.widgets.Display;
import org.jxmapviewer.JXMapViewer;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import impl.MapPanelCreator;
import impl.MyEdge;
import impl.MyUser;
import socialocean.model.MapCountries;
import socialocean.model.MapGridRectangle;
import socialocean.model.Result;
import socialocean.parts.SettingsPart;
import utils.DBManager;
import utils.GeoToCartesianTransformation;
import utils.Lucene;

public class MapController extends Observable {

	// Zoomlevel to gridCell
//	Map<Integer, Map<MapGridRectangle, List<Document>>> cellsToYard = new HashMap<>();
	Map<Integer, Map<MapGridRectangle, List<String>>> cellsToYard = new HashMap<>();
	Map<Integer, Map<MapCountries, List<String>>> countriesToYard = new HashMap<>();
	// Map<Integer, Map<MapCountries, List<Document>>> countriesToYard = new
	// HashMap<>();
	
	private ArrayList<?> selection = null;

	private int cellSize = 128;
	// ArrayList<Geometry> admin0 = new ArrayList<>();
	// boolean hasadmin0 = false;

	private JXMapViewer map;

	public MapController(JXMapViewer map) {
		this.map = map;
	}
	

	public Map<MapCountries, List<String>> getCountries(int zoomLvl) {
		if (!countriesToYard.containsKey(zoomLvl)) {
			if (zoomLvl > 12)
				if (selection != null) {
					initCountries(selection);
				}
				else
					initCountries();
		}
		return countriesToYard.get(zoomLvl);
	}
	
	
	

	public synchronized void  setSelection(ArrayList<?> allItems) {
		selection = allItems;
	}
	
	
	private void initCountries(ArrayList<?> allItems) {
		Lucene l = Lucene.INSTANCE;
		Lucene.INITCountries = false;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// SettingsPart.selectCountries(false);
				SettingsPart.enableCountries(false);
			}
		});

		Map<MapCountries, List<String>> countries = new HashMap<>();
		Map<MapCountries, List<String>> states = new HashMap<>();
		WKTReader wkt = new WKTReader();
		ShapeWriter sw = new ShapeWriter(new GeoToCartesianTransformation(map));
		GeometryFactory factory = new GeometryFactory();
		Connection c = DBManager.getConnection();
		
		if (c == null)
			return;

		boolean user = false;

		if (allItems.isEmpty()) {
			return;
		}

		if (allItems.get(0) instanceof MyUser)
			user = true;

		for (Object o : allItems) {
			String id = "";
			double lat = 0;
			double lon = 0;
			if (o instanceof MyEdge) { // change Color
				MyEdge e = (MyEdge) o;
				id = e.getId();
				if (!e.hasGeo())
					continue;
				if (e.getLatitude() == 0.0 || e.getLongitude() == 0.0)
					continue;

				lon = e.getLongitude();
				lat = e.getLatitude();

			} else if (o instanceof MyUser) {
				MyUser u = (MyUser) o;
				id = u.getId();
				if (!u.hasGeo())
					continue;
				if (u.getLatitude() == 0.0 || u.getLongitude() == 0.0)
					continue;

				lon = u.getLongitude();
				lat = u.getLatitude();
			}

			if (lat == 0.0 || lon == 0.0)
				continue;

			HashMap<String, Geometry> geometryMap = null;
			Geometry geometry = null;
			String admin = "";
			geometryMap = getCountryGeometry(lat, lon, c);
			for (String s : geometryMap.keySet())
				admin = s;
			for (Geometry g : geometryMap.values())
				geometry = g;

			// point not in country
			if (geometry == null) {
				continue;
			}

			// States
			String[] admin1 = { "Australia", "Brazil", "Canada", "United States of America", "United States" };
			boolean isInAdmin1 = false;
			for (String country : admin1) {
				if (country.equals(admin)) {
					isInAdmin1 = true;
					break;
				}
			}

			String stateName = admin;
			Geometry stateGeom = geometry;
			// Geometry stateGeom = null;
			if (isInAdmin1) {
				geometryMap = getCountryProvinceGeometry(lat, lon, c);
				for (String s : geometryMap.keySet())
					stateName = s;
				for (Geometry g : geometryMap.values())
					stateGeom = g;
			}

			// Country
			Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				polygons[i] = (Polygon) geometry.getGeometryN(i);
			}

			MapCountries country = new MapCountries(polygons, factory);
			if (!countries.containsKey(country)) {
				countries.put(country, new ArrayList<>());
			}
			countries.get(country).add(id);

			// States
			if (stateGeom == null) {
				continue;
			}
			Polygon[] polygonsState = new Polygon[stateGeom.getNumGeometries()];
			for (int i = 0; i < stateGeom.getNumGeometries(); i++) {
				polygonsState[i] = (Polygon) stateGeom.getGeometryN(i);
			}

			MapCountries state = new MapCountries(polygonsState, factory);
			if (!states.containsKey(state)) {
				states.put(state, new ArrayList<>());
			}
			states.get(state).add(id);
		}

		// Set Color for countries
		double maxDocs = Math.log((double) allItems.size());
		double stepsize = maxDocs / 5;
		for (MapCountries key : countries.keySet()) {
			double cellN = Math.log(countries.get(key).size());
			int bucket = (int) Math.ceil(cellN / stepsize);
			int a = 15;
			switch (bucket) {
			case 1:
				a = 51;
				break;
			case 2:
				a = 102;
				break;
			case 3:
				a = 153;
				break;
			case 4:
				a = 204;
				break;
			case 5:
				a = 255;
				break;
			default:
				break;
			}
			if (user) {
				
			}
			else {
				// RED
				if (user)
					key.setBackgroundColor(new Color(255, 0, 0, a));
				else 
					key.setBackgroundColor(new Color(0, 0, 255, a));
			}
		}

		// Set Color for states
		for (MapCountries key : states.keySet()) {
			double cellN = Math.log(states.get(key).size());
			int bucket = (int) Math.ceil(cellN / stepsize);
			int a = 15;
			switch (bucket) {
			case 1:
				a = 51;
				break;
			case 2:
				a = 102;
				break;
			case 3:
				a = 153;
				break;
			case 4:
				a = 204;
				break;
			case 5:
				a = 255;
				break;
			default:
				break;
			}
			
			// RED
			if (user)
				key.setBackgroundColor(new Color(255, 0, 0, a));
			else
				key.setBackgroundColor(new Color(0, 0, 255, a));
		}

		System.out.println("Number of Countries: " + countries.size());

		// from 10 to max --> countries
		for (int i = 16; i < MapPanelCreator.maxZoom; ++i) {
			countriesToYard.put(i, countries);
		}

		for (int i = 12; i < 16; ++i) {
			countriesToYard.put(i, states);
		}

		Lucene.DATACHANGED = false;
		Lucene.INITCountries = true;

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// SettingsPart.selectCountries(true);
				SettingsPart.enableCountries(true);
			}
		});

		l.printlnToConsole(">> Countries ready");
		System.out.println("Countries ready");
	}
	
	

	private void initCountries() {

		Lucene l = Lucene.INSTANCE;
		Result result = l.getLastResult();
		Lucene.INITCountries = false;
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
//		    	SettingsPart.selectCountries(false);
		    	SettingsPart.enableCountries(false);
		    }
		});
		
		// ShapeWriter sw = new ShapeWriter(new GeoToCartesianTransformation(map));

		Map<MapCountries, List<String>> countries = new HashMap<>();
		Map<MapCountries, List<String>> states = new HashMap<>();

		if (result == null)
			return;

		IndexSearcher searcher = l.getIndexSearcher();
		GeometryFactory factory = new GeometryFactory();
		Connection c = DBManager.getConnection();
		
		if (c == null)
			return;
		
		try {

			// HashMap<Point2D, List<Document>> pCluster = new HashMap<>();

			// for every tweet .. check countries
			for (ScoreDoc entry : result.getData()) {

				int docID = entry.doc;
				Document document = searcher.doc(docID);

				// System.out.println(document.getField("id").stringValue());
				
				// no geo
				IndexableField f = document.getField("geo");
				if (f == null)
					continue;
				
				long hashgeo = (document.getField("geo")).numericValue().longValue();
				double lat = GeoPointField.decodeLatitude(hashgeo);
				double lon = GeoPointField.decodeLongitude(hashgeo);

				if (lat == 0.0 || lon == 0.0)
					continue;

				// String sLat = (lat+"").substring(0, (lat+"").indexOf(".")+4);
				// String sLon = (lon+"").substring(0, (lon+"").indexOf(".")+4);
				//
				// Point2D p = new Point2D.Double(Double.parseDouble(sLon),
				// Double.parseDouble(sLat));
				//
				// if (pCluster.get(p) == null) {
				// pCluster.put(p, new ArrayList<Document>());
				// }
				// pCluster.get(p).add(document);

				HashMap<String, Geometry> geometryMap = null;
				Geometry geometry = null;
				String admin = "";
				geometryMap = getCountryGeometry(lat, lon, c);
				for (String s : geometryMap.keySet())
					admin = s;
				for (Geometry g : geometryMap.values())
					geometry = g;
				
				// point not in country
				if (geometry == null) {
					continue;
				}

				// States
				String[] admin1 = { "Australia", "Brazil", "Canada", "United States of America", "United States" };
				boolean isInAdmin1 = false;
				for (String country : admin1) {
					if (country.equals(admin)) {
						isInAdmin1 = true;
						break;
					}
				}
				
				String stateName = admin;
				Geometry stateGeom = geometry;
//				Geometry stateGeom = null;
				if (isInAdmin1) {
					geometryMap = getCountryProvinceGeometry(lat, lon, c);
					for (String s : geometryMap.keySet())
						stateName = s;
					for (Geometry g : geometryMap.values())
						stateGeom = g;
				}
				

				// Country
				Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					polygons[i] = (Polygon) geometry.getGeometryN(i);
				}
				
				MapCountries country = new MapCountries(polygons, factory);
				if (!countries.containsKey(country)) {
					countries.put(country, new ArrayList<>());
				}
				countries.get(country).add(document.getField("id").stringValue());
				
				// States
				if (stateGeom == null) {
					continue;
				}
				Polygon[] polygonsState = new Polygon[stateGeom.getNumGeometries()];
				for (int i = 0; i < stateGeom.getNumGeometries(); i++) {
					polygonsState[i] = (Polygon) stateGeom.getGeometryN(i);
				}
				
				MapCountries state = new MapCountries(polygonsState, factory);
				if (!states.containsKey(state)) {
					states.put(state, new ArrayList<>());
				}
				states.get(state).add(document.getField("id").stringValue());
				
			}

			// for (Entry<Point2D, List<Document>> e : pCluster.entrySet()) {
			//
			// Geometry geometry = getCountryGeometry(e.getKey().getY(), e.getKey().getX(),
			// c);
			// // Shape s = sw.toShape(geometry);
			//
			// // point not in country
			// if (geometry == null) {
			// continue;
			// }
			//
			// Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
			// for (int i=0 ; i< geometry.getNumGeometries(); i++) {
			// polygons[i] = (Polygon) geometry.getGeometryN(i);
			// }
			//
			// MapCountries country = new MapCountries(polygons, factory);
			//
			// if (!countries.containsKey(country)) {
			// countries.put(country, new ArrayList<>());
			// }
			// countries.get(country).addAll(e.getValue());
			// }

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set Color for countries
		double maxDocs = Math.log((double) result.getData().length);
		double stepsize = maxDocs / 5;
		for (MapCountries key : countries.keySet()) {
			double cellN = Math.log(countries.get(key).size());
			int bucket = (int) Math.ceil(cellN / stepsize);
			int a = 15;
			switch (bucket) {
			case 1:
				a = 51;
				break;
			case 2:
				a = 102;
				break;
			case 3:
				a = 153;
				break;
			case 4:
				a = 204;
				break;
			case 5:
				a = 255;
				break;
			default:
				break;
			}
			key.setBackgroundColor(new Color(0, 0, 255, a));
		}
		
		// Set Color for states
		for (MapCountries key : states.keySet()) {
			double cellN = Math.log(states.get(key).size());
			int bucket = (int) Math.ceil(cellN / stepsize);
			int a = 15;
			switch (bucket) {
			case 1:
				a = 51;
				break;
			case 2:
				a = 102;
				break;
			case 3:
				a = 153;
				break;
			case 4:
				a = 204;
				break;
			case 5:
				a = 255;
				break;
			default:
				break;
			}
			key.setBackgroundColor(new Color(0, 0, 255, a));
		}

		System.out.println("Number of Countries: " + countries.size());

		// from 10 to max --> countries
		for (int i = 16; i < MapPanelCreator.maxZoom; ++i) {
			countriesToYard.put(i, countries);
		}
		
		for (int i = 12; i < 16; ++i) {
			countriesToYard.put(i, states);
		}
		
		Lucene.DATACHANGED = false;
		Lucene.INITCountries = true;
		
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
//		     	SettingsPart.selectCountries(true);
		     	SettingsPart.enableCountries(true);
		    }
		});
		
		l.printlnToConsole(">> Countries ready");
		System.out.println("Countries ready");

	}

	// private com.vividsolutions.jts.geom.Geometry
	// getCountryGeometryFromID(Connection c, int gid) {
	// com.vividsolutions.jts.geom.Geometry geo = null;
	// WKTReader wkt = new WKTReader();
	// try {
	// Statement stmt = c.createStatement();
	//// String query = "Select name, continent, economy, income_grp,
	// ST_astext(geom) " +
	//// "from countries_admin0 " +
	//// "where gid ="+gid;
	// String query = "Select name, ST_astext(geom) " +
	// "from countries_admin0 " +
	// "where gid ="+gid;
	//
	// ResultSet rs = stmt.executeQuery(query);
	// while (rs.next()) {
	// geo = wkt.read(rs.getString(2));
	//
	// }
	//
	//
	// } catch (SQLException | ParseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return geo;
	// }

	private HashMap<String, Geometry> getCountryProvinceGeometry(double lat, double lon, Connection c) {
		HashMap<String, Geometry> countryGeoms = new HashMap<>(1);
		Geometry geo = null;
		WKTReader wkt = new WKTReader();
		String name = "";
		try {
			Statement stmt = c.createStatement();
//			String query = "Select name, ST_astext(geom) " + "from countries_admin1 "
//					+ "where ST_Contains(geom, ST_SetSRID(St_Point(" + lon + ", " + lat + "), 4326)) Limit 1";

			String query = "Select t.name_1, St_astext(admin1.geom) from countries_admin1 as admin1, ( "
					+ "Select name_1 from countries_all "
					+ "where St_Contains(geom, St_setSrid(St_Point("+lon+","+lat+"), 4326)) "
							+ ") as t "
					+ "where t.name_1 = admin1.name"
			+ "";
			
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				name = rs.getString(1);
				geo = wkt.read(rs.getString(2));
			}

		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}
		countryGeoms.put(name, geo);
		
		return countryGeoms;
	}

	
	public static HashMap<String, Geometry> getCountryGeometry(double lat, double lon, Connection c) {
		Geometry geo = null;
		HashMap<String, Geometry> countryGeoms = new HashMap<>(1);
		WKTReader wkt = new WKTReader();
		String name = "";
		try {
			Statement stmt = c.createStatement();
//			String query = "Select admin, ST_astext(geom) " + "from countries_admin0 "
//					+ "where ST_Contains(geom, ST_SetSRID(St_Point(" + lon + ", " + lat + "), 4326)) Limit 1";

			String query = "select admin0.name, St_astext(admin0.geom) from countries_admin0 as admin0, ( "
					+ "Select name_0 from countries_all "
					+ "where St_Contains(geom, St_setSrid(St_Point("+lon+","+lat+"),4326)) "
							+ ") as t "
					+ "where t.name_0 = admin0.name"
			+ "";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				name = rs.getString(1);
				geo = wkt.read(rs.getString(2));
			}

		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}
		countryGeoms.put(name, geo);
		
		return countryGeoms;

	}
	
	public Map<MapGridRectangle, List<String>> getGridCells(int zoomLvl, ArrayList<?> allItems) {
		if (!cellsToYard.containsKey(zoomLvl)) {
			initGridCells(zoomLvl, allItems);
		}

		return cellsToYard.get(zoomLvl);
		
	}
	
	
	private void initGridCells(int zoomLvl, ArrayList<?> allItems) {
		
		WKTReader wkt = new WKTReader();
		boolean user = false;
		// Assign relevant tweet or user to their gridcells wrt all zoomlvls
		ShapeWriter sw = new ShapeWriter(new GeoToCartesianTransformation(map));

		Map<MapGridRectangle, List<String>> cells = new HashMap<>();
		ArrayList<MyUser> userCells = new ArrayList<>();
		ArrayList<MyEdge> tweetCells = new ArrayList<>();
		
		boolean showBoth = false;
		if (Lucene.SHOWTweet && Lucene.SHOWUser)
			showBoth = true;
		
		
		if (allItems.isEmpty()) {
			return;
		}
		
		if (allItems.get(0) instanceof MyUser)
			user = true;
		
		for (Object o : allItems) {
			String id = "";
			String point = "";
			if (o instanceof MyEdge) {		// change Color
				MyEdge e = (MyEdge) o;
				if (showBoth) {
					tweetCells.add(e);
					continue;
				}
				id = e.getId();
				if (!e.hasGeo())
					continue;
				if (e.getLatitude() == 0.0 || e.getLongitude() == 0.0)
					continue;
				
				point = "POINT (" + e.getLongitude() + " " + e.getLatitude() + ")";
				
			} else if (o instanceof MyUser) {
				MyUser u = (MyUser) o;
				if (showBoth) {
					userCells.add(u);
					continue;
				}
				id = u.getId();
				if (!u.hasGeo())
					continue;
				if (u.getLatitude() == 0.0 || u.getLongitude() == 0.0)
					continue;
				
				point = "POINT (" + u.getLongitude() + " " + u.getLatitude() + ")";
			}
			
			if (point.isEmpty())
				continue;
			
			try {
				com.vividsolutions.jts.geom.Geometry geometry = wkt.read(point);
				// Geometry geometry = JtsGeometry.geomFromString("POINT ("+lon+" "+lat+")");
				Shape s = sw.toShape(geometry);
				
				// TODO: for now we assign each polygon to the gridcell which
				// contains the center of the bounding rect
				Rectangle2D b = s.getBounds2D();
				
				double centerX = b.getCenterX();
				double centerY = b.getCenterY();
				
				// Find the correct cell(s)
				int cellX = (int) (centerX / cellSize);
				int cellY = (int) (centerY / cellSize);
				
				MapGridRectangle cell = new MapGridRectangle(cellX * cellSize, cellY * cellSize, cellSize, cellSize);
				if (!cells.containsKey(cell)) {
					cells.put(cell, new ArrayList<>());
				}
				else
					cells.get(cell).add(id);
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		}
		
		
		if (showBoth) {
			// do all for edges and users again:
			for (MyUser u : userCells) {
				String id = u.getId();
				String point = "";
				if (!u.hasGeo())
					continue;
				if (u.getLatitude() == 0.0 || u.getLongitude() == 0.0)
					continue;
					
				point = "POINT (" + u.getLongitude() + " " + u.getLatitude() + ")";
				
				if (point.isEmpty())
					continue;
				
				try {
					com.vividsolutions.jts.geom.Geometry geometry = wkt.read(point);
					// Geometry geometry = JtsGeometry.geomFromString("POINT ("+lon+" "+lat+")");
					Shape s = sw.toShape(geometry);
					Rectangle2D b = s.getBounds2D();
					
					double centerX = b.getCenterX();
					double centerY = b.getCenterY();
					
					// Find the correct cell(s)
					int cellX = (int) (centerX / cellSize);
					int cellY = (int) (centerY / cellSize);
					
					MapGridRectangle cell = new MapGridRectangle(cellX * cellSize, cellY * cellSize, cellSize, cellSize);
					if (!cells.containsKey(cell)) {
						cells.put(cell, new ArrayList<>());
					}
					else
						cells.get(cell).add(id);
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			// Set Color of cell
			double maxDocs = Math.log((double) userCells.size());
			
			double stepsize = maxDocs / 5;
			for (MapGridRectangle key : cells.keySet()) {
				double cellN = Math.log(cells.get(key).size());
				int bucket = (int) Math.ceil(cellN / stepsize);
				int a = 15;
				switch (bucket) {
				case 1:
					a = 51;
					break;
				case 2:
					a = 102;
					break;
				case 3:
					a = 153;
					break;
				case 4:
					a = 204;
					break;
				case 5:
					a = 255;
					break;

				default:
					break;
				}
				
				// RED
				key.setBackgroundColor(new Color(255, 0, 0, a));
			}

//			cellsToYard.put(zoomLvl, cells);
			
			Map<MapGridRectangle, List<String>> edgeCells = new HashMap<>();
			for (MyEdge u : tweetCells) {
				String id = u.getId();
				String point = "";
				if (!u.hasGeo())
					continue;
				if (u.getLatitude() == 0.0 || u.getLongitude() == 0.0)
					continue;
					
				point = "POINT (" + u.getLongitude() + " " + u.getLatitude() + ")";
				
				if (point.isEmpty())
					continue;
				
				try {
					com.vividsolutions.jts.geom.Geometry geometry = wkt.read(point);
					// Geometry geometry = JtsGeometry.geomFromString("POINT ("+lon+" "+lat+")");
					Shape s = sw.toShape(geometry);
					Rectangle2D b = s.getBounds2D();
					
					double centerX = b.getCenterX();
					double centerY = b.getCenterY();
					
					// Find the correct cell(s)
					int cellX = (int) (centerX / cellSize);
					int cellY = (int) (centerY / cellSize);
					
					MapGridRectangle cell = new MapGridRectangle(cellX * cellSize, cellY * cellSize, cellSize, cellSize);
					if (!edgeCells.containsKey(cell)) {
						edgeCells.put(cell, new ArrayList<>());
					}
					else
						edgeCells.get(cell).add(id);
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			// Set Color of cell
			maxDocs = Math.log((double) tweetCells.size());
			
			stepsize = maxDocs / 5;
			for (MapGridRectangle key : edgeCells.keySet()) {
				double cellN = Math.log(edgeCells.get(key).size());
				int bucket = (int) Math.ceil(cellN / stepsize);
				int a = 15;
				switch (bucket) {
				case 1:
					a = 51;
					break;
				case 2:
					a = 102;
					break;
				case 3:
					a = 153;
					break;
				case 4:
					a = 204;
					break;
				case 5:
					a = 255;
					break;

				default:
					break;
				}
				
				// RED
				key.setBackgroundColor(new Color(0, 0, 255, a));
			}
			
			Map<MapGridRectangle, List<String>> mergedCells = new HashMap<>();
			System.out.println("EdgeCells: "+edgeCells.size());
			System.out.println("UserCells: " +cells.size());
			
			for ( MapGridRectangle userkey : cells.keySet()) {
				mergedCells.put(userkey, cells.get(userkey));
				for ( MapGridRectangle twkey : edgeCells.keySet() ) {
					if (userkey.equals(twkey)) {
						// merge rectangle color
						Color uColor = userkey.getBackgroundColor();
						int alpha = uColor.getAlpha();
						Color twColor = twkey.getBackgroundColor();
						int twalpha = twColor.getAlpha();
//						int cRed = (tRed * tAlpha + bRed * (255 - tAlpha)) / 255;
						int cRed = (uColor.getRed() * uColor.getAlpha() + twColor.getRed() * (255 - uColor.getAlpha())) / 255;
						int cGreen = (uColor.getGreen() * uColor.getAlpha() + twColor.getGreen() * (255 - uColor.getAlpha())) / 255;
						int cBlue = (uColor.getBlue() * uColor.getAlpha() + twColor.getBlue() * (255 - uColor.getAlpha())) / 255;
						
						twkey.setBackgroundColor(new Color(cRed, cGreen, cBlue, 200));
//						edgeCells.get(twkey).addAll(cells.get(userkey));
						mergedCells.put(twkey, edgeCells.get(twkey));
						
					}
					else {
						mergedCells.put(twkey, edgeCells.get(twkey));
					}
				}
			}
			
			cellsToYard.put(zoomLvl, mergedCells);
			
		}
		else {

			// Set Color of cell
			double maxDocs = Math.log((double) allItems.size());

			double stepsize = maxDocs / 5;
			for (MapGridRectangle key : cells.keySet()) {
				double cellN = Math.log(cells.get(key).size());
				int bucket = (int) Math.ceil(cellN / stepsize);
				int a = 15;
				switch (bucket) {
				case 1:
					a = 51;
					break;
				case 2:
					a = 102;
					break;
				case 3:
					a = 153;
					break;
				case 4:
					a = 204;
					break;
				case 5:
					a = 255;
					break;

				default:
					break;
				}

				// RED
				if (user) {
					key.setBackgroundColor(new Color(255, 0, 0, a));
				} else {
					key.setBackgroundColor(new Color(0, 0, 255, a));
				}
			}

			// System.out.println("Number of Cells: " + cells.size());
			// System.out.println("Cellsize: " + cellSize);
			cellsToYard.put(zoomLvl, cells);
		}
		
	}
	
	

	public Map<MapGridRectangle, List<String>> getGridCells(int zoomLvl) {
		
		if (Lucene.SHOWTweet && Lucene.SHOWUser) {
			if (selection != null) {
				initGridCells(zoomLvl, selection);
			}
			else 
				initGridCells(zoomLvl);
		}
		
		
		else if (!cellsToYard.containsKey(zoomLvl)) {
			if (selection != null) {
				initGridCells(zoomLvl, selection);
			}
			else 
				initGridCells(zoomLvl);
		}

		return cellsToYard.get(zoomLvl);
	}
	

	private void initGridCells(int zoomLvl) {
		// Check the cells that contain data (the others can be skipped)

		Lucene l = Lucene.INSTANCE;
		Result result = l.getLastResult();

		// Assign relevant tweet or user to their gridcells wrt all zoomlvls
		ShapeWriter sw = new ShapeWriter(new GeoToCartesianTransformation(map));

//		Map<MapGridRectangle, List<Document>> cells = new HashMap<>();
		Map<MapGridRectangle, List<String>> cells = new HashMap<>();

		if (result == null)
			return;

		IndexSearcher searcher = l.getIndexSearcher();
		WKTReader wkt = new WKTReader();

		for (ScoreDoc entry : result.getData()) {

			int docID = entry.doc;
			try {
				Document document = searcher.doc(docID);
				
				// no geo
				IndexableField f = document.getField("geo");
				if (f == null)
					continue;

				long hashgeo = (document.getField("geo")).numericValue().longValue();
				double lat = GeoPointField.decodeLatitude(hashgeo);
				double lon = GeoPointField.decodeLongitude(hashgeo);

				if (lat == 0.0 || lon == 0.0)
					continue;

				com.vividsolutions.jts.geom.Geometry geometry = wkt.read("POINT (" + lon + " " + lat + ")");
				// Geometry geometry = JtsGeometry.geomFromString("POINT ("+lon+" "+lat+")");
				Shape s = sw.toShape(geometry);

				// TODO: for now we assign each polygon to the gridcell which
				// contains the center of the bounding rect
				Rectangle2D b = s.getBounds2D();

				double centerX = b.getCenterX();
				double centerY = b.getCenterY();

				// Find the correct cell(s)
				int cellX = (int) (centerX / cellSize);
				int cellY = (int) (centerY / cellSize);

				MapGridRectangle cell = new MapGridRectangle(cellX * cellSize, cellY * cellSize, cellSize, cellSize);
				if (!cells.containsKey(cell)) {
					cells.put(cell, new ArrayList<>());
				}
				cells.get(cell).add(document.getField("id").stringValue());

			} catch (IOException | ParseException e) {

			}

		}

		// Set Color of cell
		double maxDocs = Math.log((double) result.getData().length);
		double stepsize = maxDocs / 5;
		for (MapGridRectangle key : cells.keySet()) {
			double cellN = Math.log(cells.get(key).size());
			int bucket = (int) Math.ceil(cellN / stepsize);
			int a = 15;
			switch (bucket) {
			case 1:
				a = 51;
				break;
			case 2:
				a = 102;
				break;
			case 3:
				a = 153;
				break;
			case 4:
				a = 204;
				break;
			case 5:
				a = 255;
				break;

			default:
				break;
			}
			key.setBackgroundColor(new Color(0, 0, 255, a));
		}

//		System.out.println("Number of Cells: " + cells.size());
//		System.out.println("Cellsize: " + cellSize);
		cellsToYard.put(zoomLvl, cells);
	}

	public int getCellSize() {
		return this.cellSize;
	}

	public void setCellSize(int cellSize) {
		this.cellSize = cellSize;

		resetGridCells();
		// Redraw Map
		setChanged();
		notifyObservers();
	}

	public void zoomChanged() {

		resetGridCells();
		// Redraw Map
		setChanged();
		notifyObservers();

	}
	
	
	public void addDataChanged() {
		
		setChanged();
		notifyObservers();
	}
	
	

	public void dataChanged() {
		
		resetGridCells();
		if (Lucene.DATACHANGED)
			resetCountry();
		// Redraw Map
		setChanged();
		notifyObservers();
	}

	public void resetCountry() {
		this.countriesToYard.clear();
		Lucene.DATACHANGED = false;
	}

	public void resetGridCells() {
		this.cellsToYard.clear();
	}

	public void clearCountries() {
		this.countriesToYard.clear();
	}




}
