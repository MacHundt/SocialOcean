package impl;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;

import socialocean.controller.MapController;

public class MyCenterMapListener extends MouseAdapter {

	
private JXMapViewer viewer;
private MapController mapCon;
	
/**
 * Centers the map on the mouse cursor
 * if left is double-clicked or middle mouse
 * button is pressed.
 * @author Martin Steiger
 * @author joshy
 */
	public MyCenterMapListener(JXMapViewer viewer, MapController mapCon)
	{
		this.viewer = viewer;
		this.mapCon = mapCon;
	}

	@Override
	public void mousePressed(MouseEvent evt)
	{
		boolean left = SwingUtilities.isLeftMouseButton(evt);
		boolean middle = SwingUtilities.isMiddleMouseButton(evt);
		boolean doubleClick = (evt.getClickCount() == 2);

		if (middle || (left && doubleClick))
		{
			recenterMap(evt);
		}
	}
	
	private void recenterMap(MouseEvent evt)
	{
		Rectangle bounds = viewer.getViewportBounds();
		double x = bounds.getX() + evt.getX();
		double y = bounds.getY() + evt.getY();
		viewer.setCenter(new Point2D.Double(x, y));
                viewer.setZoom(viewer.getZoom() - 1);
		
//        viewer.repaint();
		
		MapPanelCreator.zoom = viewer.getZoom();
		// Redraw Map
		mapCon.zoomChanged();
	}
}
