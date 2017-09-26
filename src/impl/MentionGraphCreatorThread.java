package impl;

import utils.Lucene;

public abstract class MentionGraphCreatorThread extends Thread {
	
	private Lucene l;
	
	public MentionGraphCreatorThread(Lucene l) {
		this.l = l;
	}
	
	public abstract void execute();

	
	public final void run() {

		try {
			execute();
			System.out.println("Done");
			l.printlnToConsole("MentionGraph ... DONE");
			
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

	}
	
}
