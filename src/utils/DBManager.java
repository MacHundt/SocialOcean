package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	
	private static Connection newConnection() {
		String DATA = "boston";
		String DBNAME = "masterproject_"+DATA;
		String USER = "postgres";
		String PASS = "postgres";
		int PORT = 5432;
		Connection c = null;
		String connection_str = "jdbc:postgresql://localhost:"+PORT+"/"+DBNAME;
		try {
			c = DriverManager.getConnection(connection_str, USER, PASS);
		} catch (SQLException e) {
			System.err.println("Could not connect to DB \n"
					+ ""+connection_str);
			e.printStackTrace();
		}
		return c;
	}
	
	
	public static Connection getConnection() {
		return newConnection();
		
	}
}
