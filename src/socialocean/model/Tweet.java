package socialocean.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.Geometry;

public class Tweet {
	private Geometry geoPolygon;
	private int id;
	private List<GeoPosition> geos;
	private Map<Integer, Integer> zoomLevelClusterId;
	private String uniqueId;
	private Map<String, Double> features;

	public Tweet(int id, Geometry p) {
		this.id = id;
		this.uniqueId = UUID.randomUUID().toString();
		this.geoPolygon = p;
	}

	public Geometry getGeoPolygon() {
		return geoPolygon;
	}

	public void setGeoPolygon(Geometry polygon) {
		this.geoPolygon = polygon;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<GeoPosition> getGeos() {
		return geos;
	}

	public void setGeos(List<GeoPosition> geos) {
		this.geos = geos;
	}

	public Map<Integer, Integer> getZoomLevelClusterId() {
		return zoomLevelClusterId;
	}

	public void setZoomLevelClusterId(Map<Integer, Integer> zoomLevelClusterId) {
		this.zoomLevelClusterId = zoomLevelClusterId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public Map<String, Double> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, Double> features) {
		this.features = features;
	}

}
