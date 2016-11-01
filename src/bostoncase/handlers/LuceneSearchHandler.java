 
package bostoncase.handlers;

import javax.inject.Named;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;

import utils.Lucene;


public class LuceneSearchHandler {
	
	
//	private String index = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index";
	
	// get the map
	// result table
	
	// get the Console
	
//	@Execute
//	public void execute(EPartService partService, EModelService service, MWindow window,IApplicationContext context , @Optional @Named("QueryString") String query, @Optional @Named("indexpath") String index,
//			@Optional @Named("type") String type) {
	
	@Execute
	public void execute(@Optional @Named("QueryString") String query, @Optional @Named("type") String type) {
		
//		MPart part = (MPart) service.find("bostoncase.part.console", window);
//		MPart part = partService.findPart("bostoncase.part.console");
////		partService.hidePart(part);
//		part.setTooltip("Cool .. ");
//		System.out.println(part.getLabel());
		
		
		if (query.startsWith("#")) {
			query = query.replace("#", "tags:");
		}

		Lucene l = Lucene.INSTANCE;
		while (!l.isInitialized) {
			continue;
		}
		l.printToConsole("Query: "+type+" - '"+query+"'");
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
		
		// GET QUERY
		else {
			try {
				Query q = l.getParser().parse(query);
				l.ADDQuery(q);
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