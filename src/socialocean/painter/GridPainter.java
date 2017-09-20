package socialocean.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import socialocean.controller.MapController;
import socialocean.model.MapGridRectangle;

public class GridPainter implements Painter<JXMapViewer> {
	private boolean antiAlias = true;

	// Zoomlvl to cells
	private MapController mapCon;

	public GridPainter(MapController mapCon) {
		super();
		this.mapCon = mapCon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.swingx.painter.Painter#paint(java.awt.Graphics2D,
	 * java.lang.Object, int, int)
	 */
	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
//		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(1));

		int zoom = map.getZoom();
		Map<MapGridRectangle, List<Document>> cells = mapCon.getGridCells(zoom);
		
		if (cells == null)
			return;
		
		for (MapGridRectangle r : cells.keySet()) {
			g.setStroke(new BasicStroke(1));

			if (r.getBackgroundColor() != null) {
				g.setColor(r.getBackgroundColor());
				g.fill(r);
			}

			g.setColor(Color.GREEN);
			g.draw(r);
		}

		g.dispose();
	}
}
