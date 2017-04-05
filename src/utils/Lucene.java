package utils;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.xml.QueryBuilderFactory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery;
import org.apache.lucene.spatial.geopoint.search.GeoPointInPolygonQuery;
import org.apache.lucene.store.FSDirectory;

import bostoncase.parts.Console;
import bostoncase.parts.Histogram;
import bostoncase.parts.LuceneStatistics;
import bostoncase.parts.QueryHistory;
import bostoncase.parts.Time;
import bostoncase.parts.TopSelectionPart;
import impl.MapPanelCreator;
import impl.SwingWaypoint;
import interfaces.ILuceneQuerySearcher;

// as singleton
public enum Lucene {
	INSTANCE;
	
	// Hard Coded Parameters
	private  String field = "content";		// standard field
	private  String geoField = "geo";		// standard geoField
	private  Resolution dateResolution = Resolution.MILLISECOND;
	
	private  IndexReader reader;
	private  IndexSearcher searcher;
	private  Analyzer analyzer;
	private  QueryParser parser;
	private  ILuceneQuerySearcher querySearcher;
	
	private  IndexInfo idxInfo;
	private  int numTerms = 0;
	private  Map<String, FieldTermCount> termCounts;
	
	private  ArrayList<Query> queryHistory = new ArrayList<>();
	private  ArrayList<QueryResult> queryResults = new ArrayList<>();
	private  int currentPointer;
	
	private List<String> fn;
	private String[] idxFields = null;		// ALL Fields which are indexed
	
	private TermStats[] catHisto = null;
	
	private Connection con = null;
	
	// START TIME
//	private PreparedStatement pre_statement_min;
//	private PreparedStatement pre_statement_max; 

	private LocalDateTime dt_min = null;
	private long utc_time_min;
	public boolean hasStartTime = false;
	private LocalDateTime dt_max = null;
	private long utc_time_max;
	public boolean hasStopTime = false;
	
	private ScoreDoc[] last_result = null;
	private String last_query = "";
	private String query_type = "";
	
	private MultiFieldQueryParser mq = null;
	
	public static enum TimeBin {
		SECONDS, MINUTES, HOURS, DAYS
	}
	
	
	public enum QueryTypes {
		ADD, FUSE, NORMAL
	}
	
	
	private static Color grey = new Color(240,240,240,50);		// light grey, high opacity		--> NORMAL
	private static Color green = new Color(67,245,12,120);		// light green, high opacity	--> FUSE
	private static Color blue = new Color(12,210,245,120);		// light blue, high opacity		--> ADD
	
	
//	public static void main(String[] args) {
//		String formattedString = String.format("%s \t\t %.2f \t %d %s", "Test", 1*100/(float)4, 15, "%");       
//		System.out.println(formattedString);
//	}
	
	public int serialCounter = 0;
	public boolean isInitialized = false;
	
