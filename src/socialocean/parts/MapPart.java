package socialocean.parts;

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

import impl.MapPanelCreator;
import swingintegration.example.EmbeddedSwingComposite;
import utils.Swing_SWT;

public class MapPart {
	
	private Composite mapComposite;
	private EmbeddedSwingComposite esc;		//Embedded Swing Component
	private JApplet rootContainer;
	
	
	@Inject
	public MapPart() {
		
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
		
		mapComposite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND );
		Frame frame = SWT_AWT.new_Frame(mapComposite);
 		
		rootContainer = new JApplet();
		
		JPanel panel = MapPanelCreator.getMapPanel();
		
		rootContainer.add(panel);
		rootContainer.validate();
 		
		frame.add(rootContainer);

	}
	
	
	@Focus
	public void onFocus() {
		mapComposite.setFocus();
	}
	
	
}