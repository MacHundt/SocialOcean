package impl;

import java.awt.Color;
import java.util.Date;

public class MyEdge {

	/**
	 *  
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	private String id;
	private String content;
	
//	private double sentiment;
	private int pos;				// from sentiStrength lib, polarization
	private int neg;
	private String sentiment;
	private String language = "";
	private double cred_score = 0.5;
	private boolean hasGeo = false;
	private double latitude;
	private double longitude;
	private String relationsip ="";
	private String category;
	private Date date;
	
	private Color c = new Color(255,255,255);
	
	private double weight = 1.0;
	private double betweennessScore = 0.0;
	
	private String toString = "";

	private String source;

	private String target;
	
	public MyEdge(String id) {
		this.id = id;
	}
	
	public enum LabelType {
		Credibility, Sentiment, SentiStrenth, Category
	};
	
	public String getRelationsip() {
		return relationsip;
	}

	public Date getDate() {
		return date;
	}

	public void addDate(Date date2) {
		this.date = date2;
	}

	public String getCategory() {
		return category;
	}

	public void addCategory(String category) {
		this.category = category;
	}

	public void setRelationsip(String relationsip) {
		this.relationsip = relationsip;
	}

	public void addLanguage(String lanuage) {
		this.language = language;
	}
	
	public void addCredibility(double cred_score) {
		this.cred_score = cred_score;
	}
	
//	public void addSentiment(double sentiment) {
//		this.sentiment = sentiment;
//	}
	
	public void addSentiment(String sentiment) {
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

//	public double getSentiment() {
//		return sentiment;
//	}
	
	public String getSentiment() {
		return sentiment;
	}

	public double getCred_score() {
		return cred_score;
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

	@Override
	public String toString() {
		return toString;
	}
	
	public void changeToString(LabelType type) {
		
		switch (type) {
		case Credibility:
			toString = cred_score+"";
			break;
		case Sentiment:
			toString = sentiment;
			break;
		case Category: 
			toString = category;
			break;
		case SentiStrenth:
			toString =  "("+pos+","+neg+")";
			break;

		default:
			toString = "("+pos+","+neg+")";
			break;
		}
	}

	public int getPos() {
		return pos;
	}

	public void addPos(int pos) {
		this.pos = pos;
	}

	public int getNeg() {
		return neg;
	}

	public void addNeg(int neg) {
		this.neg = neg;
	}

	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void addSource(String name) {
		this.source = name;
		
	}
	
	public String getSource() {
		return source;
	}

	public void addTarget(String target) {
		this.target = target;
		
	}
	
	public String getTarget() {
		return target;
	}

	public double getBetweennessScore() {
		return betweennessScore;
	}

	public void addBetweennessScore(double betweennessScore) {
		this.betweennessScore = betweennessScore;
	}

	public Color getColor() {
		return c;
	}

	public void addColor(Color c) {
		this.c = c;
	}
	
}
