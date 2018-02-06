package socialocean.model;

import java.awt.Color;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class MapCountries extends MultiPolygon{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5214603883773628688L;


	private Color backgroundColor = null;
	private String name = "";
	
	public MapCountries(Polygon[] polygons, GeometryFactory factory, String name) {
		super(polygons, factory);
		this.name = name;
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public String getName() {
		return name;
	}

	public void setBackgroundColor(Color backgroundcColor) {
		this.backgroundColor = backgroundcColor;
	}
	
	

}
