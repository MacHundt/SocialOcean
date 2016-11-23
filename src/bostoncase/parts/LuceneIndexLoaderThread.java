package bostoncase.parts;

import utils.Lucene;

public abstract class LuceneIndexLoaderThread extends Thread {
	
	private Lucene l;
	
	public LuceneIndexLoaderThread(Lucene l) {
		this.l = l;
	}
	
	public abstract void execute();

	
	public final void run() {
		// Display.getDefault().asyncExec(new Runnable() {
		// public void run() {
		// try {
		// execute();
		// System.out.println(" Done");
		//
		// l.printStatistics();
		// l.printToConsole("Loading Lucene Index ... DONE");
		//
		// } catch (Throwable t) {
		// t.printStackTrace();
		// System.out.println(" Could not execute()");
		// // app.showStatus(t.getMessage());
		// }
		// }
		// });

		try {
			execute();
			System.out.println(" Done");

			l.printStatistics();
			l.printToConsole("Loading Lucene Index ... DONE");

		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

		// app.remove(ui);
		// app.repaint();
	}
	
}
