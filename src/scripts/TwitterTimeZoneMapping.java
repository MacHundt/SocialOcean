package scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import utils.DBManager;

public class TwitterTimeZoneMapping {

	private static String table = "users";
	// private static String tweet_table = "nodexl_ohsen_tweets";

	private static int fetchsize = 1000;
	private static int batchcounter = 0;
	private static HashMap<String, String> TimezoneMapping = new HashMap<>();

	private static boolean LOCAL = false;
	private static boolean RCP = false;

	public static void main(String[] args) {
		System.out.println("Map Twitter Timezones ... ");

		initMapping();

		try {
			Connection c = DBManager.getConnection(LOCAL, RCP);
			Connection updateCon = DBManager.getConnection(LOCAL, RCP);

			// Go through mapping:
			for (String twTimezone : TimezoneMapping.keySet()) {
				System.out.println("\n" + twTimezone + " => " + TimezoneMapping.get(twTimezone));
				
				// E --> Escape the string within ' '
				twTimezone = twTimezone.replace("'", "\\'");
				String query = "Select user_id, user_timezone from " + table + " where user_timezone = "
						+ "E'" + twTimezone	+ "'";
				String updateQuery = "Update " + table + " set user_timezone = '";
				c.setAutoCommit(false);
				Statement st = c.createStatement();
				st.setFetchSize(fetchsize);

				updateCon.setAutoCommit(false);
				Statement update = updateCon.createStatement();
				ResultSet rs = null;
				try {
					rs = st.executeQuery(query);
				} catch (Exception e) {
					System.out.println("FEHLER >>>>"+query);
					
					continue;
				}
				int counter = 0;
				while (rs.next()) {
					counter++;
					long id = Long.parseLong(rs.getString(1));
					String timezone = TimezoneMapping.get(rs.getString(2)); // get mapping
					if (timezone == null) {
						continue;
					}
					String uQuery = updateQuery + timezone + "' where user_id = " + id;
					update.addBatch(uQuery);
					if (counter == fetchsize) {
						counter = 0;
						update.executeBatch();
						updateCon.commit();
						update.clearBatch();
						if (batchcounter++ == 100) {
							System.out.println("-");
							batchcounter = 0;
						} else
							System.out.print("-");
					}
				}
				// add last
				st.executeBatch();
				updateCon.commit();
				update.clearBatch();
			}
			c.close();
			updateCon.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// restart
			main(args);
		}
		
		System.out.println("\nDONE");
	}

