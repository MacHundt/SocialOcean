
package socialocean.parts;

import java.awt.Frame;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.piccolo2d.extras.PApplet;
import org.piccolo2d.extras.pswing.PSwingCanvas;

import impl.JungGraphImpl;
import impl.JungGraphTEST1;
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
		
//		frame.addHierarchyBoundsListener(new HierarchyBoundsListener() {
//			
//			@Override
//			public void ancestorResized(HierarchyEvent e) {
//				Shell shell = parent.getShell();
//				Rectangle rec = shell.getBounds();
//				System.out.println(rec.toString());
////				canvasSwing.setBounds(rec.x, rec.y, rec.width, rec.height);
//			}
//			
//			@Override
//			public void ancestorMoved(HierarchyEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
//		Rectangle rec = parent.getDisplay().getBounds();
//		Rectangle rec2 = parent.getBounds();
//		Rectangle rec3 = parent.getShell().getBounds();
//		System.out.println("X:"+rec.x+"  Y:"+rec.y+"  width:"+rec.width);
		
		String path = FilesUtil.getPathOfRefFile();
//		path = path.replaceAll(FilesUtil.getReferenceFile(), "graphs/mention_graph.graphml");
		// TEST
		path = path.replaceAll(FilesUtil.getReferenceFile(), "graphs/mentiontags_bostonmarathon.graphml");
		
		JungGraphImpl rootContainer = new JungGraphImpl();
		rootContainer.start(path);
		
//		JungGraphTEST1 rootContainer = new JungGraphTEST1();
//		rootContainer.start();
		
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
		
		
		
//		rootContainer = new JApplet();
//		PApplet rootContainer = new PApplet();
//		JPanel graphComponentPanel = new JPanel(new FlowLayout());
		
		
		
//		Graph<String, Number> ig = TestGraphs.createTestGraph(true);
//		AbstractLayout layout = new KKLayout(ig);
//		VisualizationViewer<String, Number> vv = new VisualizationViewer<>(layout);
//		vv.setBackground(Color.WHITE);
//
//		DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<Number, Number>();
//		vv.setGraphMouse(gm);
//		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
//		gzsp.setPreferredSize(new Dimension(200, 200));
////		graphComponentPanel.add(gzsp);
//		
//		PSwing pNodeGraph1 = new PSwing(gzsp);
//		canvasSwing.getLayer().addChild(pNodeGraph1);
//		
//		ig = TestGraphs.createTestGraph(false);
//		layout = new KKLayout<>(ig);
//		vv = new VisualizationViewer<>(layout);
//		vv.setBackground(Color.WHITE);
//		vv.setGraphMouse(gm);
//		GraphZoomScrollPane gzsp2 = new GraphZoomScrollPane(vv);
//		gzsp2.setPreferredSize(new Dimension(200, 200));
//		PSwing pNodeGraph2 = new PSwing(gzsp2);
//		pNodeGraph2.setOffset(205, 0);
//		canvasSwing.getLayer().addChild(pNodeGraph2);
////		graphComponentPanel.add(gzsp2);
//		
//		
//		ig = TestGraphs.getOneComponentGraph();
//		layout = new KKLayout<>(ig);
//		vv = new VisualizationViewer<>(layout);
//		vv.setBackground(Color.WHITE);
//		vv.setGraphMouse(gm);
//		GraphZoomScrollPane gzsp3 = new GraphZoomScrollPane(vv);
//		gzsp3.setPreferredSize(new Dimension(200, 200));
//		PSwing pNodeGraph3 = new PSwing(gzsp3);
//		pNodeGraph3.setOffset(410, 0);
//		canvasSwing.getLayer().addChild(pNodeGraph3);
////		graphComponentPanel.add(gzsp3);
//		
//		
//		rootContainer.add(canvasSwing);
//		rootContainer.validate();
//		
//		rootContainer.start();
	
//		#######################################################################	
		
		frame.add(rootContainer);

	}
	

	@Focus
	public void onFocus() {
		graphComposite.setFocus();
	}

}