package socialocean.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import com.google.common.primitives.Ints;

import socialocean.controller.ColorInterpolator;
import socialocean.controller.MapController;
import socialocean.model.MapGridRectangle;
import socialocean.model.Tweet;

public class GlyphPainter implements Painter<JXMapViewer> {
	private boolean antiAlias = true;

	// Zoomlvl to cells
	private MapController mapCon;
	private static Font font = new Font("TimesRoman", Font.BOLD, 14);
	private static int INNER_RADIUS = 20;
	private static int INNER_DIAMETER = 2 * INNER_RADIUS;

	public GlyphPainter(MapController mapCon) {
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
		Rectangle viewport = map.getViewportBounds();
		g.translate(-viewport.x, -viewport.y);

		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));

		int outerDiameter = mapCon.getCellSize();
		int outerRadius = outerDiameter / 2;
		int zoom = map.getZoom();
		Map<MapGridRectangle, List<String>> cells = mapCon.getGridCells(zoom);
		
		if (cells == null) {
			return;
		}
		
		for (Rectangle r : cells.keySet()) {
			// Draw the glyph within the cells
			List<String> yards = cells.get(r);
			int centerX = (int) r.getCenterX();
			int centerY = (int) r.getCenterY();

			// Skip if it is outside viewport
			if (!r.intersects(viewport)) {
				continue;
			}

			// Define bounding Shapes
			Rectangle innerBounds = new Rectangle(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_DIAMETER,
					INNER_DIAMETER);
			Ellipse2D innerCircle = new Ellipse2D.Double(centerX - INNER_RADIUS, centerY - INNER_RADIUS, INNER_DIAMETER,
					INNER_DIAMETER);

			// Draw inner circle and number of polygons within
			g.setPaint(Color.WHITE);
			g.fill(innerCircle);
			g.setColor(Color.BLACK);
			drawCenteredString(g, String.valueOf(yards.size()), innerBounds, font);

			// Data structures monthly aggregated
			int[] numInfested = new int[12];
			double[] infestedProbabilityAvg = new double[12];
			int[] numNotInfested = new int[12];
			double[] notInfestedProbabilityAvg = new double[12];

			// Calculate Statistics
			for (int i = 0; i < 12; i++) {
				double sumProbabilityInf = 0.0d;
				double sumProbabilityNotInf = 0.0d;

				for (String y : yards) {
//					WineyardPrediction pRes = y.getPredictionRestult();
//					double[] probInf = pRes.getInfestedProbabilityPerMonth();
//					double[] probNotInf = pRes.getNotInfestedProbabilityPerMonth();
//					boolean[] inf = pRes.getInfestedPerMonth();
//					if (inf[i]) {
//						numInfested[i]++;
//						sumProbabilityInf += probInf[i];
//					} else {
//						numNotInfested[i]++;
//						sumProbabilityNotInf += probNotInf[i];
//					}
				}

				// TODO: this is a HACK sincedividing by #infested
				if (numNotInfested[i] > 0) {
					notInfestedProbabilityAvg[i] = sumProbabilityNotInf / numNotInfested[i];
				}
				if (numInfested[i] > 0) {
					infestedProbabilityAvg[i] = sumProbabilityInf / numInfested[i];
				}
			}

			int minRadius = INNER_RADIUS;
			int maxRadius = outerRadius;
			double minCertainty = 0.5d;
			double maxCertainty = 1.0d;

			// Calc visual encoding and draw
//			int maxRadiusValue = Math.max(Ints.max(numNotInfested), Ints.max(numInfested));
//			int minRadiusValue = 0;
//			Area innerArea = new Area(innerCircle);
//			for (int i = 0; i < 12; i++) {
//				// Create all areas we need to draw
//				int arcStart = 360 - ((i + 1) * 30) + 90;
//				int arcLength = 360 / 12;
//				Arc2D overallArc = new Arc2D.Double(r, arcStart, arcLength, Arc2D.PIE);
//				Area notInfestedArcArea = new Area();
//				if (numNotInfested[i] > 0) {
//					if (notInfestedProbabilityAvg[i] == 0.0d) {
//						System.out.println("Probability is 0");
//					}
//
//					// It might be that min max and value = 0 which results in NaN
//					double normalizedCountNotInf = (double) (numNotInfested[i] - minRadiusValue)
//							/ (maxRadiusValue - minRadiusValue);
//					int radiusNotInf = (int) (normalizedCountNotInf * (maxRadius - minRadius) + minRadius);
//					int diameterNotInf = 2 * radiusNotInf;
//					int xyOffsetNotInf = maxRadius - radiusNotInf;
//					Rectangle boundsNotInf = new Rectangle(r.x + xyOffsetNotInf, r.y + xyOffsetNotInf, diameterNotInf,
//							diameterNotInf);
//					Arc2D notInfestedArc = new Arc2D.Double(boundsNotInf, arcStart, arcLength, Arc2D.PIE);
//					notInfestedArcArea = new Area(notInfestedArc);
//					notInfestedArcArea.subtract(innerArea);
//					// System.out.println(notInfestedProbabilityAvg[i]);
//					// g.setPaint(this.getColorBasedSaturation(new Color(0, 0,
//					// 255), notInfestedProbabilityAvg[i],
//					// minCertainty, maxCertainty));
//
//					g.setPaint(ColorInterpolator.getInterpolatedColorBlue(minCertainty, maxCertainty,
//							notInfestedProbabilityAvg[i]));
//					g.fill(notInfestedArcArea);
//				}
//				if (numInfested[i] > 0) {
//					if (infestedProbabilityAvg[i] == 0.0d) {
//						System.out.println("Probability is 0");
//					}
//
//					Area infestedArcArea = new Area(overallArc);
//					infestedArcArea.subtract(notInfestedArcArea);
//					infestedArcArea.subtract(innerArea);
//
//					// g.setPaint(this.getColorBasedSaturation(new Color(255, 0,
//					// 0), infestedProbabilityAvg[i], minCertainty,
//					// maxCertainty));
//					g.setPaint(ColorInterpolator.getInterpolatedColorRed(minCertainty, maxCertainty,
//							infestedProbabilityAvg[i]));
//					g.fill(infestedArcArea);
//				}
//
//				Area overallArcArea = new Area(overallArc);
//				overallArcArea.subtract(innerArea);
//				g.setPaint(Color.BLACK);
//				g.draw(overallArcArea);
//
//			}
		}

		// Draw Legend
