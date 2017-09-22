package socialocean.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import utils.HistogramEntry;
import utils.TimeLineHelper;

public class Result {
	
	private IndexSearcher searcher;
	
	private ScoreDoc[] data;
	private HashMap<String, HistogramEntry> histoCounter;
	private ArrayList<TimeLineHelper> timeCounter;
	
	private boolean hasTime = false;

	public Result(ScoreDoc[] data, IndexSearcher searcher) {
		this.setData(data);
		this.searcher = searcher;
	}

	public ScoreDoc[] getData() {
		return data;
	}

	public void setData(ScoreDoc[] data) {
		this.data = data;
	}

	public HashMap<String, HistogramEntry> getHistoCounter() {
		if (histoCounter == null) {
			histoCounter = new HashMap<>();

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
				field = field.replace(" & ", "_").toLowerCase();
				// String sentiment_str = (document.getField("sentiment")).stringValue();
				// if (sentiment_str.equals("positive"))
				// sentiment = 1.0;
				// else if (sentiment_str.equals("negative"))
				// sentiment = -1.0;
				// else
				// sentiment = 0;

				String sentiment_str = (document.getField("sentiment")).stringValue();
				if (sentiment_str.equals("pos"))
					sentiment = 1.0;
				else if (sentiment_str.equals("neg"))
					sentiment = -1.0;
				else
					sentiment = 0;

				if (histoCounter.containsKey(field)) {
					HistogramEntry category = histoCounter.get(field);
					category.count();
					category.addSentiment(sentiment);
				} else {
					HistogramEntry category = new HistogramEntry(field);
					category.count();
					category.addSentiment(sentiment);
					histoCounter.put(field, category);
				}
			}
		}
		return histoCounter;
	}

	public void setHistoCounter(HashMap<String, HistogramEntry> histoCounter) {
		this.histoCounter = histoCounter;
	}

	public ArrayList<TimeLineHelper> getTimeCounter() {
		return timeCounter;
	}

	public void setTimeCounter(ArrayList<TimeLineHelper> timeCounter) {
		this.timeCounter = timeCounter;
		hasTime = true;
	}
	
	public boolean hasTimeCounter() {
		return hasTime;
	}
	
	public int size() {
		return data.length;
	}
}
