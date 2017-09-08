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
//	private static String TWEETDATA = "tweetdata";
//	private static String USERS = "users";
	private static String TWEETDATA = "nodexl_my2k_tweets";
	private static String USERS = "nodexl_my2k_users";
	
	private static boolean local = false;
	private static boolean rcp = true;
	
	private static Connection newConnection(boolean m_local, boolean rcp_flag) {
		String DATA = "boston";
		String DBNAME = "masterproject_"+DATA;
		String USER = "postgres";
		String PASS = "postgres";
		String HOST = "localhost";
		String PORT = 5432+"";
		
		local = m_local;
		rcp = rcp_flag;
		
		Connection c = null;
		
		if (!local) {
//			## LOAD Settings File
			
			Properties prop = new Properties();
			if (rcp) {
				URL url = null;
				try {
					url = new URL("platform:/plugin/" + "BostonCase/" + "settings/db_config.properties");

				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				try {
					url = FileLocator.toFileURL(url);
					InputStream input = new FileInputStream(new File(url.getPath()));
					prop.load(input);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
			else {
				try {
				    //load a properties file from class path, inside static method
					FileInputStream ip = new FileInputStream(new File("./settings/db_config.properties"));
					prop.load(ip);
				} 
				catch (IOException ex) {
				    ex.printStackTrace();
				}
			}
			
			HOST = prop.getProperty("host");
			PORT = prop.getProperty("port");
			DBNAME = prop.getProperty("dbname");
			USER = prop.getProperty("username");
			PASS = prop.getProperty("pw");
//			TWEETDATA = prop.getProperty("tweetdata");
//			USERS = prop.getProperty("users");
			// PASS = "\'"+PASS+"\'";
			// ADD SSL=true & ssl Factory
			// @see https://bowerstudios.com/node/739
			DBNAME = DBNAME + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
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
	
	public static void setTweetdataTable(String tablename) {
		TWEETDATA = tablename;
	}
	
	
	public static String getUserTable( ) {
		return USERS;
	}
	
	public static void setUserTable( String usertable ) {
		USERS = usertable;
	}
	
	
	/**
	 * Get DB-Connection
	 * @param local local database
	 * @param rcp 	load properties file in rcp context
	 * @return
	 */
	public static Connection getConnection(boolean local, boolean rcp) {
		return newConnection(local, rcp);
		
	}
	
	/**
	 * Get Connection with default: 
	 * local = true:  localhost database
	 * rcp = true: within the rcp environment
	 * @return
	 */
	public static Connection getConnection() {
		return newConnection(local, rcp);
		
	}
}
