package scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import utils.DBManager;

public class GenderToUserReferencer {
	
	private static String user_names = "user_names";
	private static String user_table = "users";
	private static int fetchsize = 10000;
	static ResultSet rs = null;
	static ArrayList<Long> list = null;
	
	private static boolean LOCAL = false;
	private static boolean RCP = false;
	private static int batchcounter = 0;
	

	public static void main(String[] args) throws SQLException {
		System.out.println("GENDERize ...");

		Connection c = DBManager.getConnection(LOCAL, RCP);
		String query = "Select user_id from " + user_table + " where gender = 'unknown'";

		try {
			c.setAutoCommit(false);
			Statement st = c.createStatement();
			
			st.setFetchSize(fetchsize);
			rs = st.executeQuery(query);
			list = new ArrayList<>();
			int counter = 0;
			while (rs.next()) {
				counter++;
				long uid = Long.parseLong(rs.getString(1));
				list.add(uid);
				if (counter == fetchsize) {
					
					counter = 0;
					genderize(list);
					list.clear();
					if (batchcounter++ == 100) {
						System.out.println("-");
						batchcounter = 0;
					} else
						System.out.print("-");
				}
			}
			// add last
			genderize(list);
			list.clear();
			st.close();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
			list.clear();
			c.close();
			main(args);

		}
		System.out.println(">>> DONE");
	}


	private static void genderize(ArrayList<Long> list) throws SQLException {
		int batchsize = 1000;
		Connection c = DBManager.getConnection(LOCAL, RCP);
		c.setAutoCommit(false);
		
		Statement st = c.createStatement();
		int counter = 0;
		int fetchcounter = list.size();
		
		Connection ct = DBManager.getConnection(LOCAL, RCP);
		Statement stt = ct.createStatement();
		for (long uid : list) {
			fetchcounter--;
			String updateQuery = get_gender_user_names(uid, stt);
			if (!updateQuery.equals("NaV")) {
				st.addBatch(updateQuery);
				counter++;
			}
			
			if (counter == batchsize) {
				st.executeBatch();
				c.commit();
				counter = 0;
			}
		}
		st.executeBatch();
		c.commit();
		stt.close();
		ct.close();
		st.close();
		c.close();
		
	}


	private static String get_gender_user_names(long uid, Statement st) {
		String query = "Select user_name, first_name, gender from "+user_names+" as u where u.user_id= "+uid+";";
		
		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
			while (rs.next()) {
				String user_name = rs.getString(1);
				String first_name = rs.getString(2);
				String gender = rs.getString(3);
				// return update String:
				String updateString = "Update "+user_table+" set user_name = '"+user_name+"', "
						+ "first_name = '"+first_name+"', gender = '"+gender+"' where user_id = "+uid;
				return updateString;
					
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return "NaV";
	}

}
