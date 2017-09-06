package impl;

import utils.Lucene;

public abstract class LuceneIndexLoaderThread extends Thread {
	
	private Lucene l;
	private boolean changeHisto;
	private boolean changeTime;
	
	public LuceneIndexLoaderThread(Lucene l, boolean changeHisto, boolean changeTime) {
		this.l = l;
		this.changeHisto = changeHisto;
		this.changeTime = changeTime;
	}
	
	public abstract void execute();

	
	public final void run() {

		try {
			execute();
			System.out.println("Done");
			l.printToConsole("Loading Lucene Index ... DONE");
			
			l.printStatistics();
			if (changeHisto)
				l.showCatHisto();
			if (changeTime)
				l.createTimeLine(Lucene.TimeBin.HOURS);

		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

	}
	
}
