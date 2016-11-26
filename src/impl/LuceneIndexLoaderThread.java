package impl;

import utils.Lucene;

public abstract class LuceneIndexLoaderThread extends Thread {
	
	private Lucene l;
	
	public LuceneIndexLoaderThread(Lucene l) {
		this.l = l;
	}
	
	public abstract void execute();

	
	public final void run() {

		try {
			execute();
			System.out.println(" Done");
			l.printToConsole("Loading Lucene Index ... DONE");

			l.printStatistics();
			l.showCatHisto();

		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

	}
	
}
