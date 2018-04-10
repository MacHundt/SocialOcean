package utils;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Display;
import org.jxmapviewer.viewer.GeoPosition;

import impl.GraphCreatorThread;
import impl.GraphML_Helper;
import impl.GeneralGraphCreator;
import impl.DetailedGraphCreator;
import impl.MapPanelCreator;
import impl.MyEdge;
import impl.MyLuceneAnalyser;
import impl.MyUser;
import impl.ReIndexingThread;
import impl.StoreToJSONThread;
import impl.TimeLineCreatorThread;
import interfaces.ILuceneQuerySearcher;
import socialocean.model.Result;
import socialocean.parts.CategoriesPart;
import socialocean.parts.Console;
import socialocean.parts.Histogram;
import socialocean.parts.LuceneStatistics;
import socialocean.parts.QueryHistory;
import socialocean.parts.SettingsPart;
import socialocean.parts.Time;
import socialocean.parts.TopSelectionPart;

// as singleton
public enum Lucene {
	INSTANCE;

	// Hard Coded Parameters
	private String field = "content"; // standard field
	private String geoField = "geo"; // standard geoField
	private Resolution dateResolution = Resolution.MILLISECOND;

	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser;
	private ILuceneQuerySearcher querySearcher;

	private IndexInfo idxInfo;
	private int numTerms = 0;
	private Map<String, FieldTermCount> termCounts;

	private ArrayList<Query> queryHistory = new ArrayList<>();
	private ArrayList<QueryResult> queryResults = new ArrayList<>();
	private int currentPointer;

	private List<String> fn;
	private String[] idxFields = null; // ALL Fields which are indexed
	
	private TermStats[] catHisto = null;
	
	private ColorScheme colorScheme = ColorScheme.SENTIMENT;

	private LocalDateTime dt_min = null;
	private long utc_time_min;
	public boolean hasStartTime = false;
	private LocalDateTime dt_max = null;
	private long utc_time_max;
	public boolean hasStopTime = false;
	private ArrayList<TimeLineHelper> completeDataTime = new ArrayList<>();

	private Result last_result = null;
	private String last_query = "";
	private String query_type = "";

	private MultiFieldQueryParser mq = null;
	
	private static int reIndexCount = 0;
	
	public static enum TimeBin {
		SECONDS, MINUTES, HOURS, DAYS
	}
	
	public static enum ColorScheme {
		SENTIMENT, CATEGORY, SENTISTRENGTH
	}

	public enum QueryTypes {
		ADD, FUSE, NORMAL
	}

	private static Color grey = new Color(240, 240, 240, 50); // light grey,
																// high opacity
																// --> NORMAL
	private static Color green = new Color(67, 245, 12, 120); // light green,
																// high opacity
																// --> FUSE
	private static Color blue = new Color(12, 210, 245, 120); // light blue,
																// high opacity
																// --> ADD

	public int serialCounter = 0;
	public boolean isInitialized = false;
	private boolean changedTimeSeries;
	private boolean withMention = true;
	private boolean withFollows = false;
	private String luceneIndex;
	private long user_minDate;
	private long user_maxDate;
	private boolean ONLY_GEO;
	
	public static boolean SHOWHeatmap = true;
	public static boolean SHOWCountries = false;
	public static boolean SHOWUser = false;
	public static boolean SHOWTweet = true;
	
	public static boolean DATACHANGED = false;
	public static boolean INITCountries = false;
	
	public void initLucene(String index, ILuceneQuerySearcher querySearcher) throws Exception {

		luceneIndex = index;
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(luceneIndex)));
		idxInfo = new IndexInfo(reader, luceneIndex);
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
		changedTimeSeries = false;
		searcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer();
