package impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utils.DBManager;
import utils.Lucene;

public class StoreToJSONThread extends Thread{
	
	private Lucene l;
	private ScoreDoc[] data;
	private Connection c;
	private Statement stmt;
	private String indexPath = "";
	
	public StoreToJSONThread(Lucene l, ScoreDoc[] data, 
			Connection c, Statement stmt, String path)  {
		this.l = l;
		this.data = data;
		this.c = c;
		this.stmt = stmt;
		indexPath = path;
	}
	
	public final void run() {
		
		long mindate = Long.MAX_VALUE;
		long maxdate = Long.MIN_VALUE;
		
		// create an output file
		File json = new File(indexPath + "/data.json");
		FileWriter jsWr = null;
		try {
			jsWr = new FileWriter(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Lucene l = Lucene.INSTANCE;

		// JSONObject parent = new JSONObject();

		JSONObject obj = new JSONObject();
		obj.put("name", indexPath.substring(indexPath.lastIndexOf("/") + 1, indexPath.length()));
		obj.put("tweettable", DBManager.getTweetdataTable());
		obj.put("usertable", DBManager.getUserTable());
		obj.put("originIndexPath", l.getLucenIndexPath());
		// obj.put("date", "the startdate");
		obj.put("timerange", "get the timerange: long to long");
		JSONArray history = new JSONArray();
		for (Query q : l.getQueryHistory()) {
			String query = q.toString();
			if (query.startsWith("date:")) {
				obj.put("timerange", query.substring(query.indexOf("[") + 1, query.lastIndexOf("]")));
			}
			history.add(query);
		}
		obj.put("queryhistory", history);
		
		try {
			String table = DBManager.getTweetdataTable();
			 l.printToConsole("\n\tPROGRESS:  ||");

			int max = data.length;
			int step = (int) Math.ceil(max / 10.0);
			int stepCounter = 0;
			int counter = 0;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			
			JSONArray tweets = new JSONArray();
			
			for (ScoreDoc doc : data) {
				counter++;
				Document document = null;
				try {
					document = l.getIndexSearcher().doc(doc.doc);
					
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
					String query = "select t.tweet_content, tweet_creationdate from "+table+" as t where t.tweet_id = "+id;
					ResultSet rs = stmt.executeQuery(query);
					while (rs.next()) {
						content = rs.getString("tweet_content");
						tw_Date = rs.getString("tweet_creationdate");
					}
					
					content = content.replaceAll("\"", "");
					TextField content_field = new TextField("content", content, Field.Store.NO);
					document.add(content_field);
					
				} catch (IOException e) {
					continue;
				}
				if (document != null) {
					
					JSONObject tweet = new JSONObject();
					JSONArray fields = new JSONArray();
					
					for (IndexableField field : document.getFields()) {
						JSONObject f = new JSONObject();
						f.put(field.name(), document.get(field.name()));
						fields.add(f);
					}
					tweet.put("fields", fields);
					tweets.add(tweet);
					
				}
				
				if (counter % step == 0) {
					stepCounter++;
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
			
			Date date = new Date(mindate*1000L);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			String mind =  format.format(date);
			date = new Date(maxdate*1000L);
			String maxd =  format.format(date);
			
//			WRITE JSON
			obj.put("timerange", mind+" TO "+maxd);
			obj.put("tweets", tweets);
			obj.writeJSONString(jsWr);
			jsWr.flush();
			
		} 
		catch (IOException | SQLException e) {
		}
		

	}

}
