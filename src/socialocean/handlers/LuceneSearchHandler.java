 
package socialocean.handlers;

import javax.inject.Named;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;

import impl.GraphCreatorThread;
import impl.TimeLineCreatorThread;
import socialocean.model.Result;
import utils.Lucene;
import utils.Lucene.TimeBin;


public class LuceneSearchHandler {
	
	
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
		
		if (query.startsWith("@")) {
			query = query.replace("@", "mention:");
		}

		Lucene l = Lucene.INSTANCE;
		while (!l.isInitialized) {
			continue;
		}
		l.printToConsole("Query: "+type+" - '"+query+"'");
		System.out.println("Query: "+type+" - '"+query+"'");
//		// GEO Test
//		if (query.equals("geo")) {
//			result = l.ADDGeoQuery(42.2279, 42.3969, -71.1908, -70.9235);
//		}
		
		
//		// Get Time Range TEST
//		if (query.equals("time")) {
//			result = l.searchTimeRange(1366012800, 1366120800, true, true);
//		}
//		else {
		
		
		// GET QUERY
			try {
				Query q = l.getParser().parse(query);
				Result result = l.query(q, type, true, true);
				ScoreDoc[] data = result.getData();
				TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
					@Override
					public void execute() {
						result.setTimeCounter(l.createTimeBins(TimeBin.HOURS, data));
						l.showInTimeLine(result.getTimeCounter());
//						l.changeTimeLine(TimeBin.MINUTES);
					}
				};
				lilt.start();
				
				GraphCreatorThread graphThread = new GraphCreatorThread(l) {
					
					@Override
					public void execute() {
						l.createGraphView(data);
//						l.createSimpleGraphView(data);
					}
				};
				graphThread.start();
				
				l.createMapMarkers(data, true);
				l.changeHistogramm(result.getHistoCounter());
				
//				l.createGraphML_Mention(result, true);
//				l.createGraphML_Retweet(result, true);
				
			} catch (ParseException e) {
				System.out.println("Could not parse the Query: " + query);
				e.printStackTrace();
				return;
			}
//		}
		
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}