 
package bostoncase.parts;

import java.awt.Frame;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.piccolo2d.extras.pswing.PSwingCanvas;

import impl.GraphPanelCreator3;
import swingintegration.example.EmbeddedSwingComposite;
import utils.Swing_SWT;

public class GraphPart {
	
	private Composite graphComposite;
	private EmbeddedSwingComposite esc;		//Embedded Swing Component
	private JApplet rootContainer;
	private PSwingCanvas canvasSwing;
	
	@Inject
	public GraphPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		// SWING/SWT integration 
		// http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
		
		//-- same LOOK and FEEL
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		

		Swing_SWT util = new Swing_SWT();
		parent.addControlListener(util.CleanResize);
		
		graphComposite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND );
		Frame frame = SWT_AWT.new_Frame(graphComposite);
 		
		rootContainer = new JApplet();
		
		JPanel panel = GraphPanelCreator3.getGraphPanel();
		
		
//		canvasSwing = new PSwingCanvas();
//		JButton btn = new JButton("TEst");
//		PSwing pSwingNode = new PSwing(panel);
//		canvasSwing.getLayer().addChild(pSwingNode);
//		canvasSwing.removeInputEventListener(canvasSwing.getZoomEventHandler());
//		PMouseWheelEventHandler wheelHandler = new PMouseWheelEventHandler();
//		canvasSwing.addInputEventListener(wheelHandler);
//		canvasSwing.validate();
//		rootContainer.add(canvasSwing);
		
		rootContainer.add(panel);
		rootContainer.validate();
 		
		frame.add(rootContainer);

	}
	
	
	@Focus
	public void onFocus() {
		graphComposite.setFocus();
	}
	
	
}