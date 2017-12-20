package scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import scripts.AddSentimentScript.Tuple;
import uk.ac.wlv.sentistrength.SentiStrength;
import utils.DBManager;

public class AddSentiStrength {

	private static String tweet_table = "so_tweets";
//	private static String tweet_table = "nodexl_my2k_tweets";
	
	private static int fetchsize = 3000;
	private static SentiStrength sentiStrength; 
	private static int batchcounter = 0;
	static ResultSet rs = null;
	static ArrayList<Tuple<Long, String>> list = null;
	
	private static boolean LOCAL = false;
	private static boolean RCP = false;
	
	public static void main(String[] args) {
		// http://alias-i.com/lingpipe/demos/tutorial/classify/read-me.html
		System.out.print("Load SentiStrength Classifier ... ");
		sentiStrength =  new SentiStrength();
		String internalPath = "./senti_data/SentStrength_Data_December2015English/";
		String ssthInitialisation[] = {"sentidata", internalPath, "explain"};
		sentiStrength.initialise(ssthInitialisation); //Initialize
		
		System.out.println(" DONE ");
//		worker();
		
		Connection c = DBManager.getConnection(LOCAL, RCP);
		String query = "Select tweet_id, tweet_content from "+tweet_table + " Where positive = 0";
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
					addSentiStrength(list);
					list.clear();
					if (batchcounter++ == 100) {
						System.out.println("-");
						batchcounter = 0;
					} else
						
						System.out.print("-");
				}
			}
			addSentiStrength(list);
			list.clear();
		}  catch (SQLException e) {
			e.printStackTrace();
			
			main(args);
			
		}
		
		System.out.println("\nADD SENTISTRENGTH to table "+tweet_table+" ... COMPLETE");
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
	
	
	
	
	private static void addSentiStrength(ArrayList<Tuple<Long, String>> list) throws SQLException {
		
		int batchsize = 1000;
		List<Tuple<Long, String>> collect = list.parallelStream()
				.map(s -> new Tuple<Long, String>(s.getA(),  sentiStrength.computeSentimentScores(s.getB())))
				.collect(Collectors.toList());


		Connection c = DBManager.getConnection(LOCAL, RCP);
		c.setAutoCommit(false);

		Statement st = c.createStatement();
		// Update
		int counter = 0;
		for (Tuple<Long, String> t : collect) {
			double positive = 1.0;
			double negative = -1.0;
			String[] getScore = t.getB().split(" ");
			positive = Double.parseDouble(getScore[0]);
			negative = Double.parseDouble(getScore[1]);
			String query = "Update " + tweet_table + " set positive = " + positive+ ", negative = "+ negative +" Where tweet_id = " + t.getA();
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
