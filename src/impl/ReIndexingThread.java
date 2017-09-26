package impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.ScoreDoc;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

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

		try {
			String table = DBManager.getTweetdataTable();
			 l.printToConsole("\n\tPROGRESS:  ||");

			int max = data.length;
			int step = (int) Math.ceil(max / 10.0);
			int stepCounter = 0;
			int counter = 0;
			for (ScoreDoc doc : data) {
				counter++;
				Document document = null;
				try {
					document = l.getIndexSearcher().doc(doc.doc);
					
					// get content .. add content
					// fulltext
					String id = document.get("id");
					String content = "";
						
					String query = "select t.tweet_content from "+table+" as t where t.tweet_id = "+id;
					ResultSet rs = stmt.executeQuery(query);
					while (rs.next()) {
						content = rs.getString("tweet_content");
					}
						
//					String content = getContent(id);
					content = content.replaceAll("\"", "");
					TextField content_field = new TextField("content", content, Field.Store.NO);
					document.add(content_field);
					
				} catch (IOException e) {
					continue;
				}
				if (document != null)
					writer.addDocument(document);
				
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
