package scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.dom4j.rule.Pattern;

import com.vividsolutions.jts.geom.Coordinate;

import utils.DBManager;

public class Geocoding {

	private static String user_table = "so_users";
	private static int fetchsize = 10000;
	static ResultSet rs = null;
	static ArrayList<Entry<ArrayList<String>>> list = null;
	static ArrayList<String> fields = null;

	private static boolean LOCAL = false;
	private static boolean RCP = false;

	private static int batchcounter = 0;

	private static java.util.regex.Pattern letters = java.util.regex.Pattern.compile("\\w+\\.?");
	// private static java.util.regex.Pattern not_ascii_letters =
	// java.util.regex.Pattern.compile("[^a-zA-Z ]");
	private static java.util.regex.Pattern remover = java.util.regex.Pattern
			.compile("[0-9#!?§.$%&/()=¡¶¢|\\{\\}≠¿@¥≈ç√∫~µ∞,']");
	private static java.util.regex.Pattern coordinates = java.util.regex.Pattern
			.compile("[-]?(\\d+)[.](\\d+)[ ,]+[-]?(\\d+)[.](\\d+)");

	public static void main(String[] args) {

		System.out.println("GEOCODING ...");

		Connection c = DBManager.getConnection(LOCAL, RCP);
		// String query = "Select user_id, user_location, user_timezone, geocoding_type
		// from " + user_table + " where geom is null;";
//		String query = "Select user_id, user_location, user_timezone, geocoding_type from " + user_table
//				+ " where geocoding_type > 7 and geocoding_type < 11";
//		 String query = "Select user_id, user_location, user_timezone, geocoding_type from " + user_table + " where geocoding_type = 4";
		 String query = "Select user_id, user_location, user_timezone, geocoding_type from " + user_table;

		
		try {
			c.setAutoCommit(false);
			Statement st = c.createStatement();
			st.setFetchSize(fetchsize);
			rs = st.executeQuery(query);
			list = new ArrayList<>();
			int counter = 0;
			while (rs.next()) {
				long uid = Long.parseLong(rs.getString(1));
				String location = rs.getString(2);
				String timezone = rs.getString(3);
				int geoType = rs.getInt(4);

//				// GT 10 -- Stay Default. No geocoding possible --> update to 11
//				if ((location == null || location.trim().isEmpty()) && timezone.equals("null")) {
//					// if (counter == fetchsize) {
//					// counter = 0;
//					// }
//					continue;
//				}

				counter++;
				fields = new ArrayList<>();
				fields.add(uid + "");
				fields.add(location);
				fields.add(timezone);
				fields.add("" + geoType);
				Entry<ArrayList<String>> tup = new Entry<ArrayList<String>>(fields);
				// Tuple<Long, ArrayList<String>> tup = new Tuple<Long, ArrayList<String>>(uid,
				// geoFields);
				list.add(tup);
				if (counter == fetchsize) {
					counter = 0;
					geocode(list);
					list.clear();
					if (batchcounter++ == 100) {
						System.out.println("-");
						batchcounter = 0;
					} else
						System.out.print("-");
				}
			}
			// add last
			geocode(list);
			list.clear();
			st.close();
			c.close();
		} catch (SQLException e) {
			// e.printStackTrace();
			main(args);

		}
		System.out.println(">>> DONE");
	}

