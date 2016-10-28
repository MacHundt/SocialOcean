package interfaces;

import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

public interface ILuceneQuerySearcher {
	
	ILuceneQuerySearcher instance = null;
	
	public void initQuerySearcher(IndexSearcher searcher, Analyzer analyzer);
	
	
	/**
	 * This method performs a lucene <code>query</code>
	 * and retrieves the <code>topX</code> results as an array of <code>ScoreDoc</code>
	 * @param query
	 * @param int - topX
	 * @return
	 */
	public ScoreDoc[] searchTop(Query query, int topX);
	
	
	/**
	 * This method performs a lucene <code>query</code>
	 * and retrieves all results as an array of <code>ScoreDoc</code>
	 * @param query
	 * @return ScoreDoc[]
	 */
	public ScoreDoc[] searchAll(Query query);
	
	public ScoreDoc[] fuseQuery(Query query1, Query query2);
	
	public ScoreDoc[] fuseQueries(ArrayList<Query> queries);
	
	public void changeAnalyzer(Analyzer newAnalyzer);
	
}
