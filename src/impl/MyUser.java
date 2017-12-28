package impl;


public class MyUser {
	
	private String id;
	private String name;
	private String language = "";
	private double cred_score = 0.5;
	private String toString = "";
	private String gender = "unknown";
	private boolean hasGeo = false;
	private double latitude;
	private double longitude;
	
	
	private int degree = 0;
	private double betweenness = 0.0;
	private int alpha = 255;
	
	public MyUser(String id, String screenName) {
		this.setId(id);
		name = screenName;
	}
	
	public void addLanguage(String language) {
		this.language = language;
	}
	
	public void addCredibility(double cred_score) {
		this.cred_score = cred_score;
	}
	
	public void addGender(String gender) {
		this.gender = gender;
	}
	
	public void addPoint(double lat, double longi) {
		hasGeo = true;
		latitude = lat;
		longitude = longi;
	}
	
	
	public void setNameVisible() {
		toString = name;
	}
	
	public boolean hasGeo() {
		return hasGeo;
	}
	

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	
	public String getName() {
		return name;
	}
	
	public String getGender() {
		return gender;
	}
	

	@Override
	public String toString() {
		return toString;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getDegree() {
		return degree;
	}

	public void addDegree(int degree) {
		this.degree = degree;
	}

	public double getBetweennessScore() {
		return betweenness;
	}

	public void addBetweennessScore(double betweenness) {
		this.betweenness = betweenness;
	}

	public int getAlpha() {
		return alpha;
	}

	public void addAlpha(int alpha) {
		this.alpha = alpha;
	}
}
