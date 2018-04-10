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
	private int inDegree = 0;
	private int outDegree = 0;
	
	
	private int degree = 0;
	private double betweenness = 0.0;
	private int alpha = 255;
	private int statuses;
	private int followers;
	private String creation_date;
	private int geocoding_type;
	private double desc_score;
	private int listed;
	private int friends;
	private int clusterID;
	
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

	public int getInDegree() {
		return inDegree;
	}

	public void setInDegree(int inDegree) {
		this.inDegree = inDegree;
	}

	public int getOutDegree() {
		return outDegree;
	}

	public void setOutDegree(int outDegree) {
		this.outDegree = outDegree;
	}
	
	public void incOutDegree() {
		this.outDegree++;
	}
	
	public void incInDegree() {
		this.inDegree++;
	}

	public void addStatuses(int statuses) {
		this.statuses = statuses;
		
	}

	public void addFollowers(int followers) {
		this.followers = followers;
		
	}

	public void addFriends(int friends) {
		this.friends = friends;
		
	}
	

	public String getLanguage() {
		return language;
	}

	public double getCred_score() {
		return cred_score;
	}

	public double getBetweenness() {
		return betweenness;
	}

	public int getStatuses() {
		return statuses;
	}

	public int getFollowers() {
		return followers;
	}

	public String getCreation_date() {
		return creation_date;
	}

	public int getGeocoding_type() {
		return geocoding_type;
	}

	public double getDesc_score() {
		return desc_score;
	}

	public int getListed() {
		return listed;
	}

	public int getFriends() {
		return friends;
	}

	public void addListed(int listed) {
		this.listed = listed;
		
	}

	public void addDescScore(double desc_score) {
		this.desc_score = desc_score;
		
	}

	public void addGeocodingType(int geocoding_type) {
		this.geocoding_type = geocoding_type;
		
	}

	public void addCreationDate(String creationdate) {
		this.creation_date = creationdate;
		
	}

	public void addClusterID(int clusterID) {
		this.clusterID = clusterID;
	}
	
	public int getClusterID() {
		return clusterID;
	}
}
