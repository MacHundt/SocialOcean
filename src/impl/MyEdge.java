package impl;

public class MyEdge {

	/**
	 *  
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	private String id;
	private String content;
	
	private double sentiment;
	private String language = "";
	private double cred_score = 0.5;
	private boolean hasGeo = false;
	private double latitude;
	private double longitude;
	
	public MyEdge(String id) {
		this.id = id;
	}
	
	public void addLanguage(String lanuage) {
		this.language = language;
	}
	
	public void addCredibility(double cred_score) {
		this.cred_score = cred_score;
	}
	
	public void addSentiment(double sentiment) {
		this.sentiment = sentiment;
	}
	
	public void addPoint(double lat, double longi) {
		hasGeo = true;
		latitude = lat;
		longitude = longi;
	}
	
	public void addContent(String message) {
		this.content = message;
	}
	
	public String getId() {
		return id;
	}
	

	public String getContent() {
		return content;
	}

	public double getSentiment() {
		return sentiment;
	}

	public double getCred_score() {
		return cred_score;
	}

	public boolean isHasGeo() {
		return hasGeo;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return cred_score+"";
	}
}
