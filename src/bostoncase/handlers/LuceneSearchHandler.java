 
package bostoncase.handlers;

import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;

import bostoncase.parts.Console;
import bostoncase.widgets.SearchFiles;


public class LuceneSearchHandler {
	
	
//	private String index = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index";
	
	// get the Console
	// get the map
	// result table
	// get Lucene Index folder
	// ... include Lucene LIB
	// ... get geo tools
	// ... perform query!!
	
	@Execute
	public void execute(@Optional @Named("QueryString") String query,@Optional @Named("indexpath") String index, 
			@Optional @Named("type") String type)  {
		
		// execute query get Lucene result
		System.out.println(type+" - "+query);
		System.out.println(index);
		
		try {
//			SearchFiles.geoQuery(index, 42.2279, 42.3969, -71.1908, -70.9235);
			SearchFiles.searchQuery(query, index);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		try {
//		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
//		IndexSearcher searcher = new IndexSearcher(reader);
//		
//		Term t = new Term("content", query);
//		
//		// Get the top 10 docs
//		Query lucene_query = new TermQuery(t);
//		TopDocs tops= searcher.search(lucene_query, 10);
//		ScoreDoc[] scoreDoc = tops.scoreDocs;
//		System.out.println(scoreDoc.length); 
//		for (ScoreDoc score : scoreDoc){
//		    System.out.println("DOC " + score.doc + " SCORE " + score.score);
//		}
//		// Get the frequency of the term
//		int freq = reader.docFreq(t);
//		System.out.println("FREQ " + freq);
//		
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}