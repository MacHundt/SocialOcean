package scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import utils.DBManager;

public class AddSentimentScript {
	
//	private static String tweet_table = "bb_tweets";
	private static String tweet_table = "nodexl_my2k_tweets";
	
	private static int fetchsize = 10000;
	private static SentimentClassifier sentClassifier;
	private static int batchcounter = 0;
	static ResultSet rs = null;
	static ArrayList<Tuple<Long, String>> list = null;
	
	private static boolean LOCAL = true; 
	
	public static void main(String[] args) {
		// http://alias-i.com/lingpipe/demos/tutorial/classify/read-me.html
		System.out.print("Load Sentiment Classifier ... ");
		sentClassifier = new SentimentClassifier();
		
		System.out.println(" DONE ");
//		worker();
		
		Connection c = DBManager.getConnection(LOCAL, false);
		String query = "Select tweet_id, tweet_content from "+tweet_table+" where sentiment is null";
		try {
			c.setAutoCommit(false);
			Statement st = c.createStatement();
			st.setFetchSize(fetchsize);
			rs = st.executeQuery(query);
			list = new ArrayList<>();
			int counter = 0;
			while (rs.next()) {
				counter++;
				long id = Long.parseLong(rs.getString(1));
				String text = rs.getString(2);
				if (text == null || text.isEmpty())
					continue;
				
				Tuple<Long, String> tup = new Tuple<>(id, text);
				list.add(tup);
				if ( counter == fetchsize) {
					counter = 0;
					addSentiment(list);
					list.clear();
					if (batchcounter++ == 100) {
						System.out.println("-");
						batchcounter = 0;
					} else
						System.out.print("-");
				}
			}
			addSentiment(list);
			list.clear();
		}  catch (SQLException e) {
			e.printStackTrace();
			
			main(args);
			
		}
		
		System.out.println("\nADD SENTIMENT to table "+tweet_table+" ... COMPLETE");
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
	
	
	private static void addSentiment(ArrayList<Tuple<Long, String>> list) throws SQLException {
		
		int batchsize = 1000;
		// for (Tuple<Long, String> t : list) {
		// t.setB(classifier.getCategory(t.getB()));
		// }

		List<Tuple<Long, String>> collect = list.parallelStream()
				.map(s -> new Tuple<Long, String>(s.getA(), sentClassifier.classify(s.getB())))
				.collect(Collectors.toList());

		Connection c = DBManager.getConnection(LOCAL, false);
		c.setAutoCommit(false);

		Statement st = c.createStatement();
		// Update
		int counter = 0;
		for (Tuple<Long, String> t : collect) {
			String query = "Update " + tweet_table + " set sentiment = '" + t.getB() + "' Where tweet_id = " + t.getA();
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
	
}
