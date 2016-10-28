package impl;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery;

import interfaces.ILuceneQuerySearcher;

public enum LuceneQuerySearcher implements ILuceneQuerySearcher {
	
	INSTANCE;
	
	private IndexSearcher searcher;
	private Analyzer analyzer;
	

	@Override
	public void initQuerySearcher(IndexSearcher searcher, Analyzer analyzer) {
		this.searcher = searcher;
		this.analyzer = analyzer;
		
	}

	@Override
	public ScoreDoc[] searchTop(Query query, int topX) {
		TopDocs results;
		ScoreDoc[] hits = null;
		try {
			results = searcher.search(query, topX);
			hits = results.scoreDocs;
			
		} catch (IOException e) {
			System.out.println("searchTop() - Exception");
			e.printStackTrace();
			return null;
		}
		
		return hits;
	}

	@Override
	public ScoreDoc[] searchAll(Query query) {
		TopDocs results;
		ScoreDoc[] hits = null;
		try {
			results = searcher.search(query, 10);
			int numTotalHits = results.totalHits;
			// Get ALL
			results = searcher.search(query, numTotalHits);
			hits = results.scoreDocs;
			
		} catch (IOException e) {
			System.out.println("SeachAll() - Exception");
			e.printStackTrace();
			return null;
		}
		
		return hits;
	}


	@Override
	public ScoreDoc[] fuseQuery(Query query1, Query query2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScoreDoc[] fuseQueries(ArrayList<Query> queries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void changeAnalyzer(Analyzer newAnalyzer) {
		// TODO Auto-generated method stub
		
	}

}
