package utils;

import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.awt.PointTransformation;
import com.vividsolutions.jts.geom.Coordinate;

public class GeoToCartesianTransformation implements PointTransformation {

	JXMapViewer map;
	
	public GeoToCartesianTransformation(JXMapViewer map) {
		this.map = map;
	}
	
	@Override
	public void transform(Coordinate src, Point2D dest) {
		GeoPosition g = new GeoPosition(src.y, src.x);
		Point2D gp = map.getTileFactory().geoToPixel(g, map.getZoom());
		dest.setLocation(gp);
	}

}
