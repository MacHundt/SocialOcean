package impl;


public class MyUser {
	
	/**
	 *  
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	private String id;
	private String name;
	private String language = "";
	
	
	public MyUser(String id, String screenName) {
		this.id = id;
		name = screenName;
	}
	
	public void addLanguage(String lanuage) {
		this.language = language;
	}
	
	
	

	@Override
	public String toString() {
		return name;
	}
}