	/**
	 * Geocode the batched tweets
	 * 
	 * @param list
	 * @throws SQLException
	 */
	private static void geocode(ArrayList<Entry<ArrayList<String>>> list) throws SQLException {

		if (list.isEmpty())
			return;

		// System.out.println ("abc.".matches(letters.pattern()));
		int batchsize = 1000;
		Connection c = DBManager.getConnection(LOCAL, RCP);
		c.setAutoCommit(false);

		Statement st = c.createStatement();

		Connection updateC = DBManager.getConnection(LOCAL, RCP);

		// GEOCODE
		int fetchcounter = list.size();
		int counter = 0;
		for (Entry<ArrayList<String>> e : list) {
			fetchcounter--;
			long uid = Long.parseLong(e.getA().get(0));
			String loc = e.getA().get(1);
			String tz = e.getA().get(2);
			int geoType = Integer.parseInt(e.getA().get(3));
			String updateQuery = "";
			geoType = (geoType == 0) ? 11 : geoType;
			
			// ############ Geocoding Type 11 #############
			if ((loc == null || loc.trim().isEmpty()) && tz.equals("null")) {
				updateQuery =  "update "+user_table+" set geocoding_type = 11 where user_id = "+uid;
				st.addBatch(updateQuery);
				counter++;
			}

			// ############ Geocoding Type 1 #############

			if (geoType > 1) {
				updateQuery = geocode1(uid, loc, tz);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 1;
				}
			}

			// ############ Geocoding Type 2 #############

			if (geoType > 2) {
				updateQuery = geocode2(uid, loc, tz);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 2;
				}
			}

			// ############ Geocoding Type 3 #############

