package utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.table.DefaultTableModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import bostoncase.parts.CategoriesPart;
import bostoncase.parts.Console;
import bostoncase.parts.Histogram;
import bostoncase.parts.LuceneStatistics;
import bostoncase.parts.QueryHistory;
import bostoncase.parts.Time;
import bostoncase.parts.TopSelectionPart;
import impl.GraphCreatorThread;
import impl.GraphML_Helper;
import impl.GraphPanelCreator3;
import impl.LuceneIndexLoaderThread;
import impl.LuceneQuerySearcher;
import impl.MapPanelCreator;
import impl.MyEdge;
import impl.TimeLineCreatorThread;
import interfaces.ILuceneQuerySearcher;

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

	private Connection con = null;

	// START TIME
	// private PreparedStatement pre_statement_min;
	// private PreparedStatement pre_statement_max;

	private LocalDateTime dt_min = null;
	private long utc_time_min;
	public boolean hasStartTime = false;
	private LocalDateTime dt_max = null;
	private long utc_time_max;
	public boolean hasStopTime = false;
	private ArrayList<TimeLineHelper> completeDataTime = new ArrayList<>();

	private ScoreDoc[] last_result = null;
	private String last_query = "";
	private String query_type = "";

	private MultiFieldQueryParser mq = null;
	
	private static int reIndexCount = 0;

	public static enum TimeBin {
		SECONDS, MINUTES, HOURS, DAYS
	}
	
	public static enum ColorScheme {
		SENTIMENT, CATEGORY
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

	// public static void main(String[] args) {
	// String formattedString = String.format("%s \t\t %.2f \t %d %s", "Test",
	// 1*100/(float)4, 15, "%");
	// System.out.println(formattedString);
	// }

	public int serialCounter = 0;
	public boolean isInitialized = false;
	private boolean changedTimeSeries;
	private boolean withMention = true;
	private boolean withFollows = true;
	private String luceneIndex;
	private long user_minDate;
	private long user_maxDate;
	
	public void initLucene( String index, ILuceneQuerySearcher querySearcher) {
		
		luceneIndex = index;
		
		try {
			
			// ### JSch ###
//			JSch jsch = new JSch();
//			Session session = jsch.getSession("socialocean", "charon01.inf.uni-konstanz.de", 2212);
//			// username and password will be given via UserInfo interface.
//			session.setPassword(pass);
//			session.connect(); 		// throws exception
//
//			Channel channel = session.openChannel("sftp");
//			channel.connect();
//			ChannelSftp c = (ChannelSftp) channel;
//			
//			String h = c.getHome();
			
			

			// ### commons-vfs2 ###
			// FileSystemOptions fsOptions = new FileSystemOptions();
			// SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions,
			// "no");
			// FileSystemManager fsManager = VFS.getManager();
			//// sftp://socialocean@charon01.inf.uni-konstanz.de:2212/public/LuceneIndex/lucene_index
			// String uri = "sftp://socialocean@charon01.inf.uni-konstanz.de:2212/public/LuceneIndex/lucene_index";
//			FileObject fo = fsManager.resolveFile(uri, fsOptions);
			
//			URL url = new URL ("ftp://socialocean:"+pass+"@charon01.inf.uni-konstanz.de/LuceneIndex/lucene_index");
//			URLConnection urlc = url.openConnection();
//			InputStream is = urlc.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is);
//			String path = urlc.getURL().getPath();
//			URI path2 = url.toURI();
//			reader = DirectoryReader.open(FSDirectory.open(Paths.get(path2)));
			
			
//			### commons-net  -- doesn't work
//			FTPSClient client = new FTPSClient();
//			
//			client.connect("charon01.inf.uni-konstanz.de", 2212);
//			client.login("socialocean", pass);
//			
//			if (client.isConnected()) {
//                // Obtain a list of filenames in the current working
//                // directory. When no file found an empty array will
//                // be returned.
//                String[] names = client.listNames();
//                for (String name : names) {
//                    System.out.println("Name = " + name);
//                }
//                
//                FTPFile[] ftpDirs = client.listDirectories();
//                FTPFile[] ftpFiles = client.listFiles();
//                for (FTPFile ftpFile : ftpFiles) {
//                    // Check if FTPFile is a regular file
//                    if (ftpFile.getType() == FTPFile.FILE_TYPE) {
//                        System.out.println("FTPFile: " + ftpFile.getName() +
//                                "; " + FileUtils.byteCountToDisplaySize(
//                                ftpFile.getSize()));
//                    }
//                }
//            }
//            client.logout();
			
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

			// pre_statement_min = con.prepareStatement("Select creationdate
			// from tweetdata order by creationdate ASC Limit 1");
			// pre_statement_max = con.prepareStatement("Select creationdate
			// from tweetdata order by creationdate DESC Limit 1");

		} catch (IOException e) {
			System.out.println("Could not create LuceneSearcher, path to index not found " + luceneIndex);
			e.printStackTrace();
			return;
		} catch (Exception e) {
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
		
//		if (Console.isInitialized) {
//			Console console = Console.getInstance();
//			console.clear();
//		}
		
		queryHistory.clear();
		last_query = "";
		currentPointer = 0;
		queryResults.clear();
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
	public ScoreDoc[] query(Query query, String type, boolean print, boolean addToQueryHistory) {
		serialCounter++;
		ScoreDoc[] result = null;

		// ADD
		try {
			if (type.equals("ADD") && !last_query.isEmpty()) {
				String newQuery = "";
				if (query.toString().startsWith("GeoPointInBBoxQuery")
						|| last_query.startsWith("GeoPointInBBoxQuery")) {
					// merge the both results by hand
					// ArrayList<Query> last_two = new ArrayList<>();
					// last_two.add(queryHistory.get(queryHistory.size()-1));
					// last_two.add(query);
					// DisjunctionMaxQuery union = new
					// DisjunctionMaxQuery(last_two, 0);
					// result = querySearcher.searchAll(union);
					result = querySearcher.searchAll(query);
					result = mergeScoreDocs(result);
				}
				// Time Selection
				else if (query.toString().startsWith("date")) {
					timeRangeFilter(query);
				}

				else {
					newQuery = query.toString() + " OR (" + last_query + ")";
					query = parser.parse(newQuery);
					result = querySearcher.searchAll(query);
				}

			}
			// FUSE
			else if (type.equals("FUSE") && !last_query.isEmpty()) {
				String newQuery = "";
				if (query.toString().startsWith("GeoPointInBBoxQuery")
						|| last_query.startsWith("GeoPointInBBoxQuery")) {
					// FUSE and Geo is a selection!
					// 1) case: last_query is empty --> nothing to FUSE, we are
					// not here
					// 2) case: last_result AND new_result -- get the CUT
					// manually when fields overlap!
					// -- AND

					result = querySearcher.searchAll(query);
					if (result.length < 100000 || last_result.length < 100000)
						result = cutScoreDocs(result);

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

				// TODO FUSE -- when sources types are different
				// TODO FUSE --

				else {
					newQuery = "(" + query.toString() + ")" + " AND (" + last_query + ")";
					query = parser.parse(newQuery);
					last_query = query.toString();
					result = querySearcher.searchAll(query);
				}

				// System.out.println("FUSE: \nLastQuery: "+
				// queryHistory.get(queryHistory.size()-1).toString() +""
				// + "\nQuery: "+ query.toString());
				//
				// Builder bq = new Builder();
				// bq.add(queryHistory.get(queryHistory.size()-1), Occur.MUST);
				// bq.add(query, Occur.FILTER);
				// result = querySearcher.searchAll(bq.build());

			}

			// NORMAL
			else {
				last_query = query.toString();
				if (query.toString().startsWith("date")) {
					result = timeRangeFilter(query);
				} else {
					result = querySearcher.searchAll(query);
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (print && result != null) {
			System.out.println("(" + serialCounter + ") " + query.toString() + " #:" + result.length);
			printToConsole("(" + serialCounter + ") " + query.toString() + " #:" + result.length);
		}

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
		
		if (last_result == null) {
			return querySearcher.searchAll(query);
		}
		
		long from = normalizeDate(Long.parseLong(getRangeFromQuery(query)[0].trim()), 10);
		long to = normalizeDate(Long.parseLong(getRangeFromQuery(query)[1].trim()), 10);
		
		// TODO go through last result and filter those in time range
		
		ArrayList<ScoreDoc> result = new ArrayList<>();
		
		for (ScoreDoc doc : last_result	) {
			try {
				Document d = reader.document(doc.doc);
				long date = Long.parseLong(d.getField("date").stringValue());
				
				if (date > from && date < to ) {
					result.add(doc);
				}
			} catch (IOException e) {
				e.printStackTrace();
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

		// NAIV --> selection / filter Find x in y

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

	public ScoreDoc[] mergeScoreDocs(ScoreDoc[] result) {

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

	public IndexSearcher getIndexSearcher() {
		return searcher;
	}

	public void FUSEQueries(ArrayList<Integer> queryIndexes) {

		// fuse queries from the query result array
		// delete the indexes - add a new, inc serial
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
		ScoreDoc[] geoFilter = query(query, getQeryType(), true, true);

//		addnewQueryResult(geoFilter, query);

		return geoFilter;
	}

	// Time Filter
	public ScoreDoc[] searchTimeRange(long from, long to, boolean print, boolean queryhistory) {
		ScoreDoc[] result;
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

	public class QueryResult implements Comparable<QueryResult> {

		private int maxChars = 20;
		private int serial;
		private Query query;
		private ScoreDoc[] result;

		public QueryResult(Query query, ScoreDoc[] result, int serial) {
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
			return "(" + serial + ") " + query.toString() + " #:" + result.length;
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

		// TODO Histogram Part must be created!
		Histogram histogram = Histogram.getInstance();
//		CategoriesPart categories = CategoriesPart.getInstance();

		histogram.setInitialData(resulTable);
		histogram.viewInitialDataSet();
//		categories.chnageDataSet(resulTable);
		
	}

	public void changeHistogramm(ScoreDoc[] data) {

		if (!Histogram.isInitialized & !CategoriesPart.isInitialized)
			return;

		Histogram histogram = Histogram.getInstance();
//		CategoriesPart categories = CategoriesPart.getInstance();
//		HashMap<String, Integer> counter = new HashMap<>();
		HashMap<String, HistogramEntry> counter = new HashMap<>();


		for (ScoreDoc doc : data) {
			Document document = null;
			try {
				document = searcher.doc(doc.doc);
			} catch (IOException e) {
				continue;
			}
			String field = "";
			double sentiment = 0.0;

			if ((document.getField("category")) == null)
				continue;

			field = (document.getField("category")).stringValue();
//			String sentiment_str = (document.getField("sentiment")).stringValue();
//			if (sentiment_str.equals("positive"))
//				sentiment = 1.0;
//			else if (sentiment_str.equals("negative")) 
//				sentiment = -1.0;
//			else 
//				sentiment = 0;
			
			String sentiment_str = (document.getField("sentiment")).stringValue();
			if (sentiment_str.equals("pos"))
				sentiment = 1.0;
			else if (sentiment_str.equals("neg")) 
				sentiment = -1.0;
			else 
				sentiment = 0;

			if (counter.containsKey(field)) {
				HistogramEntry category = counter.get(field);
				category.count();
				category.addSentiment(sentiment);
			} else {
				HistogramEntry category = new HistogramEntry(field);
				category.count();
				category.addSentiment(sentiment);
				counter.put(field, category);
			}
		}

//		int size = counter.keySet().size();
//		Object[][] resulTable = new Object[size][2];
//		int i = 0;
//		for (String key : counter.keySet()) {
//			resulTable[i][0] = key;
//			resulTable[i][1] = counter.get(key);
//			i++;
//		}
		
		histogram.changeDataSet(counter);
		
//		histogram.chnageDataSet(resulTable);
//		categories.chnageDataSet(resulTable);
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
		
//		user_maxDate = Date.UTC(2013, 04, 24, 19, 0, 0);
//		user_minDate = Date.UTC(2006, 03, 21, 0, 0, 0);
		
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
			ScoreDoc[] rs = searchTimeRange(temp_utc, utc_plus, false, false);
			tl_data.add(new TimeLineHelper(dt_temp, rs.length));

			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}

		Time time = Time.getInstance();
		completeDataTime = tl_data;
		if (!changedTimeSeries)
			time.changeDataSet(tl_data);

	}
	
	
	
	

	public void changeTimeLine(TimeBin binsize) {

		ArrayList<TimeLineHelper> tl_data = new ArrayList<>();

		// TODO Get New MIN - MAX
		// TODO always fuse the result with last_result ( TEST!! )
		// Or by hand .. put result into Buckets (after min max ) -> create
		// buckets ... print
		long minDate = Long.MAX_VALUE;
		long maxDate = Long.MIN_VALUE;
		
		if (last_result.length == 0) {
			System.out.println(">>>> Result is empty");
			return;
		}

		// get min-max values
		for (ScoreDoc doc : last_result) {

			int docID = doc.doc;
			Document document;
			try {
				document = searcher.doc(docID);
				// System.out.println(document.getField("id").stringValue());
				long time = Long.parseLong((document.getField("date")).stringValue());
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
//		long temp_utc = utc_time_min;
		long stepSize = 0;
		
		HashMap<Long, Integer> buckets = new HashMap<>();
		// From Start Date to StopDate .. make bins and plot
		LocalDateTime dt_temp = longTOLocalDateTime(minDate);
		
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
		
		
//		while (temp_utc <= utc_time_max) {
//			LocalDateTime dt_plus = dt_temp;
//			switch (binsize) {
//			case SECONDS:
//				dt_plus = dt_temp.plusSeconds(1);
//				break;
//			case MINUTES:
//				dt_plus = dt_temp.plusMinutes(1);
//				break;
//			case HOURS:
//				dt_plus = dt_temp.plusHours(1);
//				break;
//			case DAYS:
//				dt_plus = dt_temp.plusDays(1);
//				break;
//			}
//			long utc_plus = dt_plus.toEpochSecond(ZoneOffset.UTC);
//			if (stepSize == 0) {
//				stepSize = utc_plus - temp_utc;
//			}
//			
//			buckets.put(temp_utc, 0);
//			
////			ScoreDoc[] rs = searchTimeRange(temp_utc, utc_plus, false, false);
//			dt_temp = dt_plus;
//			temp_utc = utc_plus;
//		}
		
		
		// ADD To Bins
		for (ScoreDoc doc : last_result) {
			
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
//			buckets.put(key, randomVal);
			tl_data.add(new TimeLineHelper(longTOLocalDateTime(key), buckets.get(key)));
		}

		Time time = Time.getInstance();
		time.changeDataSet(tl_data);
		changedTimeSeries = true;

	}

	
	
	private long getBucket(HashMap<Long, Integer> buckets, long stepSize, long time) {
		// TODO Go through all buckets:
		// is within key + stepSize --> return bucket key
		for (Long key : buckets.keySet()) {
			if (time >= key && time < (key+stepSize))
				return key;
		}
		return -1;
	}

	private LocalDateTime longTOLocalDateTime(long minDate) {
		
//		LocalDateTime dt = LocalDateTime.of(date, time);
////		System.out.println(dt.toEpochSecond(ZoneOffset.UTC));
//		long utc_time = dt.toEpochSecond(ZoneOffset.UTC);
		LocalDateTime time = LocalDateTime.ofEpochSecond(minDate, 0, ZoneOffset.UTC);
		
		return time;
	}
	
	
	/**
	 * This methods creates a graph based on the resultset 
	 * @param result
	 */
	public void createGraphView() {
		ScoreDoc[] result = last_result;
		
		GraphPanelCreator3.createGraph(result, searcher, withMention, withFollows);
		
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
	public void createGraphML_Mention(ScoreDoc[] result, boolean clearList) {
		try {
			String newQuery = "(has@:true)" + " AND (" + last_query + ")";
			Query nquery = parser.parse(newQuery);
			ScoreDoc[] fusedMention = querySearcher.searchAll(nquery);

//			String name = "mention" + last_query + ".graphml";
//			name = name.replace(":", "_");
			String name = "mention_graph.graphml";
			
			ArrayList<ScoreDoc> a = new ArrayList<>();
			
//			 GraphML_Helper.createGraphML_Mention(fusedMention, searcher,
//			 true, "/Users/michaelhundt/Desktop/"+name);
			 GraphML_Helper.createGraphML_Mention(fusedMention, searcher,true, name);
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
	 *            booean
	 */
	public void createGraphML_Domain(ScoreDoc[] result, boolean clearList) {

	}

	public void showInMap(ScoreDoc[] result, boolean clearList) {
		// Show on Map
		if (result != null) {
			if (clearList)
				MapPanelCreator.clearWayPoints(clearList);

			if (result.length > 17000) {

				// Cluster -- Do something: -- LEVEL of Detail <---> Zoom
				// Or only show those that I see on the Display
				System.out.println("( to many )");
				return;
			}

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
					// double sentiment =
					// Double.parseDouble((document.getField("sentiment")).stringValue());
					// MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(docID
					// + "", sentiment, lat, lon));
					String sentiment = (document.getField("sentiment")).stringValue();
					String category = (document.getField("category")).stringValue();
					
					Lucene l = Lucene.INSTANCE;
					if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, category, lat, lon));
					}
					else {
						MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, sentiment, lat, lon));
					}

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			MapPanelCreator.showWayPointsOnMap();
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
			
			for (MyEdge edge : edges) {
				String id = edge.getId();
//				double sentiment = edge.getSentiment();
				String senti = edge.getSentiment();
				double lat = edge.getLatitude();
				double lon = edge.getLongitude();
//				String senti = "neutral";
//				if (sentiment > 0)
//					senti = "positive";
//				else if (sentiment < 0)
//					senti = "negative";
					
				MapPanelCreator.addWayPoint(MapPanelCreator.createTweetWayPoint(id, senti, lat, lon));
			}
			MapPanelCreator.showWayPointsOnMap();
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
				
				if (counter.containsKey(category)) {
					HistogramEntry entry = counter.get(category);
					entry.count();
					entry.addSentiment(senti);
				} else {
					HistogramEntry entry = new HistogramEntry(category);
					entry.count();
					entry.addSentiment(senti);
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
		LocalDateTime dt_temp = longTOLocalDateTime(minDate);
		
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
			
//			ScoreDoc[] rs = searchTimeRange(temp_utc, utc_plus, false, false);
			dt_temp = dt_plus;
			temp_utc = utc_plus;
		}
		
				
		for (Long key:  buckets.keySet()) {
//			buckets.put(key, randomVal);
			tl_data.add(new TimeLineHelper(longTOLocalDateTime(key), buckets.get(key)));
		}

		Time time = Time.getInstance();
		time.changeDataSet(tl_data);
		changedTimeSeries = true;

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
		System.out.println("Last Query: "+queryResults.get(currentPointer).toString());

		ScoreDoc[] lastResult = queryResults.get(currentPointer).result;
		showInMap(lastResult, true);
		changeHistogramm(lastResult);
		
		GraphCreatorThread graphThread = new GraphCreatorThread(this) {
			
			@Override
			public void execute() {
				createGraphView();
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
				changeTimeLine(TimeBin.HOURS);
//				changeTimeLine(TimeBin.MINUTES);
			}
		};
		lilt.start();
		printToConsole("<< back: "+ last_query + "#:"+ lastResult.length);

	}

	public void addnewQueryResult(ScoreDoc[] result, Query query) {
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
		time.changeDataSet(completeDataTime);
	}

	
	public void setColorScheme(String text) {
		
		if (ColorScheme.SENTIMENT.name().toLowerCase().equals(text.toLowerCase())) {
			setColorScheme(ColorScheme.SENTIMENT);
		}
		else if (ColorScheme.CATEGORY.name().toLowerCase().equals(text.toLowerCase())) {
			setColorScheme(ColorScheme.CATEGORY);
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

	public void reindexLastResult(String name) {
		
		reIndexCount++;
		// MAP indexCount to name
		
		String ind = luceneIndex;
		
		String tempPath = ind.substring(0, ind.lastIndexOf("/")+1);
		
		if (!tempPath.endsWith("temp/")) {
			tempPath +="temp";
			File theDir = new File(tempPath+"temp");

			// if the directory does not exist, create it
			if (!theDir.exists()) {
			    System.out.println("creating directory: " + theDir.getName());
			    boolean result = false;

			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			        //handle it
			    }        
			    if(result) {    
			        System.out.println("DIR created");  
			    }
			}
		}
		
		
		
		
		File newIndex = new File(tempPath+"/"+name);
		if (!newIndex.exists()) {
		    System.out.println("creating directory: " + newIndex.getName());
		    boolean result = false;

		    try{
		    	newIndex.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
		
		System.out.println("Indexing to directory '" + newIndex + "'...");
		
		try {
			Directory dir = FSDirectory.open(Paths.get(newIndex.getAbsolutePath()));
			
			// ## LOAD Stopwords File
			Properties prop = new Properties();
			URL url = null;
			try {
			  url = new URL("platform:/plugin/"
			    + "BostonCase/"
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
			
			for (ScoreDoc doc : last_result) {
				Document document = null;
				try {
					document = searcher.doc(doc.doc);
					
					// get content .. add content
					// fulltext
					String id = document.get("id");
					String content = getContent(id);
					content = content.replaceAll("\"", "");
					TextField content_field = new TextField("content", content, Field.Store.NO);
					document.add(content_field);
					
				} catch (IOException e) {
					continue;
				}
				writer.addDocument(document);
			}
			
			writer.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// TODO store mapping (index to foldername
		// re-init Lucene with new created Lucene Index
		// Add a dialog, to enable to open LuceneIndex files
		
		LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
		LuceneIndexLoaderThread lilt = new LuceneIndexLoaderThread(this, false, false) {
			@Override
			public void execute() {
				System.out.println("Loading Lucene Index ...");
				initLucene( newIndex.getAbsolutePath(), lqs);
			}
		};
		lilt.start();
		
	}
	
	
	private String getContent(String tweetid) {
		String content = "";
		Connection c = DBManager.getConnection();
    	try {
			Statement stmt = c.createStatement();
			String table = DBManager.getTweetdataTable();
			
//			String query = "select t.\"tweetScreenName\", t.\"tweetContent\", t.creationdate, t.sentiment, t.category, t.\"containsUrl\"  from "+table+" as t where t.tweetid = "+text;
			String query = "select t.tweet_content from "+table+" as t where t.tweet_id = "+tweetid;
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				content = rs.getString("tweet_content");
			}
			
			stmt.close();
			c.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
    	
    	
    	return content;
	}

	public void clearGraph() {
		GraphPanelCreator3.clearGraph();		
	}


}
