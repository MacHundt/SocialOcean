package impl;

import utils.Lucene;

abstract class CreateGroupDetailsThread extends Thread {
	private Lucene l;
	
	public CreateGroupDetailsThread(Lucene l) {
		this.l = l;
	}
	
	public abstract void execute();

	
	public final void run() {

		try {
			execute();
			System.out.println("Details of Group created ... DONE");
			l.printlnToConsole("Details of Group created ... DONE");
			
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

	}
}
