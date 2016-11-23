package utils;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class Swing_SWT {
	
	public CleanResizeListener CleanResize;
	
	public Swing_SWT() {
		CleanResize = new CleanResizeListener();
	}

	
	
	public class CleanResizeListener extends ControlAdapter {
		private Rectangle oldRect = null;
		
		public void controlResized(ControlEvent e) {
			// Prevent garbage from Swing lags during resize. Fill exposed areas
			// with background color.
			Composite composite = (Composite) e.widget;
			Rectangle newRect = composite.getClientArea();
			if (oldRect != null) {
				int heightDelta = newRect.height - oldRect.height;
				int widthDelta = newRect.width - oldRect.width;
				if ((heightDelta > 0) || (widthDelta > 0)) {
					GC gc = new GC(composite);
					try {
						gc.fillRectangle(newRect.x, oldRect.height, newRect.width, heightDelta);
						gc.fillRectangle(oldRect.width, newRect.y, widthDelta, newRect.height);
					} finally {
						gc.dispose();
					}
				}
			}
			oldRect = newRect;
		}
	}
}