	public void initLucene(String index, ILuceneQuerySearcher querySearcher) {
		try {
			
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			idxInfo = new IndexInfo(reader, index);
			fn = idxInfo.getFieldNames();
			numTerms = idxInfo.getNumTerms();
            termCounts = idxInfo.getFieldTermCounts();
            idxFields = fn.toArray(new String[fn.size()]);
			if (fn.size() == 0) {
				System.out.println("Empty index.");
				// showStatus("Empty index."); --> print on Console ..
			}
			
			mq = new MultiFieldQueryParser(idxFields, analyzer);
			mq.setDefaultOperator(mq.AND_OPERATOR);
			isInitialized = true;
			
//			pre_statement_min = con.prepareStatement("Select creationdate from tweetdata order by creationdate ASC Limit 1");
//			pre_statement_max = con.prepareStatement("Select creationdate from tweetdata order by creationdate DESC Limit 1");
		
		} catch (IOException e) {
			System.out.println("Could not create LuceneSearcher, path to index not found "+index);
			e.printStackTrace();
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		searcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer();
		parser = new QueryParser(field, analyzer);
		parser.setDateResolution(dateResolution);
		this.querySearcher = querySearcher;
		querySearcher.initQuerySearcher(searcher, analyzer);
		
	}
	
	
	public void printToConsole(String msg) {
		while (!Console.isInitialized) {
			continue;
		}
		Console c = Console.getInstance();
		c.outputConsole(msg);
	}
	
	
	public void clearQueryHistroy() {
		if (QueryHistory.isInitialized) {
			QueryHistory history = QueryHistory.getInstance();
			history.clearHistory();
		}
		queryHistory.clear();
		last_query = "";
	}
	
	public void clearMap() {
		last_result = null;
		MapPanelCreator.clearWayPoints(true);
	}
	
	public void printLuceneFields() {
		
		// sort by names now
		String[] idxFieldsCopy = idxFields.clone();
		// sort by term count
		ValueComparator bvc = new ValueComparator(termCounts);
		TreeMap<String, FieldTermCount> termCountsSorted = new TreeMap<String, FieldTermCount>(bvc);
		termCountsSorted.putAll(termCounts);
		// String[] sortedFields = termCountsSorted.keySet().toArray(new
		// String[termCounts.size()]);
		// String[] idxFieldsCopySorted = new String[idxFields.length];
		// System.arraycopy(sortedFields, 0, idxFieldsCopySorted, 0,
		// sortedFields.length);
		// if (termCounts.size() < idxFieldsCopy.length) {
		// int idx = sortedFields.length;
		// for (String f : idxFields) {
		// if (!termCounts.containsKey(f)) {
		// idxFieldsCopySorted[idx] = f;
		// idx += 1;
		// }
		// }
		// }
		System.out.println(String.format("\n%-10s %15s \t %-10s", "Name", "Term Count", "%"));
		for (FieldTermCount ftc : termCountsSorted.values()) {
			String formattedString = String.format("%-10s %15d \t %.2f %s", ftc.fieldname, ftc.termCount,
					(ftc.termCount * 100) / (float) numTerms, "%");
			System.out.println(formattedString);
		}
	}
	
	
	public void printStatistics() {
		
		ArrayList<String> build = new ArrayList<>();
		build.add("Index name: \t\t\t"+idxInfo.getIndexPath());
		build.add("Lucene Version: \t\t"+idxInfo.getVersion());
		int fieldsCount = termCounts.size();
		build.add("Number of fieds: \t\t"+fieldsCount);
		build.add("Number of terms: \t\t"+numTerms);
		build.add("Index filesize: \t\t\t"+idxInfo.getTotalFileSize()/1000000+" MB");
		
		build.forEach(System.out::println); 
		while (!LuceneStatistics.isInitialized) {
			continue;
		}
		LuceneStatistics ls = LuceneStatistics.getInstance();
		build.forEach(item->ls.printLuceneStatistics(item));
		
		// wait for TOPSELECTIONPart -- be created
		
		TopSelectionPart tsp = TopSelectionPart.getInstance();
		
		Object[][] tableData = new Object[fieldsCount][tsp.detailsColumns];
		
		for (int i= 0; i< tableData.length; i++) {
			tableData[i][0] = fn.get(i);		// field name
			tableData[i][1] = new Long(termCounts.get(fn.get(i)).termCount);
			DecimalFormat format = new DecimalFormat("##.##");
			String s = format.format((termCounts.get(fn.get(i)).termCount*100.0 / numTerms));
			tableData[i][2] = s + " %" ;
		}
		tsp.setDetailTable(tableData);
		
	}
	

	public void changeAnalyser(Analyzer newAnalyser) {
		analyzer = newAnalyser;
		querySearcher.changeAnalyzer(analyzer);
	}
	
	public ILuceneQuerySearcher getQuerySearch() {
		return querySearcher;
	}
	
	public ArrayList<QueryResult> getQueries() {
		return queryResults;
	}
	
	public void resetQueryHistory() {
		serialCounter = 0;
		queryHistory.clear();
		queryResults.clear();
		currentPointer = 0;
	}
	
	public void deleteAtIndex(int index) {
		if (index >= queryHistory.size())
			return;
		queryHistory.remove(index);
		queryResults.remove(index);
		currentPointer--;
	}
	
	
	/**
	 * This method queries the Lucene Index.
	 * If <code>type</code> is an empty string the query is processed normally.
	 * If <code>type</code> equals "ADD", "this query OR last_query" is processed.
	 * If <code>type</code> equals "FUSE", "this query AND last_query" is processed.
	 * @param query
	 * @param type
	 * @param print
	 * @return result ScoreDoc[]
	 */
	public ScoreDoc[] query(Query query, String type, boolean print) {
		serialCounter++;
		ScoreDoc[] result = null;
		
		// ADD
		try {
			if (type.equals("ADD") && !last_query.isEmpty()) {
				String newQuery = "";
				if (query.toString().startsWith("GeoPointInBBoxQuery") || last_query.startsWith("GeoPointInBBoxQuery")) {
					// merge the both results by hand  
//					ArrayList<Query> last_two = new ArrayList<>();
//					last_two.add(queryHistory.get(queryHistory.size()-1));
//					last_two.add(query);
//					DisjunctionMaxQuery union = new DisjunctionMaxQuery(last_two, 0);
//					result = querySearcher.searchAll(union);
					result = querySearcher.searchAll(query);
					result = mergeScoreDocs(result);
				} 
				// TODO The same with TIME
				else if (query.toString().startsWith("Time") || last_query.startsWith("Time")) {
					
				}
				
				else {
					newQuery = query.toString() + " OR ("+last_query+")";
					query = parser.parse(newQuery);
				}
			
			} 
//		FUSE
			else if (type.equals("FUSE") && !last_query.isEmpty()) {
				String newQuery = "";
				if (query.toString().startsWith("GeoPointInBBoxQuery") || last_query.startsWith("GeoPointInBBoxQuery")) {
					// FUSE and Geo is a selection!
					// 1) case: last_query is empty --> nothing to FUSE, we are not here
					// 2) case: last_result AND new_result -- get the CUT manually when fields overlap!
					// -- AND
					
					result = querySearcher.searchAll(query);
					if (result.length< 100000 || last_result.length < 100000)
						result = cutScoreDocs(result);
					
					
//					String[] fields = getFieldsFromQueries() 
//					String[] usedFields = {"tags", "geo"};
//					String[] queries = new String[2];
//					queries[0] = queryHistory.get(queryHistory.size()-1).toString();
//					queries[1] = query.toString();
//					Query nQ = mq.parse(queries, usedFields, analyzer);
//					result = querySearcher.searchAll(query);
					
////					BooleanQuery bq = BooleanQuery.Builder.class.newInstance().build();
//					Builder bq = new Builder();
//					bq.add(queryHistory.get(queryHistory.size()-1), Occur.MUST);
//					bq.add(query, Occur.MUST);
//					result = querySearcher.searchAll(bq.build());
					
					
				} 
				// TODO The same with TIME
				else if (query.toString().startsWith("Time") || last_query.startsWith("Time")) {
					
				}
				
				else {
					newQuery = "("+query.toString()+")" + " AND ("+last_query+")";
					query = parser.parse(newQuery);
					last_query = query.toString();
					result = querySearcher.searchAll(query);
				}
				
//				System.out.println("FUSE: \nLastQuery: "+ queryHistory.get(queryHistory.size()-1).toString() +""
//						+ "\nQuery: "+ query.toString());
//				
//				Builder bq = new Builder();
//				bq.add(queryHistory.get(queryHistory.size()-1), Occur.MUST);
//				bq.add(query, Occur.FILTER);
//				result = querySearcher.searchAll(bq.build());
				
			} 
			
//			NORMAL
			else {
				last_query = query.toString();
				result = querySearcher.searchAll(query);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		if (print && result != null) {
			System.out.println("("+serialCounter+") "+query.toString()+" #:"+result.length);
			printToConsole("("+serialCounter+") "+query.toString()+" #:"+result.length);
		}
		
		if (QueryHistory.isInitialized) {
			QueryHistory history = QueryHistory.getInstance();
			if (type.length() > 2) {
				history.addQuery(type+"\t"+query.toString());
			} else {
				history.addQuery(query.toString());
			}
		}
		
		queryHistory.add(query);
		last_result = result;
		return result;
	}
	
	
	
	
	private ScoreDoc[] cutScoreDocs(ScoreDoc[] result) {
		if (last_result == null)
			return result;
		
		// NAIV --> selection / filter  Find x in y
		
		ArrayList<ScoreDoc> finding = new ArrayList<>();
		for (ScoreDoc x : last_result) {
			for (ScoreDoc y : result) {
				if (x.doc == y.doc) {
					finding.add(x);
					break;
				}
			}
		}
		
		ScoreDoc[] out = new ScoreDoc[finding.size()];
		for (int i = 0; i < finding.size(); i++) {
			out[i] = finding.get(i);
		}
		
		return out;
	}


	public ScoreDoc[] mergeScoreDocs( ScoreDoc[] result) {
		
		if (last_result == null)
			return result;
		
		ScoreDoc[] new_result = new ScoreDoc[last_result.length + result.length];
		int i = 0;
		for (ScoreDoc doc : last_result) {
			new_result[i++] = doc;
		}
		for (ScoreDoc doc : result) {
			new_result[i++] = doc;
		}
		
		return new_result;
	}

	
	
	public IndexSearcher getIndexSearcher () {
		return searcher;
	}
	
	
	public void FUSEQueries(ArrayList<Integer> queryIndexes) {
		
		// fuse queries from the query result array
		// delete the indexes - add a new, inc serial
	}
	
	/**
	 * This method searches for the <code>topX</code> documents on a single lucene <code>field</code>
	 * @param field
	 * @param topX
	 * @throws IOException 
	 */
	public TermStats[] searchTopXOfField(String field, int topX)  {
		String[] fields = {field};
		try {
			// TODO put into external thread!
			TermStats[] result = HighFreqTerms.getHighFreqTerms(reader, topX, fields);
			
			for (TermStats ts : result) {
				System.out.println(ts.toString());
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	// Geo Filter
	public ScoreDoc[] ADDGeoQuery(double minLat, double maxLat, double minLong, double maxLong) {
		@SuppressWarnings("deprecation")
		Query query = new GeoPointInBBoxQuery(geoField, minLat, maxLat, minLong, maxLong);
		ScoreDoc[] geoFilter = query(query, getQeryType(), true);
		
		addnewQueryResult(geoFilter, query);
		
		return geoFilter;
	}
	
	
	// Time Filter
	public ScoreDoc[] searchTimeRange(long from, long to, boolean print) {
		ScoreDoc[] result;
		try {
			Query query = parser.parse("date:["+from +" TO "+to +"]");
			result = query(query, "", print);
			return result;
		} catch (ParseException e) {
			System.out.println("Could not Parse Date Search to Query");
			e.printStackTrace();
		}
		return null;
	}
	

	public ScoreDoc[] getLastResult() {
		return last_result;
	}
	
	public void reset_lastResult() {
		last_result = null;
	}
	
	
	public QueryParser getParser() {
		return parser;
	}
	
	
	private class ValueComparator implements Comparator<String> {
		Map<String, FieldTermCount> base;

		public ValueComparator(Map<String, FieldTermCount> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(String a, String b) {
			return base.get(a).compareToValues(base.get(b));
		}
	}
	
		
	public class QueryResult implements Comparable<QueryResult>{
		
		private int maxChars = 20;
		private int serial;
		private Query query;
		private ScoreDoc[] result;
		
		public QueryResult(Query query, ScoreDoc[] result, int serial ) {
			this.serial = serial;
			this.query = query;
			this.result = result;
		}

		@Override
		public int compareTo(QueryResult qr) {
			return Integer.compare(serial, qr.serial);
		}
		
		
		// for queryHistory
		public String shortString() {
			return "("+ serial +") "+query.toString().substring(0, maxChars);
		}
		
		// for a tooltip
		@Override
		public String toString() {
			return "("+serial+") "+query.toString()+" #:"+result.length;
		}
		
	}


	public void showCatHisto() {
		if (catHisto == null)
			catHisto = searchTopXOfField("category", 20);
		
		Object[][] resulTable = new Object[catHisto.length][2];
		for (int i= 0; i< catHisto.length; i++) {
			TermStats ts = catHisto[i];
			resulTable[i][0] = ts.termtext.utf8ToString();
			resulTable[i][1] = new Integer(ts.docFreq);				
		}
		
		// TODO Histogram Part must be created! 
		Histogram histogram = Histogram.getInstance();
		histogram.chnageDataSet(resulTable);
		
	}
	
	
	
	public void changeHistogramm(ScoreDoc[] data) {
		
		if (!Histogram.isInitialized)
			return;
		
		Histogram histogram = Histogram.getInstance();
		HashMap<String, Integer> counter = new HashMap<>();
		
			for (ScoreDoc doc : data) {
				Document document = null;
				try {
				document = searcher.doc(doc.doc);
				} catch (IOException e) {
					continue;
				}
				String field = "";
				
				if ((document.getField("category")) == null)
					continue;
				
				field = (document.getField("category")).stringValue();

				if (counter.containsKey(field)) {
					counter.put(field, (counter.get(field) + 1));
				} else {
					counter.put(field, 1);
				}
			}
		
		
		int size = counter.keySet().size();
		
		Object[][] resulTable = new Object[size][2];
		int i = 0;
		for (String key : counter.keySet()) {
			resulTable[i][0] = key;
			resulTable[i][1] = counter.get(key);
			i++;
		}
		
		histogram.chnageDataSet(resulTable);
	}
	
	
	
	public void initMaxDate() {
		try {
			Connection c = DBManager.getConnection();
			Statement stmt = c.createStatement();
//			ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate DESC Limit 1");
			ResultSet rs = stmt.executeQuery("Select max from tw_minmax_date");
//			ResultSet rs = pre_statement_max.executeQuery();
			String maxDate = "";
			while (rs != null && rs.next()) {
				maxDate = rs.getString(1);
			}
			if (maxDate.isEmpty() || !maxDate.contains(" "))
				return;
			
			String[] datetime_String = maxDate.split(" ");
			String date_Str = datetime_String[0];
			String time_Str = datetime_String[1];
			String[] date_arr = date_Str.trim().split("-");
			String[] time_arr = time_Str.trim().split(":");
			if (date_arr.length == 3 && time_arr.length == 3) {
				// DATE
				int year = Integer.parseInt(date_arr[0]);
				int month = Integer.parseInt(date_arr[1]);
				int day = Integer.parseInt(date_arr[2]);
				// TIME
				int hour = Integer.parseInt(time_arr[0]);
				int min = Integer.parseInt(time_arr[1]);
				int sec = Integer.parseInt(time_arr[2]);
			
				LocalDate date = LocalDate.of(year, month, day);
				LocalTime time = LocalTime.of(hour, min, sec);
			
				dt_max = LocalDateTime.of(date, time);
				utc_time_max= dt_max.toEpochSecond(ZoneOffset.UTC);
				hasStopTime = true;
				
				System.out.println(dt_max.toEpochSecond(ZoneOffset.UTC) + " = "+ dt_max.toString());
			
			}
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		

	public void initMinDate() {
		
		try {
			Connection c = DBManager.getConnection();
			Statement stmt = c.createStatement();
//			ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate ASC Limit 1");
			ResultSet rs = stmt.executeQuery("Select min from tw_minmax_date");
//			ResultSet rs = pre_statement_min.executeQuery();
			String minDate = "";
			while (rs != null && rs.next()) {
				minDate = rs.getString(1);
			}
			if (minDate.isEmpty() || !minDate.contains(" "))
				return;
			
			String[] datetime_String = minDate.split(" ");
			String date_Str = datetime_String[0];
			String time_Str = datetime_String[1];
			String[] date_arr = date_Str.trim().split("-");
			String[] time_arr = time_Str.trim().split(":");
			if (date_arr.length == 3 && time_arr.length == 3) {
				// DATE
				int year = Integer.parseInt(date_arr[0]);
				int month = Integer.parseInt(date_arr[1]);
				int day = Integer.parseInt(date_arr[2]);
				// TIME
				int hour = Integer.parseInt(time_arr[0]);
				int min = Integer.parseInt(time_arr[1]);
				int sec = Integer.parseInt(time_arr[2]);
			
				LocalDate date = LocalDate.of(year, month, day);
				LocalTime time = LocalTime.of(hour, min, sec);
			
				dt_min = LocalDateTime.of(date, time);
				utc_time_min = dt_min.toEpochSecond(ZoneOffset.UTC);
				hasStartTime = true;
				
				System.out.println(dt_min.toEpochSecond(ZoneOffset.UTC) + " = "+ dt_min.toString());
			}
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public void createTimeLine(TimeBin binsize) {
		
		// From Start Date to StopDate .. make bins and plot
		LocalDateTime dt_temp = dt_min;
		ArrayList<TimeLineHelper> tl_data = new ArrayList<>();
		
		long temp_utc = utc_time_min;
		while (temp_utc <= utc_time_max) {
			
			LocalDateTime dt_plus = dt_temp;
			switch (binsize) {
			case SECONDS:
				dt_plus = dt_temp.plusSeconds(1);
				break;
			case MINUTES:
				dt_plus = dt_temp.plusMinutes(1);
				break;
			case HOURS:
				dt_plus = dt_temp.plusHours(1);
				break;
			case DAYS:
				dt_plus = dt_temp.plusDays(1);
				break;
			}
			long utc_plus = dt_plus.toEpochSecond(ZoneOffset.UTC);
			ScoreDoc[] rs = searchTimeRange(temp_utc, utc_plus, false);
			tl_data.add(new TimeLineHelper(dt_temp, rs.length));
			
			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}
		
		Time time = Time.getInstance();
		time.chnageDataSet(tl_data);
		
	}

	
	public void showInMap(ScoreDoc[] result, boolean clearList) {
		// Show on Map
		if (result != null) {
			if (clearList)
				MapPanelCreator.clearWayPoints(clearList);

			// Connection c = DBManager.getConnection();
			// try {
			// Statement stmt = c.createStatement();
			//
			// for (ScoreDoc entry : result) {
			// int docID = entry.doc;
			// try {
			// Document document = searcher.doc(docID);
			//// System.out.println(document.getField("id").stringValue());
			// long hashgeo =
			// (document.getField("geo")).numericValue().longValue();
			// double lat = GeoPointField.decodeLatitude(hashgeo);
			// double lon = GeoPointField.decodeLongitude(hashgeo);
			// String id = (document.getField("id")).stringValue();
			// String type = (document.getField("type")).stringValue();
			// String query = "";
			// double sentiment = 0;
			// switch(type) {
			// case "twitter":
			// query = "Select t.sentiment from tweetdata as t where t.tweetid =
			// "+Long.parseLong(id);
			// break;
			// case "flickr" :
			// query = "Select t.sentiment from flickrdata as t where
			// t.\"photoID\" = "+Long.parseLong(id);
			//
			// break;
			// default:
			// query = "Select t.sentiment from tweetdata as t where t.tweetid =
			// "+Long.parseLong(id);
			// }
			//
			// // Get Sentiment from Database // Lucene is faster
			// ResultSet rs = stmt.executeQuery(query);
			//
			// while (rs.next()) {
			//// System.out.println("senti: "+rs.getInt(1));
			// sentiment = rs.getInt(1);
			// }
			//
			// MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(docID+"",
			// sentiment, lat, lon));
			//
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			// }
			// } catch (SQLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			for (ScoreDoc entry : result) {
				int docID = entry.doc;
				try {
					Document document = searcher.doc(docID);
					// System.out.println(document.getField("id").stringValue());
					long hashgeo = (document.getField("geo")).numericValue().longValue();
					double lat = GeoPointField.decodeLatitude(hashgeo);
					double lon = GeoPointField.decodeLongitude(hashgeo);
					String id = (document.getField("id")).stringValue();
					// String type = (document.getField("type")).stringValue();
					String query = "";
//					double sentiment = Double.parseDouble((document.getField("sentiment")).stringValue());
//					MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(docID + "", sentiment, lat, lon));
					String sentiment = (document.getField("sentiment")).stringValue();
					MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, sentiment, lat, lon));

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			MapPanelCreator.showWayPointsOnMap();
		}

	}

	public void setQeryType(String text) {
		query_type = text;
	}
	
	public String getQeryType() {
		return query_type;
	}
	
	
	public Color getColor() {
		Color c;
		
		if (query_type.equals(Lucene.QueryTypes.ADD.toString())) {
			c = blue;
//			c = Color.BLUE;
		} else if (query_type.equals(Lucene.QueryTypes.FUSE.toString())) {
			c = green;
//			c = Color.GREEN;
		} else {
			c = grey;
//			c = Color.GRAY;
		}
		
		return c;
	}


	public void showLastResult() {
		
//		currentPointer--;
//		showInMap(queryResults.get(currentPointer).result, true);
//		changeHistogramm(queryResults.get(currentPointer).result);
//		System.out.println(currentPointer);
//		last_result = queryResults.get(currentPointer).result;
		
	}
	
	public void addnewQueryResult(ScoreDoc[] result, Query query) {
		
//		queryResults.add(new QueryResult(query, result, 0));
//		currentPointer = queryResults.size()-1;
//		System.out.println(currentPointer);
	}
	
	
}
