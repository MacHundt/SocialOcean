package impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import org.piccolo2d.extras.pswing.PSwing;
import org.piccolo2d.extras.pswing.PSwingCanvas;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

public class JungGraphTEST1 extends JApplet {

	
	private PSwingCanvas canvasSwing;
	
	
	
	public void start() {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				 setUpView();
			}
		});
		
          
	}



	private void setUpView() {
		

//		Container content = getContentPane();
//		content.add(new GraphZoomScrollPane(vv));
//		JPanel south = new JPanel();
//		JPanel grid = new JPanel(new GridLayout(2,1));
//		grid.add(scramble);
//		grid.add(groupVertices);
//		south.add(grid);
//		south.add(eastControls);
//		JPanel p = new JPanel();
//		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
//		p.add(gm.getModeComboBox());
//		south.add(p);
//		content.add(south, BorderLayout.SOUTH);
		
		canvasSwing = new PSwingCanvas();
		
//		canvasSwing.removeInputEventListener(canvasSwing.getZoomEventHandler());
//        PMouseWheelEventHandler wheelHandler = new PMouseWheelEventHandler();
//        canvasSwing.addInputEventListener(wheelHandler);
		
		Graph<String, Number> ig = TestGraphs.createTestGraph(true);
		AbstractLayout layout = new KKLayout(ig);
		VisualizationViewer<String, Number> vv = new VisualizationViewer<>(layout);
		vv.setBackground(Color.WHITE);

		DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<Number, Number>();
		vv.setGraphMouse(gm);
		
		vv.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				e.getComponent().setBackground(
						(e.getComponent().getBackground().equals(Color.YELLOW) ? Color.white : Color.YELLOW));
				
			}
		});
		
		
		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
		gzsp.setPreferredSize(new Dimension(200, 200));
		
		
//		graphComponentPanel.add(gzsp);
		
		PSwing pNodeGraph1 = new PSwing(gzsp);
		canvasSwing.getLayer().addChild(pNodeGraph1);
		
		ig = TestGraphs.createTestGraph(false);
		layout = new KKLayout<>(ig);
		vv = new VisualizationViewer<>(layout);
		vv.setBackground(Color.WHITE);
		vv.setGraphMouse(gm);
		
		vv.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				e.getComponent().setBackground(
						(e.getComponent().getBackground().equals(Color.YELLOW) ? Color.white : Color.YELLOW));
				
			}
		});
		
		GraphZoomScrollPane gzsp2 = new GraphZoomScrollPane(vv);
		gzsp2.setPreferredSize(new Dimension(200, 200));
		PSwing pNodeGraph2 = new PSwing(gzsp2);
		pNodeGraph2.setOffset(205, 0);
		canvasSwing.getLayer().addChild(pNodeGraph2);
//		graphComponentPanel.add(gzsp2);
		
		
		ig = TestGraphs.getOneComponentGraph();
		layout = new KKLayout<>(ig);
		vv = new VisualizationViewer<>(layout);
		vv.setBackground(Color.WHITE);
		vv.setGraphMouse(gm);
		
		vv.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				e.getComponent().setBackground(
						(e.getComponent().getBackground().equals(Color.YELLOW) ? Color.white : Color.YELLOW));
				
			}
		});
		
		GraphZoomScrollPane gzsp3 = new GraphZoomScrollPane(vv);
		gzsp3.setPreferredSize(new Dimension(200, 200));
		PSwing pNodeGraph3 = new PSwing(gzsp3);
		pNodeGraph3.setOffset(410, 0);
		canvasSwing.getLayer().addChild(pNodeGraph3);
//		graphComponentPanel.add(gzsp3);
		
		
		add(canvasSwing);
		validate();
		
	}
}
