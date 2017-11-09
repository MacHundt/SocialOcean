package socialocean.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import impl.LuceneIndexLoaderThread;
import impl.LuceneQuerySearcher;
import utils.DBManager;
import utils.Lucene;

public class OpenHandler {
	
	@Execute
	public void execute(Shell shell) {
		// FileDialog dialog = new FileDialog(shell);
		
		
		DirectoryDialog dialog = new DirectoryDialog(shell);
		String initialPath = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/LUCENE_Index/";
		dialog.setFilterPath(initialPath); // Windows
																										// specific

		dialog.setText("Select path to the Lucene Index");
		dialog.open();

		String lucenIndex = dialog.getFilterPath();
		System.out.println("IndexPath: "+lucenIndex);

		LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
		Lucene l = Lucene.INSTANCE;
		if (!lucenIndex.isEmpty() ) {
			LuceneIndexLoaderThread lilt = new LuceneIndexLoaderThread(l, true, true) {
				@Override
				public void execute() throws Exception {
					System.out.println("Loading Lucene Index ...");
					l.initLucene(lucenIndex, lqs);
				}
			};
			lilt.start();
		}
		
		// GET min max Dates	-  for normalization and time binning
		String min = "";
		String max = "";
		String usermin = "";
		String usermax = "";
		
		
		InputStream input = null;
		try {
			// ## LOAD Settings File
			Properties prop = new Properties();
			
//			URL url = null;
//			try {
//				url = new URL("platform:/plugin/" + "SocialOcean/" + "settings/config.properties");
//
//			} catch (MalformedURLException e1) {
//				e1.printStackTrace();
//			}
//			url = FileLocator.toFileURL(url);
//			input = new FileInputStream(new File(url.getPath()));
			// System.out.println(prop.getProperty("lucene_index"));
//			lucenIndex = prop.getProperty("lucene_index");
			
			input = new FileInputStream(new File(lucenIndex+"/settings.properties"));
			prop.load(input);
			
			min = prop.getProperty("min");
			max = prop.getProperty("max");
			usermin = prop.getProperty("usermin");
			usermax = prop.getProperty("usermax");
			
			// init db tables
			DBManager.setTweetdataTable(prop.getProperty("tweetdata"));
			DBManager.setUserTable(prop.getProperty("users"));
			
			// init times
			l.initMinDate(min);
			l.initMaxDate(max);
			l.iniUserMinMaxCreationDate(usermin, usermax);
			
			System.out.println("Get MinMax Dates ... DONE");
			l.printlnToConsole("Get MinMax-Date ... DONE");
			l.clearGraph();
			l.clearMap();
			l.clearQueryHistroy();
			
			
		} catch (IOException e1) {
//			e1.printStackTrace();
			System.err.println(lucenIndex +" >>>> has no settings.propertyies file");
			return;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
//					e.printStackTrace();
					return;
				}
			}
		}
	
	}
}
