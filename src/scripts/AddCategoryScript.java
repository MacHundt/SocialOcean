package scripts;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AddCategoryScript {
	
	private static String tweet_table = "bb_tweets";
	private static int fetchsize = 1000;

	public static void main(String[] args) {
		// http://alias-i.com/lingpipe/demos/tutorial/classify/read-me.html
		System.out.println("LOAD Model Newsgroup Model ... ");
		TopicClassification classifier = new TopicClassification();
		try {
			classifier.trainclassifier();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("LOAD LingPipe 20 Newsgroup Model >>> DONE \n");
		System.out.print("Connect to DB >>> ");
		Connection c = getConnection();
		System.out.println("DONE \n");
		
		String query = "Select tweet_id, tweet_content from "+tweet_table;
		ResultSet rs = null;
		try {
			c.setAutoCommit(false);
			Statement st = c.createStatement();
			st.setFetchSize(fetchsize);
			rs = st.executeQuery(query);
			addCategories(rs, classifier);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void addCategories(ResultSet rs, TopicClassification classifier) throws SQLException {
		
		Connection c = getConnection();
		c.setAutoCommit(false);
		
		Statement st = c.createStatement();
		
//		PreparedStatement preStmt = con2.prepareStatement("Update "+tweet_table+" set category = ? Where tweet_id = ?");
		
		long counter = 0;
		int batchcounter = 0;
		while (rs.next()) {
			long id = Long.parseLong(rs.getString(1));
			String text = rs.getString(2);
			// Classify Text
			String cat = classifier.getCategory(text);
			String query = "Update "+tweet_table+" set category = '"+cat+"' Where tweet_id = "+id;
			// batch Update
			st.addBatch(query);
			counter++;
			
			if (counter % fetchsize == 0) {
				st.executeBatch();
				c.commit();
				batchcounter++;
				System.out.println("Added >>> "+ batchcounter*fetchsize);
			}
			
//			preStmt.setString(1, cat);
//			preStmt.setLong(2, id);
//			preStmt.executeUpdate();
			
		}
		
		c.close();
		
		
		
	}

	private static Connection getConnection() {
		
		Connection c = null;
		String host = "db.dbvis.de";
		String port = "5432";
		String dbname = "socialoceandb";
		String username = "socialocean";
		String pw = "blFDvUic4DL0V3ODkvbK";
		
		String connection_str = "jdbc:postgresql://"+host+":"+port+"/"+dbname+"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
		try {
			c = DriverManager.getConnection(connection_str.trim(), username.trim(), pw.trim());
		} catch (SQLException e) {
			System.err.println("Could not connect to DB \n"
					+ ""+connection_str);
			e.printStackTrace();
		}
		
		return c;
	}
		
}
		
