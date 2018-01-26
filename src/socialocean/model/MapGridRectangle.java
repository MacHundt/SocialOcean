package socialocean.model;

import java.awt.Color;
import java.awt.Rectangle;

public class MapGridRectangle extends Rectangle implements Comparable<MapGridRectangle>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 781683022179294509L;

	private Color backgroundColor = null;

	public MapGridRectangle(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundcColor) {
		this.backgroundColor = backgroundcColor;
	}

	@Override
	public int compareTo(MapGridRectangle o) {
		return this.compareTo(o);
	}

}
