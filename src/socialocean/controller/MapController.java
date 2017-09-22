package socialocean.controller;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.jxmapviewer.JXMapViewer;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import socialocean.model.MapGridRectangle;
import socialocean.model.Result;
import utils.GeoToCartesianTransformation;
import utils.Lucene;

public class MapController extends Observable {

	// Zoomlevel to gridCell
	Map<Integer, Map<MapGridRectangle, List<Document>>> cellsToYard = new HashMap<>();
	private int cellSize = 128;

	private JXMapViewer map;

	public MapController(JXMapViewer map) {
		this.map = map;
	}

	public Map<MapGridRectangle, List<Document>> getGridCells(int zoomLvl) {
		if (!cellsToYard.containsKey(zoomLvl)) {
			initGridCells(zoomLvl);
		}

		return cellsToYard.get(zoomLvl);
	}

	private void initGridCells(int zoomLvl) {
		// Check the cells that contain data (the others can be skipped)
		
		Lucene l = Lucene.INSTANCE;
		Result result = l.getLastResult();

		// Assign relevant wineyards and to their gridcells wrt all zoomlvls
		ShapeWriter sw = new ShapeWriter(new GeoToCartesianTransformation(map));

		Map<MapGridRectangle, List<Document>> cells = new HashMap<>();
		
		if (result == null)
			return;
		
		IndexSearcher searcher = l.getIndexSearcher();

		for (ScoreDoc entry : result.getData()) {
			
			int docID = entry.doc;
			try {
				Document document = searcher.doc(docID);
				
				// System.out.println(document.getField("id").stringValue());
				long hashgeo = (document.getField("geo")).numericValue().longValue();
				double lat = GeoPointField.decodeLatitude(hashgeo);
				double lon = GeoPointField.decodeLongitude(hashgeo);
				
				if (lat == 0.0 || lon == 0.0) 
					continue;
				
				WKTReader wkt = new WKTReader();
				Geometry geometry = wkt.read("POINT ("+lon+" "+lat+")");
//				Geometry geometry = JtsGeometry.geomFromString("POINT ("+lon+" "+lat+")");
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
				cells.get(cell).add(document);
			
			} catch (IOException | ParseException e) {
				
			}
			
		}
		System.out.println("Number of Cells: " + cells.size());
		System.out.println("Cellsize: " + cellSize);
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
	
	
	public void dataChanged() {
		
		resetGridCells();
		// Redraw Map
		setChanged();
		notifyObservers();
	}
	

	public void resetGridCells() {
		this.cellsToYard.clear();	
	}
}
