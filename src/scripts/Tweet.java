package scripts;


public class Tweet {
	
	 private String tweet_id;
 	 private String tweet_creationdate = "";
	 private String tweet_content = "";
	 private long tweet_replytostatus;
	 private double latitude = 0;
	 private double longitude = 0;
	 private String tweet_source = "";
	 private boolean hasurl;
	 private long user_id;
	 private String user_screenname;
	 private int positive;
	 private int negative;
	 private String category = "";
	 
	 // ...
	 
	 public Tweet(String id){
		 tweet_id = id;
	 }
	 
	 public void setUserScreenName(String name) {
		 user_screenname = name;
	 }
	 
	 public String getUserScreenName()	{
		 return user_screenname;
	 }
	 
	 public String getTweet_creationdate() {
		return tweet_creationdate;
	}
	public void setTweet_creationdate(String tweet_creationdate) {
		this.tweet_creationdate = tweet_creationdate;
	}
	public String getTweet_content() {
		return tweet_content;
	}
	public void setTweet_content(String tweet_content) {
		this.tweet_content = tweet_content;
	}
	public long getTweet_replytostatus() {
		return tweet_replytostatus;
	}
	public void setTweet_replytostatus(long tweet_replytostatus) {
		this.tweet_replytostatus = tweet_replytostatus;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getTweet_source() {
		return tweet_source;
	}
	public void setTweet_source(String tweet_source) {
		this.tweet_source = tweet_source;
	}
	public boolean isHasurl() {
		return hasurl;
	}
	public void setHasurl(boolean hasurl) {
		this.hasurl = hasurl;
	}
	public long getUser_id() {
		return user_id;
	}
	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}
	public int getPositive() {
		return positive;
	}
	public void setPositive(int positive) {
		this.positive = positive;
	}
	public int getNegative() {
		return negative;
	}
	public void setNegative(int negative) {
		this.negative = negative;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTweet_id() {
		return tweet_id;
	}

}