//		int generalPadding = 5;
//		int margin = 10;
//		int legendWidth = 300;
//		int legendHeight = 120;
//		int legendX = (int) (viewport.x + viewport.getWidth() - legendWidth - 5);
//		int legendY = (int) (viewport.y + viewport.getHeight() - legendHeight - 5);
//		Rectangle legend = new Rectangle(legendX, legendY, legendWidth, legendHeight);
//
//		g.setPaint(Color.WHITE);
//		g.fill(legend);
//		g.setPaint(Color.BLACK);
//		g.draw(legend);
//
//		// Infested
//		g.setPaint(Color.BLACK);
//		g.setFont(font);
//		g.drawString("Probability Infested:", legendX + margin, legendY + font.getSize() + generalPadding);
//		int innerRecYOffset = font.getSize() + margin;
//		Rectangle legendGradientRed = new Rectangle(legend.x + margin, legend.y + innerRecYOffset,
//				legendWidth - (2 * margin), 10);
//		GradientPaint redToWhite = new GradientPaint(legendGradientRed.x, legendGradientRed.y,
//				ColorInterpolator.START_COLOR, (int) (legendGradientRed.x + legendGradientRed.getWidth()),
//				legendGradientRed.y, ColorInterpolator.END_COLOR_RED);
//		g.setPaint(redToWhite);
//		g.fill(legendGradientRed);
//		g.setPaint(Color.BLACK);
//		g.draw(legendGradientRed);
//
//		int textY = (int) (legendGradientRed.y + legendGradientRed.getHeight() + font.getSize());
//		g.drawString("Low", legendGradientRed.x, textY);
//
//		FontMetrics metrics = g.getFontMetrics(font);
//		String lHighText = "High";
//		g.drawString(lHighText,
//				(int) (legendGradientRed.x + legendGradientRed.getWidth() - metrics.stringWidth(lHighText)), textY);
//
//		// Not Infested
//		int newY = (int) (textY + font.getSize());
//		g.setPaint(Color.BLACK);
//		g.setFont(font);
//		g.drawString("Probability Not Infested:", legendX + margin, newY + font.getSize() + generalPadding);
//		Rectangle legendGradientBlue = new Rectangle(legendX + margin, newY + innerRecYOffset,
//				legendWidth - (2 * margin), 10);
//		GradientPaint blueToWhite = new GradientPaint(legendGradientBlue.x, legendGradientBlue.y,
//				ColorInterpolator.START_COLOR, (int) (legendGradientBlue.x + legendGradientBlue.getWidth()),
//				legendGradientBlue.y, ColorInterpolator.END_COLOR_BLUE);
//		g.setPaint(blueToWhite);
//		g.fill(legendGradientBlue);
//		g.setPaint(Color.BLACK);
//		g.draw(legendGradientBlue);
//
//		textY = (int) (legendGradientBlue.y + legendGradientBlue.getHeight() + font.getSize());
//		g.drawString("Low", legendGradientBlue.x, textY);
//		g.drawString(lHighText,
//				(int) (legendGradientBlue.x + legendGradientBlue.getWidth() - metrics.stringWidth(lHighText)), textY);

		g.dispose();
	}

	/*
	 * private Color getColorBasedSaturation(Color baseColor, double val, double
	 * min, double max) { float normalizedSaturation = Math.max(0.2f, (float) ((val
	 * - min) / (max - min))); // Get HUE, SATURATION, BRIGHTNESS float[] hsbVals =
	 * new float[3]; Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(),
	 * baseColor.getBlue(), hsbVals); baseColor = new
	 * Color(Color.HSBtoRGB(hsbVals[0], normalizedSaturation, hsbVals[2])); return
	 * baseColor; }
	 */

	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param g
	 *            The Graphics instance.
	 * @param text
	 *            The String to draw.
	 * @param rect
	 *            The Rectangle to center the text in.
	 */
	public void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(font);
		// Determine the X coordinate for the text
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		// Determine the Y coordinate for the text (note we add the ascent, as
		// in java 2d 0 is top of the screen)
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		// Set the font
		g.setFont(font);
		// Draw the String
		g.drawString(text, x, y);
	}

}
