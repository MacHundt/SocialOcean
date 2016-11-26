package utils;

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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery;
import org.apache.lucene.store.FSDirectory;

import bostoncase.parts.Histogram;
import bostoncase.parts.LuceneStatistics;
import bostoncase.parts.Time;
import bostoncase.parts.TopSelectionPart;
import interfaces.Console;
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
	
	private  ArrayList<String> queryStrings = new ArrayList<>();
	private  ArrayList<QueryResult> queryResults = new ArrayList<>();
	
	private List<String> fn;
	private String[] idxFields = null;
	
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
	
	public static enum TimeBin {
		SECONDS, MINUTES, HOURS, DAYS
	}
	
//	public static void main(String[] args) {
//		String formattedString = String.format("%s \t\t %.2f \t %d %s", "Test", 1*100/(float)4, 15, "%");       
//		System.out.println(formattedString);
//	}
	
	public  int serialCounter = 0;
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
		isInitialized = true;
		
	}
	
	public void printToConsole(String msg) {
		while (!Console.isInitialized) {
			continue;
		}
		Console c = Console.getInstance();
		c.outputConsole(msg);
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
		queryStrings.clear();
		queryResults.clear();
	}
	
	public void deleteAtIndex(int index) {
		if (index >= queryStrings.size())
			return;
		queryStrings.remove(index);
		queryResults.remove(index);
	}
	
	public ScoreDoc[] ADDQuery(Query query, boolean print) {
		serialCounter++;
		queryStrings.add(query.toString());
		ScoreDoc[] result = querySearcher.searchAll(query);
//		QueryResult qr = new QueryResult(query, result, serialCounter);
//		queryResults.add(qr);
//		
//		System.out.println(qr.toString());
//		printToConsole(qr.toString());
		if (print) {
			System.out.println("("+serialCounter+") "+query.toString()+" #:"+result.length);
			printToConsole("("+serialCounter+") "+query.toString()+" #:"+result.length);
		}
		return result;
	}
	
	public void FUSEQuery(int index, Query newQuery) {
	
		// fuse query this index - delete index, add new, inc serial
		serialCounter++;
		ScoreDoc[] result = querySearcher.fuseQuery(queryResults.get(index).query, newQuery);
		
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
	
	
	public void ADDGeoQuery(double minLat, double maxLat, double minLong, double maxLong) {
		Query query = new GeoPointInBBoxQuery(geoField, minLat, maxLat, minLong, maxLong);
		ADDQuery(query, true);
	}
	
	public ScoreDoc[] searchTimeRange(long from, long to, boolean print) {
		ScoreDoc[] result;
		try {
			Query query = parser.parse("date:["+from +" TO "+to +"]");
			result = ADDQuery(query, print);
			return result;
		} catch (ParseException e) {
			System.out.println("Could not Parse Date Search to Query");
			e.printStackTrace();
		}
		return null;
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
	
	private Connection newConnection() {
		String DATA = "boston";
		String DBNAME = "masterproject_"+DATA;
		String USER = "postgres";
		String PASS = "postgres";
		int PORT = 5432;
		Connection c = null;
		try {
			c = DriverManager.getConnection("jdbc:postgresql://localhost:"+PORT+"/"+DBNAME, USER, PASS);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}
	
	private Connection getConnection() {
		if (con == null) {
			con = newConnection();
		}
		return con;
		
	}
	
	public void initMaxDate() {
		try {
			Connection c = newConnection();
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate DESC Limit 1");
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
			Connection c = newConnection();
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate ASC Limit 1");
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
	
	
}
