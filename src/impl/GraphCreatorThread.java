package impl;

import utils.Lucene;

public abstract class GraphCreatorThread extends Thread{

	private Lucene l;
	
	public GraphCreatorThread(Lucene l) {
		this.l = l;
	}
	
	public abstract void execute();

	
	public final void run() {

		try {
			execute();
			System.out.println("Graph created ... DONE");
			l.printlnToConsole("Graph created ... DONE");
			
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

	}
}
