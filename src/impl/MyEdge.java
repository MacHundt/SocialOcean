package impl;

public class MyEdge {

	/**
	 *  
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	private String id;
	private String content;
	
	private String sentiment;
	private String language = "";
	private double cred_score = 0.5;
	
	public MyEdge(String id) {
		this.id = id;
	}
	
	public void addLanguage(String lanuage) {
		this.language = language;
	}
	
	public void addCredibility(double cred_score) {
		this.cred_score = cred_score;
	}
	
	public String getId() {
		return id;
	}
	
	

	@Override
	public String toString() {
		return cred_score+"";
	}
}
