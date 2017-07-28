package scripts;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
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

public class IndexTweets {
	
	private static int Fetchsize = 10000;
	
	private static LocalDate mindate = LocalDate.of(2013, 4, 14);
	private static LocalDate maxdate = LocalDate.of(2013, 4, 24);
	
//	private static String TWEETDATA = "tweetdata";
	private static String TWEETDATA = "bb_tweets";
	private static String USERS = "users";
	private static String indexPath = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index_tweets/";

	// index all tweets from DB
	public static void main(String[] args) {
		
		Connection c = getConnection();
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
			
			String query = "Select "
					+ "tweet_id, "
					+ "tweet_creationdate, "
					+ "tweet_content,"
					+ "tweet_replytostatus,"
					+ "latitude, "
					+ "longitude, "
					+ "tweet_source, "
					+ "hasurl, "
					+ "user_id, "
					+ "positive, "
					+ "negative, "
					+ "category "
					+ "from "+TWEETDATA;
			
			
			ResultSet rs = stmt.executeQuery(query);
			
			ArrayList<Tweet> tweets = new ArrayList<>(Fetchsize);
			
			int doc_counter = 0;
			int counter = 0;
			int stat = 1;
//			int topX = 2;
			while (rs.next()) {
				counter++;
				Tweet t = new Tweet(rs.getString(1));
				t.setTweet_creationdate(rs.getString(2));
				t.setTweet_content(rs.getString(3));
				t.setTweet_replytostatus(rs.getLong(4));
				t.setLatitude(rs.getDouble(5));
				t.setLongitude(rs.getDouble(6));
				t.setTweet_source(rs.getString(7));
				t.setHasurl(rs.getBoolean(8));
				t.setUser_id(rs.getLong(9));
				t.setPositive(rs.getInt(10));
				t.setNegative(rs.getInt(11));
				t.setCategory((rs.getString(12) != null) ? rs.getString(12) : "Other");
				
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
		String sentiment_string = "neutral";
		for (Tweet t : tweets) {
			doc.clear();

			doc.add(new StringField("id", t.getTweet_id(), Field.Store.YES));
			doc.add(new StringField("isRetweet", (t.getTweet_replytostatus() > 0) ? "true" : "false", Field.Store.YES));
			category = t.getCategory();
			category = category.replace(" & ", "_").toLowerCase();
			doc.add(new StringField("category", category, Field.Store.YES));
			
			// type
			Field typeField = new StringField("type", "twitter", Field.Store.YES);
			doc.add(typeField);
			
			// sentiment
			int neg = t.getNegative();
			int pos = t.getPositive();
			int sentiment = pos + neg;
			if (sentiment == -1)
				sentiment_string = "negative";		// for negative
			else if (sentiment == 1) 
				sentiment_string = "positive";
			else 
				sentiment_string = "neutral";
			
			doc.add(new StringField("sentiment", sentiment_string, Field.Store.YES));

			// # tags
			content = t.getTweet_content();
			tags = getTagsFromTweets(content);
			;
			TextField tag_field = new TextField("tags", tags, Field.Store.NO);
			doc.add(tag_field);

			// @ mentions
			mentions = getMentionsFromTweets(content);
			TextField mention_field = new TextField("mention", mentions, Field.Store.NO);

			doc.add(mention_field);
			doc.add(new StringField("has@", (!mentions.isEmpty()) ? "true" : "false", Field.Store.YES));

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
		return output.trim();
	}


	private static Connection getConnection(){
		
		Connection c = null;
		String DBNAME = "socialoceandb";
		String USER = "socialocean";
		String PASS = "blFDvUic4DL0V3ODkvbK";
		String HOST = "db.dbvis.de";
		String PORT = 5432+"";
		DBNAME = DBNAME+"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
		String connection_str = "jdbc:postgresql://"+HOST+":"+PORT+"/"+DBNAME;
			
		try {
			c = DriverManager.getConnection(connection_str.trim(), USER.trim(), PASS.trim());
		} catch (SQLException e) {
			System.err.println("Could not connect to DB \n"
					+ ""+connection_str);
			e.printStackTrace();
		}	
		
		return c;
	}
}