package scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import utils.DBManager;

public class ExtractHashtagsScript {

	private static String tweet_table = "so_tweets";
	
	private static int fetchsize = 5000;
	private static int batchcounter = 0;
	static ResultSet rs = null;
	static ArrayList<Tuple<Long, String>> list = null;
	
	private static boolean LOCAL = false;
	private static boolean RCP = false;
	
	public static void main(String[] args) {
		System.out.print("Load extract hashtags ... ");
		
		Connection c = DBManager.getConnection(LOCAL, RCP);
//		String query = "Select tweet_id, tweet_content from "+tweet_table + " Where tweet_hashtags is null";
		String query = "Select tweet_id, tweet_content from "+tweet_table ;
		try {
			c.setAutoCommit(false);
			Statement st = c.createStatement();
			st.setFetchSize(fetchsize);
			rs = st.executeQuery(query);
			list = new ArrayList<>();
			int counter = 0;
			while (rs.next()) {
				long id = Long.parseLong(rs.getString(1));
				String text = rs.getString(2);
				if (text == null || text.isEmpty())
					continue;
				
				if (!text.contains("#")) {
					continue;
				}
				
				counter++;
				Tuple<Long, String> tup = new Tuple<>(id, text);
				
				list.add(tup);
				if ( counter == fetchsize) {
					counter = 0;
					extractHashtags(list);
					list.clear();
					if (batchcounter++ == 100) {
						System.out.println("-");
						batchcounter = 0;
					} else
						
						System.out.print("-");
				}
			}
			extractHashtags(list);
			list.clear();
		}  catch (SQLException e) {
			e.printStackTrace();
			
			main(args);
			
		}
		
		System.out.println("\nADD EXTRACT Hashtags to table "+tweet_table+" ... COMPLETE");
	}
	
	public static class Tuple<A, B> {
		
		private A a;
		private B b;

		public Tuple(A a, B b) {
			this.a = a;
			this.b = b;
		}

		public A getA() {
			return a;
		}

		public void setA(A a) {
			this.a = a;
		}

		public B getB() {
			return b;
		}

		public void setB(B b) {
			this.b = b;
		}
	}
	
	
	
	private static void extractHashtags(ArrayList<Tuple<Long, String>> list) throws SQLException {
		
		int batchsize = 100;
		List<Tuple<Long, String>> collect = list.parallelStream()
				.map(s -> new Tuple<Long, String>(s.getA(),  getTagsFromTweets(s.getB())))
				.collect(Collectors.toList());


		Connection c = DBManager.getConnection(LOCAL, RCP);
		c.setAutoCommit(false);

		Statement st = c.createStatement();
		// Update
		int counter = 0;
		for (Tuple<Long, String> t : collect) {
			String hashtags = t.getB();
			String query = "Update " + tweet_table + " set tweet_hashtags = " + hashtags + " Where tweet_id = " + t.getA();
			// batch Update
			st.addBatch(query);
			counter++;
			if (counter == batchsize) {
				st.executeBatch();
				c.commit();
				counter = 0;
			}
		}
		st.executeBatch();
		c.commit();
		
		st.close();
		c.close();
	}
	
	
	private static String getTagsFromTweets(String text_content) {
		String output = "'{";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("#")) {
				token = token.replaceAll("[,.]", "");
				
				if (token.contains("'"))
					token = token.replace("'", "\'");
				
				
				output += token.substring(1) + ",";
			}
		}
		// has # --> remove last ,
		if (output.length() > 2) {
			output = output.substring(0, output.lastIndexOf(","));
		}
		output += "}'";
		return output.trim();
	}
}
