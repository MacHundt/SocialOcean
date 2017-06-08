
package bostoncase.parts;

import java.awt.Color;
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

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.samples.ClusteringDemo;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import impl.GraphPanelCreator1;
import swingintegration.example.EmbeddedSwingComposite;
import utils.Swing_SWT;

public class JungGraph {

	private Composite graphComposite;
	private EmbeddedSwingComposite esc;		//Embedded Swing Component
	private JApplet rootContainer;

	@Inject
	public JungGraph() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {

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
		
//		rootContainer = new ClusteringDemo();
//		rootContainer.start();
		
		rootContainer = new JApplet();
		
		Graph<String, Number> ig = TestGraphs.createTestGraph(true);
		AbstractLayout layout = new CircleLayout(ig);
		VisualizationViewer<String, Number> vv = new VisualizationViewer<>(layout);
		vv.setBackground(Color.WHITE);

		DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<Number, Number>();
		vv.setGraphMouse(gm);

		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
		
		rootContainer.add(gzsp);
		rootContainer.validate();
		
		frame.add(rootContainer);
		

	}




	@Focus
	public void onFocus() {
		graphComposite.setFocus();
	}

}