			if (geoType > 3) {
				updateQuery = geocode3(uid, loc, tz, updateC);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 3;
				}
			}

			// ############ Geocoding Type 4 #############

			if (geoType > 4) {
				updateQuery = geocode4(uid, tz, updateC);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 4;
				}
			}

			// ############ Geocoding Type 5 #############

			if (geoType > 5) {
				updateQuery = geocode5(uid, loc, tz, updateC);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 5;
				}
			}

			loc = loc.replaceAll(",", ", ").toLowerCase();
			loc = loc.replaceAll(remover.pattern(), "").trim();

			// ############ Geocoding Type 6 #############

			if (geoType > 6) {
				updateQuery = geocode6(uid, loc, updateC);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 6;
				}
			}

			// ############ Geocoding Type 7 #############

			if (geoType > 7) {
				updateQuery = geocode7(uid, loc, updateC);
				if (!updateQuery.equals("NaV")) {
					st.addBatch(updateQuery);
					counter++;
					geoType = 7;
				}
			}
			
			if (geoType > 7) {
				updateQuery = "update "+user_table+" set geocoding_type = 11 where user_id = "+uid;
				st.addBatch(updateQuery);
				counter++;
			}

			if (counter == batchsize) {
				st.executeBatch();
				c.commit();
				counter = 0;
			}

		}
		updateC.close();
		st.executeBatch();
		c.commit();
		st.close();
		c.close();

	}

	private static String geocode1(long uid, String loc, String tz) throws SQLException {
		// loc has coordinates:
		if (loc.matches(coordinates.pattern())) {
			loc = loc.replaceAll("[a-zA-Z?!\\'\\\":ï¿½TÜÄÖüäö]*", "").trim();
			double lat = Double.parseDouble(loc.substring(0, loc.indexOf(',')));
			double lon = Double.parseDouble(loc.substring(loc.indexOf(',') + 1, loc.length()));
			// get the coordinates, test if in timezoneshape
			if (is_in_timezoneshape(lat, lon, tz, true)) {
				String query = "Update " + user_table + " set geocoding_type = 1, " + "geom = St_setsrid(St_Point("
						+ lon + "," + lat + "), 4326), " + "longitude = " + lon + ", " + "latitude = " + lat
						+ " where user_id = " + uid + ";";
				return query;
			}
		}

		return "NaV";
	}

	private static String geocode2(long uid, String loc, String tz) throws SQLException {
		// loc has coordinates:
		if (loc.matches(coordinates.pattern())) {
			loc = loc.replaceAll("[a-zA-Z?!\\'\\\":ï¿½TÜÄÖüäö]*", "").trim();
			double lat = Double.parseDouble(loc.substring(0, loc.indexOf(',')));
			double lon = Double.parseDouble(loc.substring(loc.indexOf(',') + 1, loc.length()));
			// get the coordinates, test if in timezoneshape
			if (is_in_timezoneshape(lat, lon, tz, false)) {
				String query = "Update " + user_table + " set geocoding_type = 2, " + "geom = St_setsrid(St_Point("
						+ lon + "," + lat + "), 4326), " + "longitude = " + lon + ", " + "latitude = " + lat
						+ " where user_id = " + uid + ";";
				return query;
			}
		}

		return "NaV";
	}

	/**
	 * GT 3 Test Geocoding_type5: user_location is a city and user_timezone matches
	 * the corresponding city timezone
	 * 
	 * @param uid
	 * @param loc
	 * @param tz
	 * @return The Update query String
	 * @throws SQLException 
	 */
	private static String geocode3(long uid, String loc, String tz, Connection c) throws SQLException {
		if (loc.matches(letters.pattern()) && !tz.equals("null")) {
			String location = loc.toLowerCase();
			location = location.substring(0, 1).toUpperCase() + location.substring(1);
			Coordinate cityCoor = get_is_city_most_populated(location, tz, true, c);
			if (cityCoor != null) {
				String query = "Update " + user_table + " set geocoding_type = 3, " + "geom = St_setsrid(St_Point("
						+ cityCoor.y + "," + cityCoor.x + "), 4326), " + "longitude = " + cityCoor.y + ", "
						+ "latitude = " + cityCoor.x + " where user_id = " + uid + ";";
				return query;
			}
		}
		return "NaV";
	}

	/**
	 * GT 4 Test Geocoding_type4: user_timezone mappes to valid timezone
	 * 
	 * @param uid
	 * @param loc
	 * @param tz
	 * @return The Update query String
	 * @throws SQLException 
	 */
	private static String geocode4(long uid, String tz, Connection c) throws SQLException {
		if (!tz.equals("null")) {
			Coordinate cityCoor = get_is_timezone(tz, c);
			if (cityCoor != null) {
				String query = "Update " + user_table + " set geocoding_type = 4, " + "geom = St_setsrid(St_Point("
						+ cityCoor.y + "," + cityCoor.x + "), 4326), " + "longitude = " + cityCoor.y + ", "
						+ "latitude = " + cityCoor.x + " where user_id = " + uid + ";";
				return query;
			}
		}
		return "NaV";
	}

	/**
	 * GT 5 Test Geocoding_type5: user_location is a city
	 * 
	 * @param uid
	 * @param loc
	 * @param tz
	 * @return The Update query String
	 * @throws SQLException 
	 */
	private static String geocode5(long uid, String loc, String tz, Connection c) throws SQLException {

		if (loc.matches(letters.pattern())) {
			String location = loc.toLowerCase();
			location = location.substring(0, 1).toUpperCase() + location.substring(1);
			Coordinate cityCoor = get_is_city_most_populated(location, tz, false, c);
			if (cityCoor != null) {
				String query = "Update " + user_table + " set geocoding_type = 5, " + "geom = St_setsrid(St_Point("
						+ cityCoor.y + "," + cityCoor.x + "), 4326), " + "longitude = " + cityCoor.y + ", "
						+ "latitude = " + cityCoor.x + " where user_id = " + uid + ";";
				return query;
			}
		}
		return "NaV";
	}

	/**
	 * GT 6 Test Geocoding_type6: user_location is like a cityname Take the longest
	 * string match and the highest population
	 * 
	 * @param uid
	 * @param c
	 * @param loc
	 * @return The Update query String
	 * @throws SQLException 
	 */
	private static String geocode6(long uid, String location, Connection c) throws SQLException {
		// String location = loc.toLowerCase();
		// location = location.replaceAll(",", ", ");
		// location = location.replaceAll(remover.pattern(), "");

		if (!location.isEmpty()) {
			location = location.substring(0, 1).toUpperCase() + location.substring(1);
			// contains " " Capitalize all words
			if (location.contains(" ")) {
				String capLocation = "";
				for (String s : location.split(" ")) {
					if (s.length() > 2)
						capLocation += s.substring(0, 1).toUpperCase() + s.substring(1) + " ";
					else
						capLocation += s.toUpperCase();
				}
				location = capLocation.trim();
			}

			Coordinate cityCoor = get_like_city(location, c);
			if (cityCoor != null) {
				String query = "Update " + user_table + " set geocoding_type = 6, " + "geom = St_setsrid(St_Point("
						+ cityCoor.y + "," + cityCoor.x + "), 4326), " + "longitude = " + cityCoor.y + ", "
						+ "latitude = " + cityCoor.x + " where user_id = " + uid + ";";
				return query;
			}
		}
		return "NaV";
	}

	/**
	 * GT 7 Test Geocoding_type7: user_location is like a timezone Take the longest
	 * string match and the highest population
	 * 
	 * @param uid
	 * @param loc
	 * @return The Update query String
	 * @throws SQLException 
	 */
	private static String geocode7(long uid, String location, Connection c) throws SQLException {
		// String location = loc.toLowerCase();
		// location = location.replaceAll(",", ", ");
		// location = location.replaceAll(remover.pattern(), "");

		if (!location.isEmpty()) {
			location = location.substring(0, 1).toUpperCase() + location.substring(1);
			// contains " " Capitalize all words
			if (location.contains(" ")) {
				String capLocation = "";
				for (String s : location.split(" ")) {
					if (s.length() > 2)
						capLocation += s.substring(0, 1).toUpperCase() + s.substring(1) + " ";
					else
						capLocation += s.toUpperCase();
				}
				location = capLocation.trim();
			}

			Coordinate cityCoor = get_like_Timezone(location, c);
			if (cityCoor != null) {
				String query = "Update " + user_table + " set geocoding_type = 6, " + "geom = St_setsrid(St_Point("
						+ cityCoor.y + "," + cityCoor.x + "), 4326), " + "longitude = " + cityCoor.y + ", "
						+ "latitude = " + cityCoor.x + " where user_id = " + uid + ";";
				return query;
			}
		}
		return "NaV";
	}

	/**
	 * Helper method to find out if user_location is LIKE a country of a timezone
	 * Order by longest string match
	 * 
	 * @param loc
	 * @return the coordinates of the city
	 * @throws SQLException 
	 */
	private static Coordinate get_like_Timezone(String loc, Connection c) throws SQLException {

		if (loc.length() > 3) {
			// Select * from cities1000 where ('Canada Red Deer' like '%'||asciiname||'%' or
			// 'Canada Red Deer' like '%'||name||'%') Order by char_length(asciiname) DESC,
			// population DESC Limit 1
			String query = "Select St_Y(cpoint), St_X(cpoint) from timezone_shapes where tzid like '%'||$$" + loc
					+ "$$||'%' or $$" + loc
					+ "$$ like '%'||substring(tzid, 0, position('/' in tzid))||'%' Order by char_length(tzid) DESC Limit 1";
			ResultSet cr = null;
			Statement st = c.createStatement();
			cr = st.executeQuery(query);
			int counter = 0;
			double lat = 0;
			double lon = 0;
			while (cr.next()) {
				counter++;
				lat = cr.getDouble(1);
				lon = cr.getDouble(2);
			}
			if (counter > 0) {
				Coordinate latlong = new Coordinate(lat, lon);
				st.close();
				return latlong;
			} else {
				st.close();
			}
		}
		return null;
	}

	/**
	 * Helper method to find out if user_location is LIKE a cityname Order by
	 * longest string match and for the city with the highest population
	 * 
	 * @param loc
	 * @return the coordinates of the city
	 * @throws SQLException 
	 */
	private static Coordinate get_like_city(String loc, Connection c) throws SQLException {
		if (loc.length() > 3) {
			// Select * from cities1000 where ('Canada Red Deer' like '%'||asciiname||'%' or
			// 'Canada Red Deer' like '%'||name||'%') Order by char_length(asciiname) DESC,
			// population DESC Limit 1
			String query = "Select lat, lon from cities1000 where $$" + loc + "$$ like '%'||asciiname||'%' or $$" + loc
					+ "$$ like '%'||name||'%' Order by char_length(asciiname) DESC, population DESC Limit 1";
			ResultSet cr = null;
			Statement st = c.createStatement();
			cr = st.executeQuery(query);
			int counter = 0;
			double lat = 0;
			double lon = 0;
			while (cr.next()) {
				counter++;
				lat = cr.getDouble(1);
				lon = cr.getDouble(2);
			}
			if (counter > 0) {
				Coordinate latlong = new Coordinate(lat, lon);
				st.close();
				return latlong;
			} else {
				st.close();
			}
		}
		return null;
	}

	/**
	 * Helper method to find out if a point lays inside of a timezone_shape
	 * Additionally you could check if the user_timezone equals the timezone_id of
	 * the timezone_shape
	 * 
	 * @param lat
	 * @param lon
	 * @param utz,
	 *            the user_timezone
	 * @param withTZ
	 * @return Boolean
	 * @throws SQLException 
	 */
	private static boolean is_in_timezoneshape(double lat, double lon, String user_timezone, boolean withTZ) throws SQLException {
		boolean isIn = false;
		Connection c = DBManager.getConnection(LOCAL, RCP);
		String query = "Select tzid from timezone_shapes as b where St_Contains(b.geom, St_setsrid(St_point(" + lon
				+ ", " + lat + "),4326)) = TRUE;";
		ResultSet rs = null;
		Statement st = c.createStatement();
		rs = st.executeQuery(query);
		while (rs.next()) {
			String tz = rs.getString(1);
			if (withTZ) {
				if (tz.equals(user_timezone.trim()))
					isIn = true;
			} else {
				isIn = true;
			}

		}
		st.close();
		c.close();
		return isIn;
	}

	/**
	 * Helper method to get a matching city from the cities1000 dataset
	 * 
	 * @param loc
	 * @param tz
	 * @param withTZ
	 * @param c
	 * @return the coordinates of the city
	 * @throws SQLException 
	 */
	private static Coordinate get_is_city_most_populated(String loc, String tz, boolean withTZ, Connection c)  {
		String query = "Select lat, lon from cities1000 where asciiname = $$" + loc + "$$ or name = $$" + loc
				+ "$$ order by population DESC Limit 1";
		if (withTZ)
			query = "Select lat, lon from cities1000 where (asciiname = $$" + loc + "$$ or name = $$" + loc
					+ "$$) and timezone = $$" + tz + "$$ order by population DESC Limit 1";

		ResultSet cr = null;
		Statement st;
		try {
			st = c.createStatement();
			cr = st.executeQuery(query);
			int counter = 0;
			double lat = 0;
			double lon = 0;
			while (cr.next()) {
				counter++;
				lat = cr.getDouble(1);
				lon = cr.getDouble(2);
			}
			if (counter > 0) {
				Coordinate latlong = new Coordinate(lat, lon);
				st.close();
				return latlong;
			} else {
				st.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(query);
			return null;
		}
		return null;
	}

	/**
	 * Helper method to check if the user_timezone equals a valid timezone from
	 * timezone_shapes
	 * 
	 * @param tz
	 * @return the coordinates of the centroid of the timezone shape.
	 * @throws SQLException 
	 */
	private static Coordinate get_is_timezone(String tz, Connection c) {
		String query = "Select St_Y(tz.cpoint), St_X(tz.cpoint) from timezone_shapes as tz where tz.tzid = $$" + tz
				+ "$$;";
		ResultSet rs = null;
		Statement st;
		try {
			st = c.createStatement();
			rs = st.executeQuery(query);
			int counter = 0;
			double lat = 0;
			double lon = 0;
			while (rs.next()) {
				counter++;
				lat = rs.getDouble(1);
				lon = rs.getDouble(2);
			}
			if (counter > 0) {
				Coordinate latlong = new Coordinate(lat, lon);
				st.close();
				return latlong;
			} else {
				st.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(query);
			return null;
		}
		return null;
	}

	public static class Entry<A> {

		private ArrayList<String> a;

		// public Entry(ArrayList<A> geoFields) {
		//// this.a = a;
		// this.a = geoFields;
		// }

		public Entry(ArrayList<String> geoFields) {
			this.a = geoFields;
		}

		public ArrayList<String> getA() {
			return a;
		}

		public void setA(ArrayList<String> a) {
			this.a = a;
		}

	}

}
