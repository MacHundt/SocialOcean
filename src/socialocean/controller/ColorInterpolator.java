package socialocean.controller;

import java.awt.Color;

public class ColorInterpolator {
	public static final Color START_COLOR = new Color(0.865f, 0.865f, 0.865f);
	public static final Color END_COLOR_BLUE = new Color(0.230f, 0.299f, 0.754f);
	private static int[] DIFF_BLUE = new int[]{
			START_COLOR.getRed() - END_COLOR_BLUE.getRed(),
			START_COLOR.getGreen() - END_COLOR_BLUE.getGreen(),
			START_COLOR.getBlue() - END_COLOR_BLUE.getBlue()
	};

	public static final Color END_COLOR_RED = new Color(0.706f, 0.016f, 0.150f);
	private static int[] DIFF_RED = new int[]{
			START_COLOR.getRed() - END_COLOR_RED.getRed(),
			START_COLOR.getGreen() - END_COLOR_RED.getGreen(),
			START_COLOR.getBlue() - END_COLOR_RED.getBlue()
	};
	
	public static Color getInterpolatedColorRed(double min, double max, double val) {
		double fraction = getFraction(min, max, val);
		return new Color(START_COLOR.getRed() - (int)(fraction * DIFF_RED[0]),
				START_COLOR.getGreen() - (int)(fraction * DIFF_RED[1]),
				START_COLOR.getBlue() - (int)(fraction * DIFF_RED[2]));
	}
	
	public static Color getInterpolatedColorBlue(double min, double max, double val) {
		double fraction = getFraction(min, max, val);
		
//		System.out.println(min + " " + max + " " + val);
//		System.out.println(fraction);
//		System.out.println(START_COLOR.getRed() + " - " + DIFF_BLUE[0]);
//		System.out.println(START_COLOR.getGreen() + " - " + DIFF_BLUE[0]);
//		System.out.println(START_COLOR.getBlue() + " - " + DIFF_BLUE[0]);
		
		return new Color(START_COLOR.getRed() - (int)(fraction * DIFF_BLUE[0]),
				START_COLOR.getGreen() - (int)(fraction * DIFF_BLUE[1]),
				START_COLOR.getBlue() - (int)(fraction * DIFF_BLUE[2]));
	}
	
	private static double getFraction(double min, double max, double val) {
		return (val - min) / (max - min);
	}
}
