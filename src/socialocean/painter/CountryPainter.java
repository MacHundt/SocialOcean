package socialocean.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Polygon;

import socialocean.controller.MapController;
import socialocean.model.MapCountries;
import utils.GeoToCartesianTransformation;
import utils.Lucene;

public class CountryPainter implements Painter<JXMapViewer> {
	
	private boolean antiAlias = true;
	
	// Zoomlvl to cells
	private MapController mapCon;
	
	public CountryPainter(MapController mapcon) {
		super();
		this.mapCon = mapcon;
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
		
		// convert from viewport to world bitmap
		Rectangle viewport = map.getViewportBounds();
		g.translate(-viewport.x, -viewport.y);
		
		ShapeWriter sw = new ShapeWriter(new GeoToCartesianTransformation(map));
		
		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(1));
		
		int zoom = map.getZoom();
		Map<MapCountries, List<Document>> countries = mapCon.getCountries(zoom);
		
		if (countries == null)
			return;
		
		for (MapCountries c : countries.keySet()) {
			g.setStroke(new BasicStroke(1));

			if (c.getBackgroundColor() != null) {
				g.setColor(c.getBackgroundColor());
				for (int i = 0; i < c.getNumGeometries(); i++) {
					Polygon p = (Polygon) c.getGeometryN(i);
					Shape s = sw.toShape(p);
					g.fill(s);
				}
			}

			g.setColor(Color.GRAY);
			for (int i = 0; i < c.getNumGeometries(); i++) {
				Polygon p = (Polygon) c.getGeometryN(i);
				Shape s = sw.toShape(p);
				g.draw(s);
			}
			
		}

		g.dispose();
	}

}
