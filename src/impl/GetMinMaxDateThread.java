package impl;

import utils.Lucene;

public abstract class GetMinMaxDateThread extends Thread {
	
	private Lucene l;
	
	public GetMinMaxDateThread(Lucene l) {
		this.l = l;
	}
	
	public abstract void execute();

	
	public final void run() {

		try {
			execute();
			System.out.println("Done");
			l.printToConsole("Get MinMax-Date ... DONE");
			
//			while (!l.isInitialized)
//				continue;
//			
//			l.createTimeLine(Lucene.TimeBin.HOURS);
//			l.printToConsole("Print TimeLine ... DONE");
			

		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(" Could not execute()");
			// app.showStatus(t.getMessage());
		}

	}
	
}
