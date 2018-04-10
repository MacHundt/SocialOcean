package impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.joda.time.DateTime;

import utils.DBManager;
import utils.Lucene;

public class ReIndexingThread extends Thread {

	private Lucene l;
	private ScoreDoc[] data;
	private Connection c;
	private Statement stmt;
	private IndexWriter writer;
	private boolean RELOAD = false;
	private String indexPath = "";
	
	public ReIndexingThread(Lucene l, ScoreDoc[] data, 
			Connection c, Statement stmt, IndexWriter writer, boolean reload, String path)  {
		this.l = l;
		this.data = data;
		this.c = c;
		this.stmt = stmt;
		this.writer = writer;
		this.RELOAD = reload;
		indexPath = path;
	}
	
//	public abstract void execute() throws Exception;
	
	public final void run() {

		long mindate = Long.MAX_VALUE;
		long maxdate = Long.MIN_VALUE;
		Date mincdate = DateTime.now().toDate();
		Date maxcdate = new Date(0, 0, 0 ); 

		try {
			String table = DBManager.getTweetdataTable();
			 l.printToConsole("\n\tPROGRESS:  ||");

			int max = data.length;
			int step = (int) Math.ceil(max / 10.0);
			int stepCounter = 0;
			int counter = 0;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			for (ScoreDoc doc : data) {
				counter++;
				Document document = null;
				Document outDoc = new Document();
				try {
					document = l.getIndexSearcher().doc(doc.doc);
					
					for (IndexableField field : document.getFields()) {
						if (field.name().equals("geo")) {
							long hashgeo = field.numericValue().longValue();
							double lat = GeoPointField.decodeLatitude(hashgeo);
							double lon = GeoPointField.decodeLongitude(hashgeo);
							GeoPointField geo = new GeoPointField("geo", lat, lon, GeoPointField.Store.YES);
							outDoc.add(geo);
						}
						else
							outDoc.add(field);
					}
					
					
					// get content .. add content
					// fulltext
					String id = document.get("id");
					String content = "";
					String user_cDate = "";
					String tw_Date = "";
					long date = Long.parseLong(document.get("date"));
					
					mindate = Math.min(mindate, date);
					maxdate = Math.max(maxdate, date);
					
					// add user creation date
					String query = "select t.tweet_content, t.user_creationdate, tweet_creationdate from "+table+" as t where t.tweet_id = "+id;
					ResultSet rs = stmt.executeQuery(query);
					while (rs.next()) {
						content = rs.getString("tweet_content");
						user_cDate = rs.getString("user_creationdate");
						tw_Date = rs.getString("tweet_creationdate");
					}
					Date cDate = sdf.parse(user_cDate);
					
					mincdate = (cDate.before(mincdate) ? cDate : mincdate);
					maxcdate = (cDate.after(maxcdate) ? cDate : maxcdate);
					content = content.replaceAll("\"", "");
					TextField content_field = new TextField("content", content, Field.Store.NO);
					outDoc.add(content_field);
					
				} catch (IOException e) {
					continue;
				}
				if (outDoc != null) {
					writer.addDocument(outDoc);
				}
				
				if (counter % step == 0) {
					stepCounter++;
					// Increment the progress bar
//					bar.setSelection(bar.getSelection() + 1);
//					bar.setFocus();
					System.out.print("-");
					if (stepCounter == 5)
						l.printToConsole("***|");
					else
						l.printToConsole("***");
				}
			}
			
			System.out.println();
			l.printToConsole("||  \nDONE");
			
			stmt.close();
			c.close();
			writer.close();
			
			
			// create properties file
			File propfile = new File(indexPath+"/settings.properties");
			FileWriter wr;
			Date date = new Date(mindate*1000L);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			String mind =  format.format(date);
			date = new Date(maxdate*1000L);
			String maxd =  format.format(date);
			
			String userMinD = sdf.format(mincdate);
			String userMaxD = sdf.format(maxcdate);
			
			try {
				wr = new FileWriter(propfile);
				wr.write("# DB \ntweetdata="+DBManager.getTweetdataTable()+"\nusers="+DBManager.getUserTable()+"\n# TIME \n"
						+ "min="+mind+"\nmax="+maxd+"\nusermin="+userMinD+"\nusermax="+userMaxD+"\n");
				wr.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		} catch (Throwable e) {
		}
		
		if (RELOAD) {
			// TODO store mapping (index to foldername
			// re-init Lucene with new created Lucene Index
			// Add a dialog, to enable to open LuceneIndex files
			
			LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
			LuceneIndexLoaderThread lilt = new LuceneIndexLoaderThread(l, false, false) {
				@Override
				public void execute() throws Exception {
					System.out.println("Loading Lucene Index ...");
					l.initLucene( indexPath, lqs);
				}
			};
			
			lilt.start();
		}

	}
}