	/**
	 * The mapping is taken from:
	 * http://api.rubyonrails.org/classes/ActiveSupport/TimeZone.html
	 */
	private static void initMapping() {

		TimezoneMapping.put("International Date Line West", "Pacific/Midway");
		TimezoneMapping.put("Midway Island", "Pacific/Midway");
		TimezoneMapping.put("American Samoa", "Pacific/Pago_Pago");
		TimezoneMapping.put("Hawaii", "Pacific/Honolulu");
		TimezoneMapping.put("Alaska", "America/Juneau");
		TimezoneMapping.put("Pacific Time (US & Canada)", "America/Los_Angeles");
		TimezoneMapping.put("Tijuana", "America/Tijuana");
		TimezoneMapping.put("Mountain Time (US & Canada)", "America/Denver");
		// TimezoneMapping.put("Arizona","America/Phoenix");
		TimezoneMapping.put("Chihuahua", "America/Chihuahua");
		TimezoneMapping.put("Mazatlan", "America/Mazatlan");
		TimezoneMapping.put("Central Time (US & Canada)", "America/Chicago");
		// TimezoneMapping.put("Saskatchewan","America/Regina");
		TimezoneMapping.put("Guadalajara", "America/Mexico_City");
		TimezoneMapping.put("Mexico City", "America/Mexico_City");
		// TimezoneMapping.put("Monterrey","America/Monterrey" );
		TimezoneMapping.put("Central America", "America/Guatemala");
		TimezoneMapping.put("Eastern Time (US & Canada)", "America/New_York");
		// TimezoneMapping.put("Indiana (East)","America/Indiana/Indianapolis" );
		TimezoneMapping.put("Bogota", "America/Bogota");
		TimezoneMapping.put("Lima", "America/Lima");
		TimezoneMapping.put("Quito", "America/Lima");
		TimezoneMapping.put("Atlantic Time (Canada)", "America/Halifax");
		TimezoneMapping.put("Caracas", "America/Caracas");
		TimezoneMapping.put("La Paz", "America/La_Paz");
		TimezoneMapping.put("Santiago", "America/Santiago");
		TimezoneMapping.put("Newfoundland", "America/St_Johns");
		TimezoneMapping.put("Brasilia", "America/Sao_Paulo");
		TimezoneMapping.put("Buenos Aires", "America/Argentina/Buenos_Aires");
		TimezoneMapping.put("Montevideo", "America/Montevideo");
		// TimezoneMapping.put("Georgetown","America/Guyana" );
		TimezoneMapping.put("Greenland", "America/Godthab");
		// TimezoneMapping.put( "Mid-Atlantic","Atlantic/South_Georgia" );
		TimezoneMapping.put("Azores", "Atlantic/Azores");
		TimezoneMapping.put("Cape Verde Is.", "Atlantic/Cape_Verde");
		TimezoneMapping.put("Dublin", "Europe/Dublin");
		TimezoneMapping.put("Edinburgh", "Europe/London");
		TimezoneMapping.put("Lisbon", "Europe/Lisbon");
		TimezoneMapping.put("London", "Europe/London");
		// TimezoneMapping.put( "Casablanca","Africa/Casablanca");
		TimezoneMapping.put("Monrovia", "Africa/Monrovia");
		TimezoneMapping.put("UTC", "Etc/UTC");
		// TimezoneMapping.put("Belgrade","Europe/Belgrade" );
		TimezoneMapping.put("Bratislava", "Europe/Bratislava");
		TimezoneMapping.put("Budapest", "Europe/Budapest");
		TimezoneMapping.put("Ljubljana", "Europe/Ljubljana");
		TimezoneMapping.put("Prague", "Europe/Prague");
		TimezoneMapping.put("Sarajevo", "Europe/Sarajevo");
		TimezoneMapping.put("Skopje", "Europe/Skopje");
		// TimezoneMapping.put("Warsaw","Europe/Warsaw" );
		TimezoneMapping.put("Zagreb", "Europe/Zagreb");
		TimezoneMapping.put("Brussels", "Europe/Brussels");
		// TimezoneMapping.put("Copenhagen","Europe/Copenhagen");
		TimezoneMapping.put("Madrid", "Europe/Madrid");
		TimezoneMapping.put("Paris", "Europe/Paris");
		TimezoneMapping.put("Amsterdam", "Europe/Amsterdam");
		TimezoneMapping.put("Berlin", "Europe/Berlin");
		TimezoneMapping.put("Bern", "Europe/Zurich");
		TimezoneMapping.put("Zurich", "Europe/Zurich");
		TimezoneMapping.put("Rome", "Europe/Rome");
		TimezoneMapping.put("Stockholm", "Europe/Stockholm");
		TimezoneMapping.put("Vienna", "Europe/Vienna");
		// TimezoneMapping.put("West Central Africa","Africa/Algiers");
		// TimezoneMapping.put("Bucharest","Europe/Bucharest");
		TimezoneMapping.put("Cairo", "Africa/Cairo");
		TimezoneMapping.put("Helsinki", "Europe/Helsinki");
		// TimezoneMapping.put("Kyiv","Europe/Kiev");
		TimezoneMapping.put("Riga", "Europe/Riga");
		TimezoneMapping.put("Sofia", "Europe/Sofia");
		TimezoneMapping.put("Tallinn", "Europe/Tallinn");
		TimezoneMapping.put("Vilnius", "Europe/Vilnius");
		// TimezoneMapping.put("Athens","Europe/Athens");
		// TimezoneMapping.put("Istanbul","Europe/Istanbul");
		TimezoneMapping.put("Minsk", "Europe/Minsk");
		TimezoneMapping.put("Jerusalem", "Asia/Jerusalem");
		TimezoneMapping.put("Harare", "Africa/Harare");
		TimezoneMapping.put("Pretoria", "Africa/Johannesburg");
		TimezoneMapping.put("Kaliningrad", "Europe/Kaliningrad");
		TimezoneMapping.put("Moscow", "Europe/Moscow");
		// TimezoneMapping.put("St. Petersburg","Europe/Moscow");
		TimezoneMapping.put("Volgograd", "Europe/Volgograd");
		// TimezoneMapping.put("Samara","Europe/Samara");
		TimezoneMapping.put("Kuwait", "Asia/Kuwait");
		TimezoneMapping.put("Riyadh", "Asia/Riyadh");
		TimezoneMapping.put("Nairobi", "Africa/Nairobi");
		TimezoneMapping.put("Baghdad", "Asia/Baghdad");
		TimezoneMapping.put("Tehran", "Asia/Tehran");
		// TimezoneMapping.put("Abu Dhabi","Asia/Muscat");
		TimezoneMapping.put("Muscat", "Asia/Muscat");
		// TimezoneMapping.put("Baku","Asia/Baku");
		TimezoneMapping.put("Tbilisi", "Asia/Tbilisi");
		TimezoneMapping.put("Yerevan", "Asia/Yerevan");
		// TimezoneMapping.put("Kabul","Asia/Kabul");
		TimezoneMapping.put("Ekaterinburg", "Asia/Yekaterinburg");
		TimezoneMapping.put("Islamabad", "Asia/Karachi");
		TimezoneMapping.put("Karachi", "Asia/Karachi");
		// TimezoneMapping.put("Tashkent","Asia/Tashkent");
		// TimezoneMapping.put("Chennai","Asia/Kolkata");
		TimezoneMapping.put("Kolkata", "Asia/Kolkata");
		TimezoneMapping.put("Mumbai", "Asia/Kolkata");
		TimezoneMapping.put("New Delhi", "Asia/Kolkata");
		// TimezoneMapping.put("Kathmandu","Asia/Kathmandu");
		TimezoneMapping.put("Astana", "Asia/Dhaka");
		TimezoneMapping.put("Dhaka", "Asia/Dhaka");
		TimezoneMapping.put("Sri Jayawardenepura", "Asia/Colombo");
		TimezoneMapping.put("Almaty", "Asia/Almaty");
		TimezoneMapping.put("Novosibirsk", "Asia/Novosibirsk");
		TimezoneMapping.put("Rangoon", "Asia/Rangoon");
		// TimezoneMapping.put("Bangkok","Asia/Bangkok");
		TimezoneMapping.put("Hanoi", "Asia/Bangkok");
		TimezoneMapping.put("Jakarta", "Asia/Jakarta");
		TimezoneMapping.put("Krasnoyarsk", "Asia/Krasnoyarsk");
		TimezoneMapping.put("Beijing", "Asia/Shanghai");
		TimezoneMapping.put("Chongqing", "Asia/Chongqing");
		TimezoneMapping.put("Hong Kong", "Asia/Hong_Kong");
		TimezoneMapping.put("Urumqi", "Asia/Urumqi");
		TimezoneMapping.put("Kuala Lumpur", "Asia/Kuala_Lumpur");
		TimezoneMapping.put("Singapore", "Asia/Singapore");
		TimezoneMapping.put("Taipei", "Asia/Taipei");
		TimezoneMapping.put("Perth", "Australia/Perth");
		TimezoneMapping.put("Irkutsk", "Asia/Irkutsk");
		TimezoneMapping.put("Ulaanbaatar", "Asia/Ulaanbaatar");
		TimezoneMapping.put("Seoul", "Asia/Seoul");
		TimezoneMapping.put("Osaka", "Asia/Tokyo");
		TimezoneMapping.put("Sapporo", "Asia/Tokyo");
		TimezoneMapping.put("Tokyo", "Asia/Tokyo");
		TimezoneMapping.put("Yakutsk", "Asia/Yakutsk");
		TimezoneMapping.put("Darwin", "Australia/Darwin");
		TimezoneMapping.put("Adelaide", "Australia/Adelaide");
		TimezoneMapping.put("Canberra", "Australia/Melbourne");
		TimezoneMapping.put("Melbourne", "Australia/Melbourne");
		TimezoneMapping.put("Sydney", "Australia/Sydney");
		TimezoneMapping.put("Brisbane", "Australia/Brisbane");
		TimezoneMapping.put("Hobart", "Australia/Hobart");
		TimezoneMapping.put("Vladivostok", "Asia/Vladivostok");
		TimezoneMapping.put("Guam", "Pacific/Guam");
		TimezoneMapping.put("Port Moresby", "Pacific/Port_Moresby");
		TimezoneMapping.put("Magadan", "Asia/Magadan");
		TimezoneMapping.put("Srednekolymsk", "Asia/Srednekolymsk");
		TimezoneMapping.put("Solomon Is.", "Pacific/Guadalcanal");
		// TimezoneMapping.put("New Caledonia","Pacific/Noumea");
		TimezoneMapping.put("Fiji", "Pacific/Fiji");
		// TimezoneMapping.put("Kamchatka","Asia/Kamchatka");
		TimezoneMapping.put("Marshall Is.", "Pacific/Majuro");
		TimezoneMapping.put("Auckland", "Pacific/Auckland");
		// TimezoneMapping.put("Wellington","Pacific/Auckland");
		TimezoneMapping.put("Nuku'alofa", "Pacific/Tongatapu");
		TimezoneMapping.put("Tokelau Is.", "Pacific/Fakaofo");
		TimezoneMapping.put("Chatham Is.", "Pacific/Chatham");
		TimezoneMapping.put("Samoa", "Pacific/Apia");

	}

}
