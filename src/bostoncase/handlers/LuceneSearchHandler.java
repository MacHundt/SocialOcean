 
package bostoncase.handlers;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Named;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;

import impl.LuceneQuerySearcher;
import utils.Lucene;


public class LuceneSearchHandler {
	
	
//	private String index = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index";
	
	// get the Console
	// get the map
	// result table
	
	@Execute
	public void execute(@Optional @Named("QueryString") String query, @Optional @Named("indexpath") String index,
			@Optional @Named("type") String type) {

		if (query.startsWith("#")) {
			query = query.replace("#", "tags:");
		}

		LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
		Lucene l = Lucene.INSTANCE;
		if (!l.isInitialized)
			l.initLucene(index, lqs);

		// GEO Test
		if (query.equals("geo")) {
			l.ADDGeoQuery(42.2279, 42.3969, -71.1908, -70.9235);
		}
		
		// Get Field -> Top50 --> TEST
		else if (query.equals("field40")) {
			l.searchTopXOfField("content", 50);
		}
		
		// Get Time Range TEST
		else if (query.equals("time")) {
			l.searchTimeRange(1366012800, 1366120800);
		}
		
		else {
			try {
				l.ADDQuery(l.getParser().parse(query));
			} catch (ParseException e) {
				System.out.println("Could not parse the Query: " + query);
				e.printStackTrace();
				return;
			}
		}
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}