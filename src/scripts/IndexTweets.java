package scripts;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import utils.DBManager;

public class IndexTweets {
	
	private static int Fetchsize = 10000;
	
//	private static String TWEETDATA = "tweetdata";
//	private static String TWEETDATA = "bb_tweets";
	private static String TWEETDATA = "nodexl_my2k_tweets";
//	private static String USERS = "users";
//	private static String indexPath = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index_tweets/";
	private static String indexPath = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index_nodexl/";

	private static boolean LOCAL = true;
	
	// index all tweets from DB
	public static void main(String[] args) {
		
		Connection c = DBManager.getConnection(LOCAL, false);
		boolean create = true;	// create new Index
		
		Date start = new Date();
		System.out.println("Indexing to directory '" + indexPath + "'...");

		try {
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			
			FileReader reader = new FileReader(new File("./stopwords/stopwords.txt"));
			Analyzer analyzer = new StandardAnalyzer(reader);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			c.setAutoCommit(false);
			Statement stmt = c.createStatement();
			stmt.setFetchSize(Fetchsize);
			
//			String query = "Select "
//					+ "tweet_id, "
//					+ "tweet_creationdate, "
//					+ "tweet_content,"
//					+ "tweet_replytostatus,"
//					+ "latitude, "
//					+ "longitude, "
//					+ "tweet_source, "
//					+ "hasurl, "
//					+ "user_id, "
//					+ "user_screenname, "
//					+ "positive, "
//					+ "negative, "
//					+ "category, "
//					+ "sentiment "
//					+ "from "+TWEETDATA;
			
			String query = "Select "
					+ "tweet_id, "
					+ "tweet_creationdate, "
					+ "tweet_content, "
					+ "relationship, "
					+ "latitude, "
					+ "longitude, "
					+ "hasurl, "
					+ "source, "
					+ "positive, "
					+ "negative, "
					+ "category, "
					+ "sentiment "
					+ "from "+TWEETDATA;
			
			  
			ResultSet rs = stmt.executeQuery(query);
			
			ArrayList<Tweet> tweets = new ArrayList<>(Fetchsize);
			
			int doc_counter = 0;
			int counter = 0;
			int stat = 1;
//			int topX = 1;
			while (rs.next()) {
				counter++;
//				Tweet t = new Tweet(rs.getString(1));
//				t.setTweet_creationdate(rs.getString(2));
//				t.setTweet_content(rs.getString(3));
//				t.setTweet_replytostatus(rs.getLong(4));
//				t.setLatitude(rs.getDouble(5));
//				t.setLongitude(rs.getDouble(6));
//				t.setTweet_source(rs.getString(7));
//				t.setHasurl(rs.getBoolean(8));
//				t.setUser_id(rs.getLong(9));
//				t.setUserScreenName(rs.getString(10));
//				t.setPositive(rs.getInt(11));
//				t.setNegative(rs.getInt(12));
//				t.setCategory((rs.getString(13) != null) ? rs.getString(13) : "Other");
//				t.setSentiment((rs.getString(14) != null) ? rs.getString(14) : "neu");
				
				Tweet t = new Tweet(rs.getString("tweet_id"));
				t.setTweet_creationdate(rs.getString("tweet_creationdate"));
				t.setTweet_content(rs.getString("tweet_content"));
				t.setRelationship(rs.getString("relationship"));
				t.setLatitude(rs.getDouble("latitude"));
				t.setLongitude(rs.getDouble("longitude"));
				t.setHasurl(rs.getBoolean("hasurl"));
				t.setUserScreenName(rs.getString("source"));
				t.setPositive(rs.getInt("positive"));
				t.setNegative(rs.getInt("negative"));
				t.setCategory((rs.getString("category") != null) ? rs.getString(11) : "other");
				t.setSentiment((rs.getString("sentiment") != null) ? rs.getString(12) : "neu");
				
				tweets.add(t);
				
				if (counter == Fetchsize) {
					indexTweets(writer, tweets, doc_counter);
					counter = 0;
					tweets.clear();
					doc_counter++;
					if (doc_counter % 100 == 0) {
						System.out.println(". >>"+ Fetchsize*100*stat+" tweets processed");
						stat++;
//						if (topX-- == 0)
//							break;
					}				
					else {
						System.out.print(".");
					}
				}
			}
			
			writer.close();

			Date end = new Date();
			System.out.println();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
			System.out.println("END");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private static void indexTweets(IndexWriter writer, ArrayList<Tweet> tweets, int doc_counter) {

		Document doc = new Document();
		long counterLines = 0;
		int no_geo = 0;
		String content = "";
		String tags = "";
		String mentions = "";
		double longi = 0;
		double lati = 0;
		String category = "";
		for (Tweet t : tweets) {
			doc.clear();
			// type
			doc.add(new StringField("type", "twitter", Field.Store.YES));
			doc.add(new StringField("id", t.getTweet_id(), Field.Store.YES));
			
			
//			doc.add(new StringField("isRetweet", (t.getTweet_replytostatus() > 0) ? "true" : "false", Field.Store.NO));
			doc.add(new StringField("relationship", t.getRelationship(), Field.Store.YES));

			
			category = t.getCategory();
			category = category.replace(" & ", "_").toLowerCase();
			doc.add(new StringField("category", category, Field.Store.YES));
		
			doc.add(new StringField("hasURL", (t.isHasurl())? "true" : "false" , Field.Store.YES));
			
			// User_ScreenName
			doc.add(new StringField("name", t.getUserScreenName(), Field.Store.YES));
			
			// TweetSource
//			doc.add(new StringField("source", t.getTweet_source(), Field.Store.YES));
			
//			// User_id
//			doc.add(new StringField("uid", t.getUser_id()+"", Field.Store.YES));
			
			// Sentiment
			doc.add(new StringField("sentiment", t.getSentiment(), Field.Store.YES));
			doc.add(new StringField("neg", t.getNegative()+"", Field.Store.YES));
			doc.add(new StringField("pos", t.getPositive()+"", Field.Store.YES));
//			doc.add(new IntPoint("neg",t.getNegative()));
//			doc.add(new StoredField("neg", t.getNegative()));
//			doc.add(new IntPoint("pos",t.getPositive()));
//			doc.add(new StoredField("pos", t.getPositive()));
			
			// # tags
			content = t.getTweet_content();
			
			if (content == null)
				continue;
			
			tags = getTagsFromTweets(content);
			;
			TextField tag_field = new TextField("tags", tags, Field.Store.YES);
			doc.add(tag_field);

			// @ mentions
			mentions = getMentionsFromTweets(content);
			StringField hasMention = new StringField("has@", (mentions.isEmpty()) ? "false" : "true", Field.Store.YES);
			doc.add(hasMention);
			TextField mention_field = new TextField("mention", mentions, Field.Store.NO);
			doc.add(mention_field);

//			doc.add(mention_field);
//			doc.add(new StringField("has@", (!mentions.isEmpty()) ? "true" : "false", Field.Store.YES));

			// fulltext
			content = content.replaceAll("\"", "");
			TextField content_field = new TextField("content", content, Field.Store.NO);
			doc.add(content_field);

			// geo
			lati = t.getLatitude();
			longi = t.getLongitude();
			if (lati == 0 || longi == 0) {
				no_geo++;
			}

			GeoPointField geo = new GeoPointField("geo", lati, longi, GeoPointField.Store.YES);
			doc.add(geo);
			
			
			// time
			String[] datetime = t.getTweet_creationdate().split(" ");
			String date_Str = datetime[0];
			String time_Str = datetime[1];
			String[] date_arr = date_Str.trim().split("-");
			String[] time_arr = time_Str.trim().split(":");
			if (date_arr.length == 3 && time_arr.length == 3) {
				// DATE
				int year = Integer.parseInt(date_arr[0]);
				int month = Integer.parseInt(date_arr[1]);
				int day = Integer.parseInt(date_arr[2]);
				// TIME
				int hour = Integer.parseInt(time_arr[0]);
				int min = Integer.parseInt(time_arr[1]);
				int sec = Integer.parseInt(time_arr[2]);
				
				LocalDate date = LocalDate.of(year, month, day);
				LocalTime time = LocalTime.of(hour, min, sec);
				
//				boolean isbefor = date.isBefore(mindate);
//				boolean isafter = date.isAfter(maxdate);
//				
//				if (isbefor || isafter ) {
//					continue;
//				}
				
				LocalDateTime dt = LocalDateTime.of(date, time);
				long utc_time = dt.toEpochSecond(ZoneOffset.UTC);
//				String date_str = DateTools.dateToString(dt, Resolution.SECOND);
				doc.add(new StringField("date", ""+utc_time , Field.Store.YES ));
				
			} else {
				continue;
			}
			

			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document
				// can be there):
				counterLines++;
				try {
					writer.addDocument(doc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// Existing index (an old copy of this document may have
				// been indexed) so
				// we use updateDocument instead to replace the old one
				// matching the exact
				// path, if present:
				// System.out.println("updating " + file);
				// writer.updateDocument(new Term("path", file.toString()),
				// doc);
			}
		}

	}
	
	
	private static String getTagsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("#")) {
				output += token.substring(1) + " ";
			}
		}
		return output.trim();
	}
	
	
	private static String getMentionsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("@")) {
				output += token.substring(1) + " ";
			}
		}
		output = output.replace(":", "");
		return output.trim();
	}

}
