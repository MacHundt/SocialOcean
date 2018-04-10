
package socialocean.parts;

import java.awt.Frame;
import java.io.IOException;

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

import impl.GeneralGraphCreator;
import utils.Swing_SWT;

public class JungGraphPart {

	private Composite graphComposite;
	private JApplet rootContainer;

	@Inject
	public JungGraphPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) throws IOException {
		
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
		JPanel panel = GeneralGraphCreator.getGraphPanel();
		rootContainer.add(panel);
		
		rootContainer.validate();
		frame.add(rootContainer);

	}
	

	@Focus
	public void onFocus() {
		graphComposite.setFocus();
	}

}