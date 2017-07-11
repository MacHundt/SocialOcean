package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;

public class DBManager {
	
//	private static boolean local = true;
	private static String TWEETDATA = "tweetdata";
	private static String USERS = "users";
	
	private static boolean local = true;
	
	private static Connection newConnection(boolean m_local) {
		String DATA = "boston";
		String DBNAME = "masterproject_"+DATA;
		String USER = "postgres";
		String PASS = "postgres";
		String HOST = "localhost";
		String PORT = 5432+"";
		
		local = m_local;
		
		Connection c = null;
		
		if (!local) {
//			## LOAD Settings File
			Properties prop = new Properties();
			URL url = null;
			try {
			  url = new URL("platform:/plugin/"
			    + "BostonCase/"
			    + "settings/db_config.properties");

			    } catch (MalformedURLException e1) {
			      e1.printStackTrace();
			}
			try {
				url = FileLocator.toFileURL(url);
				InputStream input = new FileInputStream(new File(url.getPath()));
				prop.load(input);
				
				HOST =  prop.getProperty("host");
				PORT =  prop.getProperty("port");
				DBNAME = prop.getProperty("dbname");
				USER =  prop.getProperty("username");
				PASS = prop.getProperty("pw");
				TWEETDATA = prop.getProperty("tweetdata");
				USERS = prop.getProperty("users");
//				PASS = "\'"+PASS+"\'";
				// ADD SSL=true & ssl Factory
				// @see https://bowerstudios.com/node/739
				DBNAME = DBNAME+"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
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
	
	
	public static String getTweetdataTable() {
		return TWEETDATA;
	}
	
	
	public static String getUserTable( ) {
		return USERS;
	}
	
	
	public static Connection getConnection(boolean local) {
		return newConnection(local);
		
	}
	
	public static Connection getConnection() {
		return newConnection(local);
		
	}
}