//		analyzer = new MyLuceneAnalyser();
		parser = new QueryParser(field, analyzer);
		parser.setDateResolution(dateResolution);
		this.querySearcher = querySearcher;
		querySearcher.initQuerySearcher(searcher, analyzer);

	}
	
	
	public String getLucenIndexPath() {
		return luceneIndex;
	}

	
	public void printToConsole(String msg) {
		while (!Console.isInitialized) {
			continue;
		}
		Console c = Console.getInstance();
		c.outputConsole(msg);
	}
	
	
	public void printlnToConsole(String msg) {
		while (!Console.isInitialized) {
			continue;
		}
		Console c = Console.getInstance();
		c.outputConsoleln(msg);
	}

	public void clearQueryHistroy() {
		if (QueryHistory.isInitialized) {
			QueryHistory history = QueryHistory.getInstance();
			history.clearHistory();
		}
		
		queryHistory.clear();
		last_query = "";
		currentPointer = 0;
		queryResults.clear();
	}

	public void clearMap() {
		last_result = null;
		MapPanelCreator.clearWayPoints(true);
	}
	
	public void clearWayPoints() {
		MapPanelCreator.clearWayPoints(true);
	}

	public void printLuceneFields() {

		// sort by names now
		String[] idxFieldsCopy = idxFields.clone();
		// sort by term count
		ValueComparator bvc = new ValueComparator(termCounts);
		TreeMap<String, FieldTermCount> termCountsSorted = new TreeMap<String, FieldTermCount>(bvc);
		termCountsSorted.putAll(termCounts);
		System.out.println(String.format("\n%-10s %15s \t %-10s", "Name", "Term Count", "%"));
		for (FieldTermCount ftc : termCountsSorted.values()) {
			String formattedString = String.format("%-10s %15d \t %.2f %s", ftc.fieldname, ftc.termCount,
					(ftc.termCount * 100) / (float) numTerms, "%");
			System.out.println(formattedString);
		}
	}

	public void printStatistics() {

		ArrayList<String> build = new ArrayList<>();
		build.add("Index name: \t\t\t" + idxInfo.getIndexPath());
		build.add("Lucene Version: \t\t" + idxInfo.getVersion());
		int fieldsCount = termCounts.size();
		build.add("Number of fieds: \t\t" + fieldsCount);
		build.add("Number of terms: \t\t" + numTerms);
		build.add("Index filesize: \t\t\t" + idxInfo.getTotalFileSize() / 1000000 + " MB");

		build.forEach(System.out::println);
		while (!LuceneStatistics.isInitialized) {
			continue;
		}
		LuceneStatistics ls = LuceneStatistics.getInstance();
		build.forEach(item -> ls.printLuceneStatistics(item));

		TopSelectionPart tsp = TopSelectionPart.getInstance();
		
		// fields - date, geo, id, path
		Object[][] tableData = new Object[tsp.detailsToShow.length][tsp.detailsColumns];
//		int detailsToShow_counter = tsp.detailsToShow.length-4;
		int index = 0;
		for (int i = 0; i < fn.size(); i++) {
			
			String fieldname = fn.get(i);	// field name
//			if (isInDetails(fieldname, tsp.detailsToShow) && detailsToShow_counter >= 0) {
//				int index = detailsToShow_counter--;
			if (isInDetails(fieldname, tsp.detailsToShow)) {
				tableData[index][0] = fieldname; 
				if (termCounts.get(fn.get(i)) != null )
					tableData[index][1] = new Long(termCounts.get(fn.get(i)).termCount);
				else 
					tableData[index][1] = 0;
				
				index++;
//				DecimalFormat format = new DecimalFormat("##.##");
//				String s = format.format((termCounts.get(fn.get(i)).termCount * 100.0 / numTerms));
//				tableData[index][2] = s + " %";
			}
		}
		tsp.setDetailTable(tableData);
		
		Object[][] result_data = new Object[9][3];
		tsp.setResultTable(result_data);

	}
	
	private boolean isInDetails(String name, String[] details) {
		for (String s : details) {
			if (s.equals(name)) {
				return true;
			}
		}
		return false;
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
	 * This method queries the Lucene Index. If <code>type</code> is an empty
	 * string the query is processed normally. If <code>type</code> equals
	 * "ADD", "this query OR last_query" is processed. If <code>type</code>
	 * equals "FUSE", "this query AND last_query" is processed.
	 * 
	 * @param query
	 * @param type
	 * @param print
	 * @return result ScoreDoc[]
	 */
	public Result query( Query query, String type, boolean print, boolean addToQueryHistory) {
		serialCounter++;
		ScoreDoc[] queryResult = null;

		// ADD
		try {
			if (type.equals("ADD") && !last_query.isEmpty()) {
				String newQuery = "";
				if (query.toString().contains("GeoPointInBBoxQuery")
						|| last_query.contains("GeoPointInBBoxQuery")) {
					// merge the both results by hand
					// ArrayList<Query> last_two = new ArrayList<>();
					// last_two.add(queryHistory.get(queryHistory.size()-1));
					// last_two.add(query);
					// DisjunctionMaxQuery union = new
					// DisjunctionMaxQuery(last_two, 0);
					// result = querySearcher.searchAll(union);
					queryResult = querySearcher.searchAll(query);
					queryResult = mergeScoreDocs(queryResult);
				}
				// Time Selection
				else if (query.toString().startsWith("date")) {
//					queryResult = querySearcher.searchAll(query);
//					mergeScoreDocs(queryResult);
					timeRangeFilter(query);
				}
				else if (query.toString().startsWith("urls:")) {
					if (query instanceof TermQuery) {
						queryResult = querySearcher.searchAll(query);
						queryResult = mergeScoreDocs(queryResult);
					}
				}
				else {
					newQuery = query.toString() + " OR (" + last_query + ")";
					last_query = newQuery;
					query = parser.parse(newQuery);
					queryResult = querySearcher.searchAll(query);
				}

			}
			// FUSE
			else if (type.equals("FUSE") && !last_query.isEmpty()) {
				String newQuery = "";
				if (query.toString().contains("GeoPointInBBoxQuery")
						|| last_query.contains("GeoPointInBBoxQuery")) {
					// FUSE and Geo is a selection!
					// 1) case: 
//						last_query is empty --> nothing to FUSE, we are not here
					// 2) case: 
//						last_result AND new_result -- get the CUT manually when fields overlap!
					queryResult = querySearcher.searchAll(query);
					queryResult = cutScoreDocs(queryResult);

					// String[] fields = getFieldsFromQueries()
					// String[] usedFields = {"tags", "geo"};
					// String[] queries = new String[2];
					// queries[0] =
					// queryHistory.get(queryHistory.size()-1).toString();
					// queries[1] = query.toString();
					// Query nQ = mq.parse(queries, usedFields, analyzer);
					// result = querySearcher.searchAll(query);

					//// BooleanQuery bq =
					//// BooleanQuery.Builder.class.newInstance().build();
					// Builder bq = new Builder();
					// bq.add(queryHistory.get(queryHistory.size()-1),
					//// Occur.MUST);
					// bq.add(query, Occur.MUST);
					// result = querySearcher.searchAll(bq.build());

				}
				// Time Selection from last_result
				else if (query.toString().startsWith("date")) {
					timeRangeFilter(query);
				}
				
				
				else {
					newQuery = "(" + query.toString() + ")" + " AND (" + last_query + ")";
					
					if (query instanceof TermQuery || last_query.contains("urls:")) {
						BooleanQuery bool =  BooleanQuery.Builder.class.newInstance().build();
						Builder bq = new Builder();
						// last AND new
						if (last_query.contains("urls:")) {
							String url = last_query.substring(last_query.indexOf("urls:")+5);
							TermQuery tquery = new TermQuery(new Term("urls", url));
							bq.add(tquery, Occur.MUST);
						}
						else {
							Query lastQery = parser.parse(last_query);
							bq.add(lastQery, Occur.MUST);
						}
						
						bq.add(query, Occur.MUST);
						
						query = bq.build();
						
					}
					else {
						if (newQuery.contains("name:") || newQuery.contains("mention:")) {
							// take MyLuceneParser
							QueryParser parser = new QueryParser("name", new MyLuceneAnalyser());
							query = parser.parse(newQuery);
						} else
							query = parser.parse(newQuery);
					}
					last_query = query.toString();

					queryResult = querySearcher.searchAll(query);
				}
			}

			// NORMAL
			else {
				last_query = query.toString();
				if (query.toString().startsWith("date")) {
					queryResult = timeRangeFilter(query);
				} else {
					queryResult = querySearcher.searchAll(query);
				}
			}

		} catch (ParseException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		if (print && queryResult != null) {
			System.out.println("(" + serialCounter + ") " + query.toString() + " #:" + queryResult.length);
			printlnToConsole("(" + serialCounter + ") " + query.toString() + " #:" + queryResult.length);
		}
		
		Result result = new Result(queryResult, searcher);
		
		if (QueryHistory.isInitialized && addToQueryHistory) {
			QueryHistory history = QueryHistory.getInstance();
			if (type.length() > 2) {
				history.addQuery(type + "\t" + query.toString());
			} else {
				history.addQuery(query.toString());
			}
			queryHistory.add(query);
			addnewQueryResult(result, query);
		}
		
		if (addToQueryHistory)
			last_result = result;
		
		return result;
	}

	
	private ScoreDoc[] timeRangeFilter(Query query) {
		
		if (last_result == null || last_result.getData() == null) {
			return querySearcher.searchAll(query);
		}
		
		long from = normalizeDate(Long.parseLong(getRangeFromQuery(query)[0].trim()), 10);
		long to = normalizeDate(Long.parseLong(getRangeFromQuery(query)[1].trim()), 10);
		
		ArrayList<ScoreDoc> result = new ArrayList<>();
		
		//if selection -- get selection result
		if (MapPanelCreator.mapCon.isSelection()) {
			ArrayList<ScoreDoc> selectedDocs = new ArrayList<>();
			Query q;
			for (MyEdge x : DetailedGraphCreator.allEdges) {
				try {
					q = parser.parse("id:"+x.getId());
					ScoreDoc[] rs = querySearcher.searchAll(q);
					for (ScoreDoc doc : rs) 
						selectedDocs.add(doc);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			for (ScoreDoc x : selectedDocs) {
				long date = 0;
				Document docu;
				try {
					docu = searcher.doc(x.doc);
					date = Long.parseLong(docu.get("date"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (date >= from && date < to) {
					result.add(x);
				}
			}
		}
		else {
			for (ScoreDoc doc : last_result.getData()) {
				try {
					Document d = reader.document(doc.doc);
					long date = Long.parseLong(d.getField("date").stringValue());

					if (date > from && date < to) {
						result.add(doc);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		ScoreDoc[] out = new ScoreDoc[result.size()];
		
		return result.toArray(out);
	}
	
	
//	public ScoreDoc[] timeRangeFilter(ScoreDoc[] last_result, long from, long to) {
//		
//		// TODO go through last result and filter those in time range
//		
//		ArrayList<ScoreDoc> result = new ArrayList<>();
//		
//		for (ScoreDoc doc : last_result	 ) {
//			System.out.println();
//			try {
//				Document d = reader.document(doc.doc);
//				long date = Long.parseLong(d.getField("date").stringValue());
//				
//				if (date > from && date < to ) {
//					result.add(doc);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		ScoreDoc[] out = new ScoreDoc[result.size()];
//		return result.toArray(out);
//	}
	
	public LocalDateTime getLatestTweetDate() {
		return dt_max;
	}

	
	private String[] getRangeFromQuery(Query query) {
		String queryStr = query.toString().toLowerCase();
		queryStr = queryStr.substring("date:[".length());
		queryStr = queryStr.substring(0, queryStr.length()-1);
		
		return queryStr.split(" to ");
	}
	
	
	private long normalizeDate(long date, int precision) {
		String dateStr = ""+date;
		dateStr = dateStr.substring(0, precision);
		long out = Long.parseLong(dateStr);

		return out;
	}
	

	private ScoreDoc[] cutScoreDocs(ScoreDoc[] result) {
		if (last_result == null)
			return result;
		
		ArrayList<ScoreDoc> finding = new ArrayList<>();
		//if selection -- get selection result
		if (MapPanelCreator.mapCon.isSelection()) {
			ArrayList<ScoreDoc> selectedDocs = new ArrayList<>();
			Query q;
			for (MyEdge x : DetailedGraphCreator.allEdges) {
				try {
					q = parser.parse("id:"+x.getId());
					ScoreDoc[] rs = querySearcher.searchAll(q);
					for (ScoreDoc doc : rs) 
						selectedDocs.add(doc);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			for (ScoreDoc x : selectedDocs) {
				for (ScoreDoc y : result) {
					if (x.doc == y.doc) {
						finding.add(x);
						break;
					}
				}
			}
		}
		else {
			// NAIV --> selection / filter Find x in y
			for (ScoreDoc x : last_result.getData()) {
				for (ScoreDoc y : result) {
					if (x.doc == y.doc) {
						finding.add(x);
						break;
					}
				}
			}
		}

		ScoreDoc[] out = new ScoreDoc[finding.size()];
		for (int i = 0; i < finding.size(); i++) {
			out[i] = finding.get(i);
		}

		return out;
	}

	
	
	public ScoreDoc[] mergeScoreDocs(ScoreDoc[] result) {

		if (last_result == null)
			return result;

		ScoreDoc[] new_result = new ScoreDoc[last_result.size() + result.length];
		int i = 0;
		for (ScoreDoc doc : last_result.getData()) {
			new_result[i++] = doc;
		}
		for (ScoreDoc doc : result) {
			new_result[i++] = doc;
		}

		return new_result;
	}

	public IndexSearcher getIndexSearcher() {
		return searcher;
	}


	/**
	 * This method searches for the <code>topX</code> documents on a single
	 * lucene <code>field</code>
	 * 
	 * @param field
	 * @param topX
	 * @throws IOException
	 */
	public TermStats[] searchTopXOfField(String field, int topX) {
		String[] fields = { field };
		try {
			// TODO put into external thread!
			TermStats[] result = HighFreqTerms.getHighFreqTerms(reader, topX, fields);

//			for (TermStats ts : result) {
//				System.out.println(ts.toString());
//			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	// Geo Filter
	public Result ADDGeoQuery(double minLat, double maxLat, double minLong, double maxLong) {
		@SuppressWarnings("deprecation")
		Query query = new GeoPointInBBoxQuery(geoField, minLat, maxLat, minLong, maxLong);
		Result geoFilter = query(query, getQeryType(), true, true);

//		addnewQueryResult(geoFilter, query);

		return geoFilter;
	}

	// Time Filter
	public Result searchTimeRange(long from, long to, boolean print, boolean queryhistory) {
		Result result;
		try {
			Query query = parser.parse("date:[" + from + " TO " + to + "]");
			result = query(query, "", print, queryhistory);
			return result;
		} catch (ParseException e) {
			System.out.println("Could not Parse Date Search to Query");
			e.printStackTrace();
		}
		return null;
	}

	public Result getLastResult() {
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

	public class QueryResult implements Comparable<QueryResult> {

		private int maxChars = 20;
		private int serial;
		private Query query;
		private Result result;

		public QueryResult(Query query, Result result, int serial) {
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
			return "(" + serial + ") " + query.toString().substring(0, maxChars);
		}

		// for a tooltip
		@Override
		public String toString() {
			return "(" + serial + ") " + query.toString() + " #:" + result.size();
		}

	}

	public void showCatHisto() {
		if (catHisto == null)
			catHisto = searchTopXOfField("category", 20);

		Object[][] resulTable = new Object[catHisto.length][2];
		for (int i = 0; i < catHisto.length; i++) {
			TermStats ts = catHisto[i];
			resulTable[i][0] = ts.termtext.utf8ToString();
			resulTable[i][1] = new Integer(ts.docFreq);
		}

		Histogram histogram = Histogram.getInstance();
		histogram.setInitialData(resulTable);
		histogram.viewInitialDataSet();
//		CategoriesPart categories = CategoriesPart.getInstance();
//		categories.chnageDataSet(resulTable);
		
	}

	public void changeHistogramm(HashMap<String, HistogramEntry> histoCounter) {
		if (!Histogram.isInitialized & !CategoriesPart.isInitialized)
			return;
		Histogram histogram = Histogram.getInstance();
		histogram.changeDataSet(histoCounter);
	}
	

	/**
	 * Init the max creation date
	 * @param maxDate
	 */
	public void initMaxDate(String maxDate) {
		// maxDate = "2013-01-08 01:15:00";
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
			utc_time_max = dt_max.toEpochSecond(ZoneOffset.UTC);
			hasStopTime = true;

			System.out.println(dt_max.toEpochSecond(ZoneOffset.UTC) + " = " + dt_max.toString());
		}
	}

	/**
	 * Init the min creatino date
	 * @param minDate
	 */
	public void initMinDate(String minDate) {

//		minDate = "2013-01-07 12:42:00"; // my2k
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

			System.out.println(dt_min.toEpochSecond(ZoneOffset.UTC) + " = " + dt_min.toString());
		}
	}
	
	public void iniUserMinMaxCreationDate(String usermin, String usermax) {
		
		// MIN
		String[] datetime_String = usermin.split(" ");
		String date_Str = datetime_String[0];
		String time_Str = datetime_String[1];
		String[] date_arr = date_Str.trim().split("-");
		String[] time_arr = time_Str.trim().split(":");
		if (date_arr.length == 3 && time_arr.length == 3) {
			// DATE
			int year = Integer.parseInt(date_arr[0]);
			int month = Integer.parseInt(date_arr[1]);
			int day = Integer.parseInt(date_arr[2]);
			
			user_minDate = Date.UTC(year, month, day, 0, 0, 0);
		}
		
		//MAX
		datetime_String = usermax.split(" ");
		date_Str = datetime_String[0];
		time_Str = datetime_String[1];
		date_arr = date_Str.trim().split("-");
		time_arr = time_Str.trim().split(":");
		if (date_arr.length == 3 && time_arr.length == 3) {
			// DATE
			int year = Integer.parseInt(date_arr[0]);
			int month = Integer.parseInt(date_arr[1]);
			int day = Integer.parseInt(date_arr[2]);
			
			user_maxDate = Date.UTC(year, month, day, 0, 0, 0);
		}
		
	}

	public long getUser_minDate() {
		return user_minDate;
	}

	public long getUser_maxDate() {
		return user_maxDate;
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
			Result rs = searchTimeRange(temp_utc, utc_plus, false, false);
			tl_data.add(new TimeLineHelper(dt_temp, rs.size()));

			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}

		Time time = Time.getInstance();
		completeDataTime = tl_data;
		if (!changedTimeSeries && time !=null)
			time.changeDataSet(tl_data);
		
		last_query = "";
	}
	
	
	public ArrayList<TimeLineHelper> createTimeBins(TimeBin binsize, ArrayList<MyEdge> tweets) {
		ArrayList<TimeLineHelper> tl_data = new ArrayList<>();
		long minDate = Long.MAX_VALUE;
		long maxDate = Long.MIN_VALUE;
		if (tweets == null || tweets.size() == 0) {
			System.out.println(">>>> Result is empty");
			return tl_data;
		}
		
		// get min-max values
		for (MyEdge edge : tweets) {
			long time = edge.getUtimestamp();
			if (time > maxDate) {
				maxDate = time;
			}
			if (time < minDate) {
				minDate = time;
			}
		}
		long temp_utc = minDate;
		long stepSize = 0;
		HashMap<Long, Integer> buckets = new HashMap<>();
		// From Start Date to StopDate .. make bins and plot
		LocalDateTime dt_temp = TimeLineHelper.longTOLocalDateTime(minDate);
		
		// CREATE TIME Bins
		while (temp_utc <= maxDate) {
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
			if (stepSize == 0) {
				stepSize = utc_plus - temp_utc;
			}
			
			buckets.put(temp_utc, 0);
//			ScoreDoc[] rs = searchTimeRange(temp_utc, utc_plus, false, false);
			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}
		// ADD To Bins
		for (MyEdge edge : tweets) {
			long time = edge.getUtimestamp();
			long key = getBucket(buckets, stepSize, time);
			if (key >= 0)
				buckets.put(key, (buckets.get(key) + 1));
			else
				continue;
		}

		for (Long key : buckets.keySet()) {
			tl_data.add(new TimeLineHelper(TimeLineHelper.longTOLocalDateTime(key), buckets.get(key)));
		}

		return tl_data;
	}
	

	public ArrayList<TimeLineHelper> createTimeBins(TimeBin binsize, ScoreDoc[] result) {

		ArrayList<TimeLineHelper> tl_data = new ArrayList<>();

		long minDate = Long.MAX_VALUE;
		long maxDate = Long.MIN_VALUE;
		
		if (result.length == 0) {
			System.out.println(">>>> Result is empty");
			return tl_data;
		}

		// get min-max values
		for (ScoreDoc doc : result) {

			int docID = doc.doc;
			Document document;
			try {
				document = searcher.doc(docID);
				// System.out.println(document.getField("id").stringValue());
				long time = (document.getField("date") != null) ? Long.parseLong((document.getField("date")).stringValue()) : -1;
				if ( time == -1)
					continue;
				if (time > maxDate) {
					maxDate = time;
				}
				if (time < minDate) {
					minDate = time;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		long temp_utc = minDate;
		long stepSize = 0;
		
		HashMap<Long, Integer> buckets = new HashMap<>();
		// From Start Date to StopDate .. make bins and plot
		LocalDateTime dt_temp = TimeLineHelper.longTOLocalDateTime(minDate);
		
		// CREATE TIME Bins
		while (temp_utc <= maxDate) {
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
			if (stepSize == 0) {
				stepSize = utc_plus - temp_utc;
			}
			
			buckets.put(temp_utc, 0);
//			ScoreDoc[] rs = searchTimeRange(temp_utc, utc_plus, false, false);
			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}
		
		// ADD To Bins
		for (ScoreDoc doc : result) {
			
			int docID = doc.doc;
			Document document;
			try {
				document = searcher.doc(docID);
				long time = Long.parseLong((document.getField("date")).stringValue());
				
				long key = getBucket(buckets, stepSize,  time);
				if (key >= 0)
					buckets.put(key, ( buckets.get(key) + 1 ));
				else 
					continue;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (Long key:  buckets.keySet()) {
			tl_data.add(new TimeLineHelper( TimeLineHelper.longTOLocalDateTime(key), buckets.get(key)));
		}
		
		return tl_data;
	}
	
	
	public void showInTimeLine(ArrayList<TimeLineHelper> timeCounter) {
		Time time = Time.getInstance();
		if (time != null)
			time.changeDataSet(timeCounter);
		changedTimeSeries = true;
	}

	
	
	private long getBucket(HashMap<Long, Integer> buckets, long stepSize, long time) {
		// Go through all buckets:
		// is within key + stepSize --> return bucket key
		for (Long key : buckets.keySet()) {
			if (time >= key && time < (key+stepSize))
				return key;
		}
		return -1;
	}

	
	
	
	
	
	/**
	 * This methods creates a graph based on the resultset 
	 * @param result 
	 * @param result
	 */
	public void createGraphView(ScoreDoc[] result) {
//		GraphPanelCreator3.createGraph(result, searcher, withMention, withFollows);
//		GraphPanelCreator3.createSimpleGraph(result, searcher, withMention, withFollows);
		GeneralGraphCreator.createSimpleGraph(result, searcher, withMention, withFollows);
	}
	
	
	
	public void changeEdgeColor() {
		DetailedGraphCreator.changeEdgeColor();
	}


	/**
	 * This method creates an external mention.graphml file, to open it with
	 * other programs: For every mention (@) that is found in the result set a
	 * directed edge is created.
	 * 
	 * @param result
	 * @param clearList
	 *            booean
	 * @throws ParseException
	 */
	public void createGraphML_Mention(ScoreDoc[] result, boolean clearList, String name, File exportDir) {
		try {
			String newQuery = "(has@:true)" + " AND (" + last_query + ")";
			Query nquery = parser.parse(newQuery);
			ScoreDoc[] fusedMention = querySearcher.searchAll(nquery);

			name = name+".graphml";
			ArrayList<ScoreDoc> a = new ArrayList<>();
			
//			 GraphML_Helper.createGraphML_Mention(fusedMention, searcher,
//			 true, "/Users/michaelhundt/Desktop/"+name);
			 GraphML_Helper.createGraphML_Mention(fusedMention, searcher, true, name, exportDir);
			// GraphML_Helper.createGraphML_Mention(fusedMention, searcher,
			// true, "./graphs/"+name);

		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method creates an external mention.graphml file, to open it with
	 * other programs: For every mention (@) that is found in the result set a
	 * directed edge is created.
	 * 
	 * @param result
	 * @param clearList
	 *            booean
	 * @throws ParseException
	 */
	public void createGraphML_Retweet(ScoreDoc[] result, boolean clearList) {
		try {
			String newQuery = "(isRetweet:true)" + " AND (" + last_query + ")";
			Query nquery = parser.parse(newQuery);
			ScoreDoc[] fusedMention = querySearcher.searchAll(nquery);

			String name = "retweet" + last_query + ".graphml";
			name = name.replace(":", "_");

//			System.out.println(System.getProperty("user.dir"));
//			Files.write(Paths.get("platform:/plugin/BostonCase/graphs/test.txt"), "TEST".getBytes(), StandardOpenOption.CREATE);
			
//			GraphML_Helper.createGraphML_Mention(fusedMention, searcher, true, "/Users/michaelhundt/Desktop/"+name);
//			GraphML_Helper.createGraphML_Mention(fusedMention, searcher, true, "./graphs/"+name);

		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method creates an external domain.graphml file, to open it with
	 * other programs: For every domain that is found in the result set a domain
	 * node is created and an edge from the user node
	 * 
	 * @param result
	 * @param clearList
	 */
	public void createGraphML_Domain(ScoreDoc[] result, boolean clearList) {

	}

	
	public void createMapMarkers(ScoreDoc[] result, boolean clearList) {
		// Show on Map
		if (result != null) {
			if (clearList)
				MapPanelCreator.clearWayPoints(clearList);
			
			for (ScoreDoc entry : result) {
				int docID = entry.doc;
				try {
					Document document = searcher.doc(docID);
					// System.out.println(document.getField("id").stringValue());
					
					// no geo
					IndexableField f = document.getField("geo");
					if (f == null)
						continue;
					
					long hashgeo = (document.getField("geo")).numericValue().longValue();
					double lat = GeoPointField.decodeLatitude(hashgeo);
					double lon = GeoPointField.decodeLongitude(hashgeo);
					String id = (document.getField("id")).stringValue();
					// String type = (document.getField("type")).stringValue();
					String sentiment = (document.getField("sentiment") != null) ? (document.getField("sentiment")).stringValue() : "neu";
					String category = (document.getField("category") != null) ? (document.getField("category")).stringValue() : "other";
					int s_strength = Integer.parseInt(document.getField("pos").stringValue()) + Integer.parseInt(document.getField("neg").stringValue());
					
					Lucene l = Lucene.INSTANCE;
					if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, category, lat, lon));
					}
					else if (l.getColorScheme().equals(Lucene.ColorScheme.SENTISTRENGTH)) {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, s_strength+"", lat, lon));
					}
					else {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, sentiment, lat, lon));
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			MapPanelCreator.mapCon.setSelection(null);
			MapPanelCreator.dataChanged();
		}

	}
	
	
	/**
	 * 
	 * @param edges
	 * @param clearMap clear all WayPoints
	 */
	public void showSelectionInMap(ArrayList<MyEdge> edges, boolean clearMap) {
		if (!edges.isEmpty()) {
			if (clearMap)
				MapPanelCreator.clearWayPoints(clearMap);
			
			HashSet<GeoPosition> points = new HashSet<>();
			ArrayList<MyEdge> noGeoEdge = new ArrayList<>();
			
			for (MyEdge edge : edges) {
				String id = edge.getId();
				String senti = edge.getSentiment();
				String cate = edge.getCategory();
				double lat = edge.getLatitude();
				double lon = edge.getLongitude();
				
				String s_strength = (edge.getPos() + edge.getNeg()) + "";
				
				if (lat !=0.0 || lon != 0.0) {
					GeoPosition g = new GeoPosition(lat, lon);
					points.add(g);
					if (colorScheme.equals(Lucene.ColorScheme.CATEGORY)) {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, cate, lat, lon));
					}
					else if (colorScheme.equals(Lucene.ColorScheme.SENTISTRENGTH)) {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, s_strength, lat, lon));
					}
					else
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, senti, lat, lon));
				}
				else {
					// if no geo location: print content to console
					noGeoEdge.add(edge);
				}
				
//				String senti = "neutral";
//				if (sentiment > 0)
//					senti = "positive";
//				else if (sentiment < 0)
//					senti = "negative";
				
			}
			
			if (noGeoEdge.size() > 0) {
//				System.out.println("Edges with no geo: "+ noGeoEdge.size());
				for (MyEdge noGe : noGeoEdge	) {
					
				}
				
			}
			MapPanelCreator.zoomToBestFit(points);
		}
	}
	
	
	public void showSelectionInHistogramm(ArrayList<MyEdge> edges) {
		if (!edges.isEmpty()) {
			Histogram histogram = Histogram.getInstance();
			HashMap<String, HistogramEntry> counter = new HashMap<>();
			
			for (MyEdge edge : edges) {
				String id = edge.getId();
				String sentiment = edge.getSentiment();
				String category = edge.getCategory();
				double senti = 0.0;
				if (category == null || category.isEmpty())
					continue;
				
				if (sentiment.equals("pos"))
					senti = 1.0;
				else if (sentiment.equals("neg")) 
					senti = -1.0;
				else 
					senti = 0;
				
				double pos = edge.getPos();
				double neg = edge.getNeg();
				
				if (counter.containsKey(category)) {
					HistogramEntry entry = counter.get(category);
					entry.count();
					entry.addSentiment(senti, pos, neg);
				} else {
					HistogramEntry entry = new HistogramEntry(category);
					entry.count();
					entry.addSentiment(senti, pos, neg);
					counter.put(category, entry);
				}
				
			}
			histogram.changeDataSet(counter);
		}
	}
	
	
	
	public void showSelectionInTimeline(ArrayList<MyEdge> edges, TimeBin binsize) {

		ArrayList<TimeLineHelper> tl_data = new ArrayList<>();

		long minDate = Long.MAX_VALUE;
		long maxDate = Long.MIN_VALUE;
		
		for (MyEdge edge : edges) {
			String id = edge.getId();
			
			Date date = edge.getDate();

		}

		long temp_utc = utc_time_min;
		long stepSize = 0;
		
		HashMap<Long, Integer> buckets = new HashMap<>();
		// From Start Date to StopDate .. make bins and plot
		LocalDateTime dt_temp =  TimeLineHelper.longTOLocalDateTime(minDate);
		
		// CREATE TIME Bins
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
			if (stepSize == 0) {
				stepSize = utc_plus - temp_utc;
			}
			
			buckets.put(temp_utc, 0);
			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}
		
				
		for (Long key:  buckets.keySet()) {
			tl_data.add(new TimeLineHelper( TimeLineHelper.longTOLocalDateTime(key), buckets.get(key)));
		}

		Time time = Time.getInstance();
		if (time != null)
			time.changeDataSet(tl_data);
		changedTimeSeries = true;

	}
	
	
	public void setQeryType(String text) {
		query_type = text;
	}

	public String getQeryType() {
		return query_type;
	}

	public ArrayList<Query> getQueryHistory() {
		return queryHistory;
	}


	public Color getColor() {
		Color c;

		if (query_type.equals(Lucene.QueryTypes.ADD.toString())) {
			c = blue;
			// c = Color.BLUE;
		} else if (query_type.equals(Lucene.QueryTypes.FUSE.toString())) {
			c = green;
			// c = Color.GREEN;
		} else {
			c = grey;
			// c = Color.GRAY;
		}

		return c;
	}

	
	public void showLastResult() {

		currentPointer = Math.max(currentPointer-1, 0);
		if (queryResults.isEmpty()) {
			return;
		}

		System.out.println("Last Query: "+queryResults.get(currentPointer).toString());
		Result lastResult = queryResults.get(currentPointer).result;
		ScoreDoc[] data = lastResult.getData();
		createMapMarkers(data, true);
		changeHistogramm(lastResult.getHistoCounter());
		
		GraphCreatorThread graphThread = new GraphCreatorThread(this) {
			
			@Override
			public void execute() {
				createGraphView(data);
//				createSimpleGraphView(data);
			}
		};
		graphThread.start();
//		createGraphML_Mention(lastResult, true);
		
		if (QueryHistory.isInitialized) {
			QueryHistory history = QueryHistory.getInstance();
			history.removeLastQuery();
		}
		
		
		Time time = Time.getInstance();
		last_result = lastResult;
		last_query = queryResults.get(currentPointer).query.toString();
		TimeLineCreatorThread lilt = new TimeLineCreatorThread(this) {
			@Override
			public void execute() {
				Lucene l = Lucene.INSTANCE;
//				changeTimeLine(TimeBin.MINUTES);
				last_result.setTimeCounter(createTimeBins(TimeBin.HOURS, last_result.getData()));
				l.showInTimeLine(last_result.getTimeCounter());
			}
		};
		lilt.start();
		printlnToConsole("<< back: "+ last_query + "#:"+ lastResult.size());

	}
	
	
	public void showCurrentResult() {

		currentPointer = Math.max(currentPointer, 0);
		if (queryResults.isEmpty()) {
			return;
		}

		System.out.println("Last Query: "+queryResults.get(currentPointer).toString());
		Result lastResult = queryResults.get(currentPointer).result;
		ScoreDoc[] data = lastResult.getData();
		createMapMarkers(data, true);
		changeHistogramm(lastResult.getHistoCounter());
		
		GraphCreatorThread graphThread = new GraphCreatorThread(this) {
			
			@Override
			public void execute() {
				createGraphView(data);
			}
		};
		graphThread.start();
//		createGraphML_Mention(lastResult, true);
		
		if (QueryHistory.isInitialized) {
			QueryHistory history = QueryHistory.getInstance();
			history.removeLastQuery();
		}
		
		
		Time time = Time.getInstance();
		last_result = lastResult;
		last_query = queryResults.get(currentPointer).query.toString();
		TimeLineCreatorThread lilt = new TimeLineCreatorThread(this) {
			@Override
			public void execute() {
				Lucene l = Lucene.INSTANCE;
//				changeTimeLine(TimeBin.MINUTES);
				last_result.setTimeCounter(createTimeBins(TimeBin.HOURS, last_result.getData()));
				l.showInTimeLine(last_result.getTimeCounter());
			}
		};
		lilt.start();
		printlnToConsole("<< back: "+ last_query + "#:"+ lastResult.size());

	}
	

	public void addnewQueryResult(Result result, Query query) {
//		System.out.println("CurrentPointer: "+currentPointer + " results:size(): "+queryResults.size());
		
		// remove from current to last
		for (int i = currentPointer+1 ; i < queryResults.size(); i++) {
			queryResults.remove(i);
		}
		
		queryResults.add(new QueryResult(query, result, currentPointer));
		currentPointer = queryResults.size()-1;
		 
	}

	
	public void resetTimeLine() {
		Time time = Time.getInstance();
		if (time != null)
			time.changeDataSet(completeDataTime);
	}

	
	public void setColorScheme(String text) {
		
		if (ColorScheme.SENTIMENT.name().toLowerCase().equals(text.toLowerCase())) {
			setColorScheme(ColorScheme.SENTIMENT);
		}
		else if (ColorScheme.CATEGORY.name().toLowerCase().equals(text.toLowerCase())) {
			setColorScheme(ColorScheme.CATEGORY);
		}
		else if (ColorScheme.SENTISTRENGTH.name().toLowerCase().equals(text.toLowerCase())) {
			setColorScheme(ColorScheme.SENTISTRENGTH);
		}
		else {
			setColorScheme(ColorScheme.SENTIMENT);	// default
		}
		
	}

	public ColorScheme getColorScheme() {
		return colorScheme;
	}

	public void setColorScheme(ColorScheme colorScheme) {
		this.colorScheme = colorScheme;
	}

	public void setWithMentions(boolean selection) {
		withMention = selection;
	}

	
	public void setWithFollows(boolean selection) {
		withFollows = selection;
	}

	
	public void exporttoJSON(File exportDir, String name) {
		
		Connection c = DBManager.getConnection();
		Statement stmt;
		try {
			stmt = c.createStatement();
			StoreToJSONThread indexer = new StoreToJSONThread(this, last_result.getData(), c, stmt,  exportDir.getAbsolutePath(), name);
			indexer.start();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}
	
	
	/**
	 * 
	 * @param name of the lucene index directory
	 */
	public void reindexLastResult(String name, boolean reload, boolean temp, File exportDir)  {
		
		reIndexCount++;
		// MAP indexCount to name
		File newIndex = null;
		String ind = luceneIndex;
		String tempPath = ind.substring(0, ind.lastIndexOf("/")+1);
		// NOT temp --> take exportDir path
		if (!temp ) {
			newIndex = exportDir;
		}
		// NOT temp/ ending && temp = true  --> create temp folder
		else {
			if (!tempPath.endsWith("temp/") && temp) {
				tempPath += "temp/";
				File theDir = new File(tempPath);

				// if the directory does not exist, create it
				if (!theDir.exists()) {
					System.out.println("\tcreating directory: " + theDir.getName());
					boolean result = false;

					try {
						theDir.mkdir();
						result = true;
					} catch (SecurityException se) {
						// handle it
					}
					if (result) {
						System.out.println("\tDIR created");
					}
				}
			}

			newIndex = new File(tempPath + "" + name);
			if (!newIndex.exists()) {
				System.out.println("\tcreating directory: " + newIndex.getName());
				boolean result = false;

				try {
					newIndex.mkdir();
					result = true;
				} catch (SecurityException se) {
					// handle it
				}
				if (result) {
					System.out.println("\tDIR created");
				}
			}
			System.out.println("\tcreated directory '" + newIndex + "' ... DONE");
			printlnToConsole("\tcreated directory '"+newIndex+"' ... DONE");
		}
		
		try {
			Directory dir = FSDirectory.open(Paths.get(newIndex.getAbsolutePath()));
			
			// ## LOAD Stopwords File
			Properties prop = new Properties();
			URL url = null;
			try {
			  url = new URL("platform:/plugin/"
			    + "SocialOcean/"
			    + "stopwords/stopwords.txt");

			    } catch (MalformedURLException e1) {
			      e1.printStackTrace();
			}
			url = FileLocator.toFileURL(url);
			
			FileReader reader = new FileReader(new File(url.getPath()));
			Analyzer analyzer = new StandardAnalyzer(reader);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
//			final Progress progress = new Progress("Progress");
//			progress.fill(parent);
			
			Connection c = DBManager.getConnection();
			Statement stmt = c.createStatement();
			
			ScoreDoc[] data = last_result.getData();
			
			// if selection - get ScoreDocs of selection
			if (MapPanelCreator.mapCon.isSelection() ) {
				data = getSelectionFromScoreDocs(DetailedGraphCreator.allEdges, data);
			}
			
			ReIndexingThread indexer = new ReIndexingThread(this, data, c, stmt, writer, 
					reload, newIndex.getAbsolutePath());
			indexer.start();
			
		} catch (IOException | SQLException e1 ) {
			e1.printStackTrace();
		}
		
	}
	
	private ScoreDoc[] getSelectionFromScoreDocs(ArrayList<MyEdge> allEdges, ScoreDoc[] data) {
		
		ArrayList<ScoreDoc> result = new ArrayList<>();
		for (ScoreDoc doc : data	) {
			try {
				Document d = reader.document(doc.doc);
				String tweet_id = d.getField("id").stringValue();
				for (MyEdge e : allEdges) {
					if (e.getId().equals(tweet_id))
						result.add(doc);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ScoreDoc[] out = new ScoreDoc[result.size()];
		return result.toArray(out);
	}


	public void clearGraph() {
		
		DetailedGraphCreator.clearGraph();	
		
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				GeneralGraphCreator.clearGraph();
			}
		});
		
	}
	
	
	public void initCountriesMap() {
		DATACHANGED = true;
		SHOWCountries = false;
		
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
//		    	SettingsPart.selectCountries(false);
		    	SettingsPart.enableCountries(true);
		    }
		});
		
//		DATACHANGED = true;
//		SHOWCountries = false;
////		MapPanelCreator.mapCon.
//		
//		Display.getDefault().asyncExec(new Runnable() {
//		    public void run() {
//		    	SettingsPart.selectCountries(false);
////		    	SettingsPart.enableCountries(false);
//		    }
//		});
//		
//		Thread initCountries = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				MapPanelCreator.mapCon.clearCountries();
//				MapPanelCreator.mapCon.getCountries(MapPanelCreator.getZoomLevel());
//			}
//		}, "InitCountries");
//		initCountries.start();
		
	}


	public <V, E> void showDetailsOfSelection(Collection<V> nodes, Collection<E> edges, boolean clear) {
		DATACHANGED = true;
		DetailedGraphCreator.createDetailGraph(nodes, edges, withMention, withFollows, clear);
		
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	SettingsPart.selectCountries(false);
		    }
		});
		
	}
	
	


	public void showSelectionInMap() {
		showClustersInMap(DetailedGraphCreator.allUser, DetailedGraphCreator.allEdges);
	}
	


	public void showClustersInMap(ArrayList<MyUser> allUser, ArrayList<MyEdge> allEdges) {
		
		if (SHOWUser && SHOWTweet) {
			// paint both
			ArrayList<Object> merged = new ArrayList<>();
			for (MyUser u : allUser)
				merged.add(u);
			for (MyEdge e : allEdges)
				merged.add(e);
			
			
			if (SHOWHeatmap) {
				MapPanelCreator.mapCon.resetGridCells();
				MapPanelCreator.mapCon.setSelection(merged);
				MapPanelCreator.dataChanged();
			}
			if (SHOWCountries) {
				MapPanelCreator.mapCon.resetCountry();
				MapPanelCreator.mapCon.setSelection(merged);
				MapPanelCreator.dataChanged();
			}
			return;
		}
		
		// User locations or Edges locations?
		if (SHOWUser) {
			if (allEdges.isEmpty() && allUser.isEmpty()) {
				// take last_result
				MapPanelCreator.mapCon.setSelection(null);
				showCurrentResult();
				return;
			}
			if (SHOWHeatmap) {
				MapPanelCreator.mapCon.resetGridCells();
				MapPanelCreator.mapCon.setSelection(allUser);
				MapPanelCreator.dataChanged();
			}
			if (SHOWCountries) {
				MapPanelCreator.mapCon.clearCountries();
				MapPanelCreator.mapCon.setSelection(allUser);
				MapPanelCreator.dataChanged();
			}
		}
		
		else if (SHOWTweet) {
			if (allEdges.isEmpty() && allUser.isEmpty()) {
				MapPanelCreator.mapCon.setSelection(null);
				showCurrentResult();
				return;
			}
			if (SHOWHeatmap) {
				MapPanelCreator.mapCon.resetGridCells();
				MapPanelCreator.mapCon.setSelection(allEdges);
				MapPanelCreator.dataChanged();
			}
			if (SHOWCountries) {
				MapPanelCreator.mapCon.clearCountries();
				MapPanelCreator.mapCon.setSelection(allEdges);
				MapPanelCreator.dataChanged();
			}
		}
	}


	public void takeScreenshot(String name, Rectangle appBounds, String path) {
		
		try {
			BufferedImage image = null;
			if ( appBounds == null)
				image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			else 
				image = new Robot().createScreenCapture(appBounds);
			
			if (image != null)
				ImageIO.write(image, "png", new File(path+"/"+name+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
	}


	public void setOnlyGeo(boolean onlyGeo) {
		this.ONLY_GEO = onlyGeo;
	}

	public boolean isOnlyGeo() {
		return ONLY_GEO;
	}
}
