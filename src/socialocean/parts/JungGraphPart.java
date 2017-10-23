
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
import org.piccolo2d.extras.pswing.PSwingCanvas;

import edu.uci.ics.jung.samples.ClusteringDemo;
import edu.uci.ics.jung.samples.VertexCollapseDemo;
import edu.uci.ics.jung.samples.WorldMapGraphDemo;
import impl.GraphPanelCreator;
import impl.JungGraphImpl;
import impl.JungGraphTEST1;
import impl.MapPanelCreator;
import swingintegration.example.EmbeddedSwingComposite;
import utils.FilesUtil;
import utils.Swing_SWT;

public class JungGraphPart {

	private Composite graphComposite;
	private EmbeddedSwingComposite esc;		//Embedded Swing Component
	private JApplet rootContainer;
	private PSwingCanvas canvasSwing;

	@Inject
	public JungGraphPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) throws IOException {
		
//		canvasSwing = new PSwingCanvas();
		
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
		JPanel panel = GraphPanelCreator.getGraphPanel();
		rootContainer.add(panel);
		
		
//	#######################################################################	
		// TEST   GraphML reading In
//		String path = FilesUtil.getPathOfRefFile();
//		path = path.replaceAll(FilesUtil.getReferenceFile(), "graphs/mention_graph.graphml");
//		path = path.replaceAll(FilesUtil.getReferenceFile(), "graphs/mentiontags_bostonmarathon.graphml");
//		rootContainer.start(path);
		
//	#######################################################################	
		// DEMOS
//		rootContainer = new EdgeLabelDemo();
//		rootContainer.start();
		
//		rootContainer = new ClusteringDemo();
//		rootContainer.start();
		
//		rootContainer = new MinimumSpanningTreeDemo();
//		rootContainer.start();
		
//		rootContainer = new MultiViewDemo();
//		rootContainer.start();
		
//		rootContainer = new WorldMapGraphDemo();
//		rootContainer.start();
		
//		rootContainer = new VertexCollapseDemo();
//		rootContainer.start();
		
//		rootContainer = new VertexImageShaperDemo();
//		rootContainer.start();
		
//		rootContainer = new VertexLabelPositionDemo();
//		rootContainer.start();
		
//		rootContainer = new ImageEdgeLabelDemo();
//		rootContainer.start();
		
//		rootContainer = new PluggableRendererDemo();
//		rootContainer.start();
		
//		GraphFromGraphMLDemo rootContainer = new GraphFromGraphMLDemo(path);
//		rootContainer.start();
		
//		#######################################################################	
		
		rootContainer.validate();
		frame.add(rootContainer);

	}
	

	@Focus
	public void onFocus() {
		graphComposite.setFocus();
	}

}