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

	private static String user_table = "users";
	private static int fetchsize = 10000;
	static ResultSet rs = null;
	static ArrayList<Entry<ArrayList<String>>> list = null;
	static ArrayList<String> fields = null;

	private static boolean LOCAL = false;
	private static boolean RCP = false;

	private static int batchcounter = 0;
	
	private static java.util.regex.Pattern letters = java.util.regex.Pattern.compile("\\w+\\.?");

	public static void main(String[] args) {

		Connection c = DBManager.getConnection(LOCAL, RCP);
		String query = "Select user_id, user_location, user_timezone, geocoding_type from " + user_table + " where geom is null";

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
				String location = rs.getString(2);
				String timezone = rs.getString(3);
				int geoType = rs.getInt(4);
				
				// GT 10  -- Stay Default. No geocoding possible
				if ((location == null || location.trim().isEmpty()) && timezone.equals("null")) {
					if (counter == fetchsize) {
						counter = 0;
					}
					continue;
				}

				fields = new ArrayList<>();
				fields.add(uid+"");
				fields.add(location);
				fields.add(timezone);
				fields.add(""+geoType);
				Entry<ArrayList<String>> tup = new Entry<ArrayList<String>>(fields);
//				Tuple<Long, ArrayList<String>> tup = new Tuple<Long, ArrayList<String>>(uid, geoFields);
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
		} catch (SQLException e) {
			e.printStackTrace();

			main(args);

		}
	}


	/**
	 * Geocode the batched tweets
	 * @param list
	 * @throws SQLException 
	 */
	private static void geocode(ArrayList<Entry<ArrayList<String>>> list) throws SQLException {
		
		if (list.isEmpty())
			return;
		
//		System.out.println ("abc.".matches(letters.pattern())); 
		int batchsize = 1000;
		Connection c = DBManager.getConnection(LOCAL, RCP);
		c.setAutoCommit(false);
		
		Statement st = c.createStatement();
		
		// GEOCODE
		int counter = 0;
		for (Entry<ArrayList<String>> e : list) {
			
			long uid = Long.parseLong(e.getA().get(0));
			String loc = e.getA().get(1);
			String tz = e.getA().get(2);
			int geoType = Integer.parseInt(e.getA().get(3));
			
			String updateQuery = "";
			if (geoType > 5) {
				updateQuery = geocode5(uid, loc, tz);
				if (updateQuery != null) {
//					System.out.println(loc+" >> From Type "+geoType);
//					System.out.println(updateQuery);
					st.addBatch(updateQuery);
					counter++;
				}
			}
			
			
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

	
	/**
	 * GT 5 Test
	 * Geocoding_type5:
	 * user_location is a city
	 * update ... add to batch.
	 * @param uid
	 * @param loc
	 * @param tz
	 * @return The Update query String
	 */
	private static String geocode5(long uid, String loc, String tz) {
		
		if (loc.matches(letters.pattern()))  {
			// loc is a cityname ( get asciiname of cities1000
			// sort by population, take the first
			String location = loc.toLowerCase();
			location = location.substring(0, 1).toUpperCase() + location.substring(1);
			Coordinate cityCoor = get_is_city_most_populated(location, tz, false);
			if (cityCoor != null) {
				String query = "Update "+user_table+" set geocoding_type = 5, "
						+ "geom = St_setsrid(St_Point("+cityCoor.y+","+cityCoor.x+"), 4326), "
								+ "longitude = "+cityCoor.y+", "
										+ "latitude = "+cityCoor.x+" where user_id = "+uid+";";
				return query;
			}
		}
		return null;
	}

	
	
	private static Coordinate get_is_city_most_populated(String loc, String tz, boolean withTZ) {
		Connection c = DBManager.getConnection(LOCAL, RCP);
		String query = "Select lat, lon from cities1000 where asciiname = '"+loc+"' order by population DESC Limit 1";
		if (withTZ)
			query = "Select lat, lon from cities1000 where asciiname = '"+loc+"' and timezone = '"+tz+"' order by population DESC Limit 1";
		
		ResultSet cr = null;
		try {
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
			Coordinate latlong = new Coordinate(lat, lon);
			st.close();
			c.close();
			return latlong;
					
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static class Entry<A> {

		private ArrayList<String> a;

//		public Entry(ArrayList<A> geoFields) {
////			this.a = a;
//			this.a = geoFields;
//		}


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
