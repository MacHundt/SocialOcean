package impl;


public class MyUser {
	
	/**
	 *  
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	private String id;
	private String name;
	private String language = "";
	private double cred_score = 0.5;
	
	
	public MyUser(String id, String screenName) {
		this.id = id;
		name = screenName;
	}
	
	public void addLanguage(String lanuage) {
		this.language = language;
	}
	
	public void addCredibility(double cred_score) {
		this.cred_score = cred_score;
	}
	
	

	@Override
	public String toString() {
		return name;
	}
}
