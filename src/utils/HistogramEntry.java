package utils;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import utils.Lucene.ColorScheme;

public class HistogramEntry implements Comparable<HistogramEntry>{
	
	private String categoryName;
	private double sumSentiment = 0.0;
	private double sumPos = 0.0;
	private double sumNeg = 0.0;
	private int count = 0;
	
	private double sentiment  = sumSentiment;
	
	public HistogramEntry(String categoryName) {
		this.categoryName = categoryName;
	}
	
	
	public void count() {
		count++;
	}
	
	
	public int getCount() {
		return count;
	}
	
	
	public void addSentiment(double sentiment, double pos, double neg) {
		sumSentiment += sentiment;
		sumPos += pos;
		sumNeg += neg;
	}
	
	public double getAvgSentiment() {
		Lucene l = Lucene.INSTANCE;
		if (l.getColorScheme().equals(Lucene.ColorScheme.SENTIMENT)) {
			sentiment = sumSentiment;
		} else if (l.getColorScheme().equals(Lucene.ColorScheme.SENTISTRENGTH)) {
			sentiment = sumPos+sumNeg;
		}
		
		return (sentiment / count);
	}
	
	
	public String getName()	{
		return categoryName;
	}
	
	
	public Color getAvgSentimentColor(ColorScheme colorScheme) {
		
//		Color color = new Color(Display.getDefault(), 204, 204, 204);		// grey
		Color color = new Color(Display.getDefault(), 255,255,191);
		
		if (colorScheme.equals(Lucene.ColorScheme.SENTIMENT)) {
			sentiment = sumSentiment;
		} else if (colorScheme.equals(Lucene.ColorScheme.SENTISTRENGTH)) {
			sentiment = sumPos+sumNeg;
		}

		// Positive tendency
		if ((sentiment / count) > 0) {
			if ((sentiment / count) < 0.25) {
				color = new Color(Display.getDefault(), 217, 239, 139);
				return color;
			} else if ((sentiment / count) < 0.5) {
				color = new Color(Display.getDefault(), 166, 217, 106);
				return color;
			} else if ((sentiment / count) < 0.75) {
				color = new Color(Display.getDefault(), 102, 189, 99);
				return color;
			} else if ((sentiment / count) >= 0.75) {
				color = new Color(Display.getDefault(), 26, 152, 80);
				return color;
			}
		}
		// Negative tendency
		else if ((sentiment / count) < 0) {
			if ((sentiment / count) > -0.25) {
				color = new Color(Display.getDefault(), 254, 224, 139);
				return color;
			} else if ((sentiment / count) > -0.5) {
				color = new Color(Display.getDefault(), 253, 174, 97);
				return color;
			} else if ((sentiment / count) > -0.75) {
				color = new Color(Display.getDefault(), 244, 109, 67);
				return color;
			} else if ((sentiment / count) <= -0.75) {
				color = new Color(Display.getDefault(), 215, 48, 39);
				return color;
			}
		}
		
		return color;
	}
	
	
	
//	public Color getCategoryColor() {
//		Color color = new Color(Display.getDefault(), 0, 0, 0);
//		
//		Color pink = new Color(Display.getDefault(), 250, 22, 129);		// Default
//		
//		switch (categoryName.toLowerCase()) {
//		case "computers_technology":
//			color = new Color(Display.getDefault(), 228, 26, 28);
//			break;
//		case "education":
//			color = new Color(Display.getDefault(), 102, 99, 14);
//			break;
//		case "family":
//			color = new Color(Display.getDefault(), 65, 148, 134);
//			break;
//		case "food":
//			color = new Color(Display.getDefault(), 91, 157, 90);
//			break;
//		case "health":
//			color = new Color(Display.getDefault(), 145, 87, 155);
//			break;
//		case "marketing":
//			color = new Color(Display.getDefault(), 218, 109, 59);
//			break;
//		case "music":
//			color = new Color(Display.getDefault(), 255, 174, 19);
//			break;
//		case "news_media":
//			color = new Color(Display.getDefault(), 247, 240, 50);
//			break;
//		case "other":
//			color = new Color(Display.getDefault(), 182, 117, 42);
//			break;
//		case "pets":
//			color = new Color(Display.getDefault(), 210, 109, 122);
//			break;
//		case "politics":
//			color = new Color(Display.getDefault(), 221, 136, 181);
//			break;
//		case "recreation_sports":
//			color = new Color(Display.getDefault(), 153, 153, 153);
//			break;
//
//		default:
//			color = pink;
//			break;
//		}
//		
//		return color;
//	}


	@Override
	public int compareTo(HistogramEntry o) {
		return Double.compare(this.getAvgSentiment(), o.getAvgSentiment());
	}
	

}
