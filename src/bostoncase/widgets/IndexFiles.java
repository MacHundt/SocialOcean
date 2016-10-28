package bostoncase.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class IndexFiles {

	private static String DELIMITER = "\t";
	private static String TWITTER = "twitter";
	private static String FLICKR = "flickr";
	private static String CRIME = "crime_report";
	

	private IndexFiles() {
	}

	/** Index all text files under a directory. */
	public static void main(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles" + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "index";
		String docsPath = null;
		
		boolean create = true;	// create new Index
		
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		if (docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			
			FileReader reader = new FileReader(new File("./stopwords/stopwords.txt"));
			Analyzer analyzer = new StandardAnalyzer(reader);
			
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);

			writer.close();

			Date end = new Date();
			System.out.println();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
			System.out.println("END");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}

	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
		
		System.out.println(file.getFileName());
		if (file.getFileName().endsWith(".DS_Store"))
			return;
		
		String type = "";
		if (file.getFileName().toString().toLowerCase().contains("flickr"))
			type = FLICKR;
		else if (file.getFileName().toString().toLowerCase().contains("crime"))
			type = CRIME;
		else
			type = TWITTER;
		
		
		
//		if (file.getFileName().endsWith(other))
		

		try (InputStream stream = Files.newInputStream(file)) {
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader br = new BufferedReader(reader);

			// header --> get index
			String line_string =  br.readLine();
			String[] header = line_string.split(DELIMITER);
			
			if (header.length < 5) {
				if (DELIMITER.equals(";"))
					DELIMITER = "\t";
				else if (DELIMITER.equals("\t"))
					DELIMITER = ";";
				header = line_string.split(DELIMITER);
			}
			if (header.length < 5) 
				return;
			
			int id_index = -1;
			int time_index = -1;
			int longitude_index = -1;
			int latitude_index = -1;
			int tag_index = -1;
			ArrayList<Integer> content_index = new ArrayList<>();

			for (int i = 0; i < header.length; i++) {
				String s = header[i];
				if (type.equals(TWITTER)) {
					if (s.equals("\"tweetid\"") || s.equals("tweetid") || s.equals("TWEET_ID")) {
						id_index = i;
					} else if (s.equals("\"tweetContent\"") || s.equals("\"place\"") || 
							s.equals("tweetContent") || s.equals("place") || s.equals("TWEET_CONTENT") || s.equals("TWEET_PLACE") ) {
						content_index.add(i);
					} else if (s.equals("\"creationdate\"") || s.equals("creationdate") || s.equals("TWEET_CREATIONDATE")) {
						time_index = i;
					} else if (s.equals("\"latitude\"") || s.equals("latitude") || s.equals("LATITUDE")) {
						latitude_index = i;
					} else if (s.equals("\"longitude\"") || s.equals("longitude") || s.equals("LONGITUDE")) {
						longitude_index = i;
					} 
				}
				else if (type.equals(FLICKR)) {
					if (s.equals("photoID") || s.equals("\"photoID\"")) {
						id_index = i;
					} else if (s.equals("photoTitle") || s.equals("photoTags") || s.equals("photoDescription") ||
							s.equals("\"photoTitle\"") || s.equals("\"photoTags\"") || s.equals("\"photoDescription\"")) {
						content_index.add(i);
					} else if (s.equals("photoDateTaken") || s.equals("\"photoDateTaken\"")) {
						time_index = i;
					} else if (s.equals("photoLatitude") || s.equals("\"photoLatitude\"")) {
						latitude_index = i;
					} else if (s.equals("photoLongitude") || s.equals("\"photoLongitude\"")) {
						longitude_index = i;
					}
					if  (s.equals("photoTags") || s.equals("\"photoTags\"")) {
						tag_index = i;
					}
				}
				else if (type.equals(CRIME)) {
					if (s.equals("COMPNOS") || s.equals("\"COMPNOS\"")) {
						id_index = i;
					} else if (s.equals("INCIDENT_TYPE_DESCRIPTION") || s.equals("WEAPONTYPE")  ||
							s.equals("\"INCIDENT_TYPE_DESCRIPTION\"") || s.equals("\"WEAPONTYPE\"") ) {
						content_index.add(i);
					} else if (s.equals("FROMDATE") || s.equals("\"FROMDATE\"")) {
						time_index = i;
					} else if (s.equals("LONG") || s.equals("\"LONG\"")) {
						latitude_index = i;
					} else if (s.equals("LAT") || s.equals("\"LAT\"")) {
						longitude_index = i;
					} 
				}
			}

			// make a new, empty document
			Document doc = new Document();

			int counterLines = 0;
			while (true) {
				doc.clear();
				String line = br.readLine();
				if (line == null)
					break;
				
				String[] line_arr = line.toLowerCase().split(DELIMITER);
				if (line_arr.length != header.length)
					continue;
				
				counterLines ++;
				
				// ID
				String id =  line_arr[id_index];
				doc.add(new StringField("id", id, Field.Store.YES));
				
				
				// TYPE
				Field typeField = new StringField("type", type, Field.Store.YES);
				doc.add(typeField);
				
				
				// Add the path of the file as a field named "path". Use a
				// field that is indexed (i.e. searchable), but don't tokenize
				// the field into separate words and don't index term frequency
				// or positional information:
				Field pathField = new StringField("path", file.toString(), Field.Store.YES);
				doc.add(pathField);

				// Add the last modified date of the file a field named
				// "modified".
				// Use a LongPoint that is indexed (i.e. efficiently filterable
				// with
				// PointRangeQuery). This indexes to milli-second resolution,
				// which
				// is often too fine. You could instead create a number based on
				// year/month/day/hour/minutes/seconds, down the resolution you
				// require.
				// For example the long value 2011021714 would mean
				// February 17, 2011, 2-3 PM.
//				doc.add(new LongPoint("modified", lastModified) );

				// TEXT
				// Add the contents of the file to a field named "contents".
				// Specify
				// a Reader,
				// so that the text of the file is tokenized and indexed, but
				// not
				// stored.
				// Note that FileReader expects the file to be in UTF-8
				// encoding.
				// If that's not the case searching for special characters will
				// fail.
				// doc.add(new TextField("contents", new BufferedReader(new
				// InputStreamReader(stream, StandardCharsets.UTF_8))));
				String text_content = "";
				for (int i : content_index) {
					text_content += line_arr[i] + " ";
				}
				if (text_content.isEmpty()) {
					text_content = "null";
				}
				
				text_content = text_content.replaceAll("\"", "");
				TextField content_field = new TextField("content", text_content, Field.Store.NO);
				doc.add(content_field);
				
				String tag_content = text_content;
				if (type.equals(FLICKR) ) {
					tag_content = line_arr[tag_index];
					tag_content = tag_content.replaceAll(" -", "").trim();
				}
				else if (type.equals(TWITTER)) {
					//get #tags :)
					
					tag_content = getTagsFromTweets(text_content);
				}
				else if (type.equals(CRIME)) {
					tag_content = "";
				}
				TextField tag_field = new TextField("tags", tag_content, Field.Store.NO);
				doc.add(tag_field);
				

				// GEO
				// classes to search
				// GeoPointDistanceQuery
				// GeoPointInBBoxQuery
				// GeoPointInPolygonQuery
				double longi = 0;
				double lati = 0;
				try {
					longi = Double.parseDouble(line_arr[longitude_index].trim());
					lati = Double.parseDouble(line_arr[latitude_index].trim());
				} catch(NumberFormatException nfe) {
					System.out.println("could not parse LAT:"+ line_arr[latitude_index].trim() +" LONG:"+ line_arr[longitude_index].trim() );
					continue;
				} catch(NullPointerException npe) {
					System.err.println(npe.getStackTrace());
					continue;
				}
				// OUT of Geo-Bounds
				if (longi == 0.0 || lati == 0.0 )
					continue;
				
				GeoPointField geo = new GeoPointField("geo", lati, longi, Field.Store.YES);
				doc.add(geo);

				// TIME
				String[] datetime_String = line_arr[time_index].split("t");
				if (datetime_String.length<2)
					datetime_String = line_arr[time_index].split(" ");
				String date_Str = datetime_String[0];
				String time_Str = datetime_String[1];
				
				if (date_Str.charAt(0) == '"')
					date_Str = date_Str.substring(1);
				
				if (time_Str.length() > 8 && time_Str.charAt(8) == '"')
					time_Str = time_Str.substring(0,8);
				
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
				
					LocalDateTime dt = LocalDateTime.of(date, time);
//					System.out.println(dt.toEpochSecond(ZoneOffset.UTC));
					
					long utc_time = dt.toEpochSecond(ZoneOffset.UTC);
//					String date_str = DateTools.dateToString(dt, Resolution.SECOND);
					doc.add(new StringField("date", ""+utc_time , Field.Store.YES ));
					
				} else {
					continue;
				}
				

				if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
					// New index, so we just add the document (no old document
					// can be there):
//					System.out.println("adding " + file);
					writer.addDocument(doc);
				} else {
					// Existing index (an old copy of this document may have
					// been indexed) so
					// we use updateDocument instead to replace the old one
					// matching the exact
					// path, if present:
//					System.out.println("updating " + file);
					writer.updateDocument(new Term("path", file.toString()), doc);
				}
			}
			System.out.println("\t added "+counterLines +" Documents");
		}
	}

	private static String getTagsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("#")) {
				output += token.substring(1) + " ";
			}
		}
		return output.trim();
	}
}
