package impl;

import utils.Lucene;

	public abstract class TimeLineCreatorThread extends Thread {
		
		private Lucene l;
		
		public TimeLineCreatorThread(Lucene l) {
			this.l = l;
		}
		
		public abstract void execute();
		public final void run() {

			try {
				execute();
				System.out.println("Changed Timeline ... DONE");
				l.printToConsole("TimeLine ... DONE");
				
			} catch (Throwable t) {
				t.printStackTrace();
				System.out.println(" Could not execute()");
				// app.showStatus(t.getMessage());
			}

		}
}
