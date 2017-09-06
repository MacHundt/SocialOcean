package bostoncase.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import impl.LuceneIndexLoaderThread;
import impl.LuceneQuerySearcher;
import utils.Lucene;
public class OpenHandler {

	@Execute
	public void execute(Shell shell){
//		FileDialog dialog = new FileDialog(shell);
		
		 DirectoryDialog dialog = new DirectoryDialog(shell);
		 dialog.setFilterPath("/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data"); // Windows specific
		 
		 dialog.setText("Select path to the Lucene Index");
		 dialog.open();
		 
		 String lucenIndex = dialog.getFilterPath();
		 System.out.println(lucenIndex);
		 
		LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
		Lucene l = Lucene.INSTANCE;
		if (!lucenIndex.isEmpty()) {
			LuceneIndexLoaderThread lilt = new LuceneIndexLoaderThread(l, true, true) {
				@Override
				public void execute() {
					System.out.println("Loading Lucene Index ...");
					l.initLucene(lucenIndex, lqs);
				}
			};
			lilt.start();
		}	 
		
	}
}
