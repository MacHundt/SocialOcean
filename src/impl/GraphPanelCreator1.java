package impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import utils.DBManager;
import utils.Swing_SWT;

public class GraphPanelCreator1 {
	
	private static JPanel graphPanel = null;
//	private static Graph graph = new MultiGraph("embedded");
//	private static Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//	private static View view = viewer.getDefaultView();
	
	public static JPanel getGraphPanel() {
		
		if (graphPanel != null) {
			return graphPanel;
		} else {
			
//			JPanel graphPanel = new JPanel(new BorderLayout());
//			ScrollPane pane = new ScrollPane(ScrollPane.)
			
//			ImageDescriptor st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/echochamber1.png");
			ImageDescriptor st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/graphView.png");
			org.eclipse.swt.graphics.Image img = st.createImage();
			BufferedImage image = Swing_SWT.convertToAWT(img.getImageData());
			
			ImagePanel graphPanel = new ImagePanel(image, 15);
			
//			JLabel picLabel = new JLabel(new ImageIcon(image));
//			ImageLabel picLabel = new ImageLabel(image, image.getWidth(), image.getHeight());
//			graphPanel.add(picLabel);
			
			graphPanel.addMouseWheelListener(new MouseWheelListener() {
				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					
					// ZOOM out
					if (e.getWheelRotation() < 0) {
						graphPanel.zoomOut();
					} else {
						// ZOOM in
						graphPanel.zoomIn();
					}
					graphPanel.revalidate();
					graphPanel.repaint();
					
				}
			});;
			
//			System.setProperty("org.graphstream.ui.j2dviewer.renderer", "org.graphstream.ui.j2dviewer.renderer.JComponentRenderer");
//			Graph graph = new MultiGraph("embedded");
//			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//			
//			graph.addNode("A");
//			graph.addNode("B");
//			graph.addNode("C");
//			graph.addEdge("AB", "A", "B");
//			graph.addEdge("BC", "B", "C");
//			graph.addEdge("CA", "C", "A");
//
//			graph.addAttribute("ui.quality");
//			graph.addAttribute("ui.antialias");
//			
//
////			graphPanel.add((JComponent)viewer.getDefaultView(), BorderLayout.CENTER);
////			SwingUtilities.invokeLater(new Runnable() {
////	            public void run() {
////	            	
//////	            	Graph graph = new SingleGraph("Tutorial 1");
//////	                Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
////	            	
//////	            	WARNUNG: "gs.ui.renderer" is deprecated, use "org.graphstream.ui.renderer" instead.
////	            	
//////	                View view = viewer.addDefaultView(true);
////	                
////	                graph.addNode("A");
////	                graph.addNode("B");
////	                graph.addNode("C");
////	                graph.addEdge("AB", "A", "B");
////	                graph.addEdge("BC", "B", "C");
////	                graph.addEdge("CA", "C", "A");
////
////	                graph.addAttribute("ui.quality");
////	                graph.addAttribute("ui.antialias");
////	                
////	                graphPanel.add((Component)view, BorderLayout.CENTER);
////	            }
////	        });
//			
//			
//			
////			Graph graph = new MultiGraph("embedded");
////			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
////			View view = viewer.addDefaultView(false);   // false indicates "no JFrame".
//			// ...
//			
//			
//			
//			
//			JButton updateGraph_btn = new JButton("Update");
//			
//			updateGraph_btn.addActionListener(new ActionListener() {
//				
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					
//					// load Graph from DB and show
////					try {
////						updateGraph(viewer, graph);
////					} catch (SQLException e1) {
////						// TODO Auto-generated catch block
////						e1.printStackTrace();
////					}
//				}
//			});
//			
//			graphPanel.add(updateGraph_btn, BorderLayout.SOUTH);
			
			return graphPanel;
		}
	}
	
	
	private static class ImagePanel extends JPanel {
		  private double zoom = 1.0;

		  private double percentage;

		  private Image image;

		  public ImagePanel(Image image, double zoomPercentage) {
		    this.image = image;
		    percentage = zoomPercentage / 100;
		  }

		  public void paintComponent(Graphics grp) {
		    Graphics2D g2D = (Graphics2D) grp;
		    g2D.scale(zoom, zoom);
		    g2D.drawImage(image, 0, 0, this);
		  }

		  public void setZoomPercentage(int zoomPercentage) {
		    percentage = ((double) zoomPercentage) / 100;
		  }

		  public void originalSize() {
		    zoom = 1;
		  }

		  public void zoomIn() {
		    zoom += percentage;
		  }

		  public void zoomOut() {
		    zoom -= percentage;

		    if (zoom < percentage) {
		      if (percentage > 1.0) {
		        zoom = 1.0;
		      } else {
		        zoomIn();
		      }
		    }
		  }
		}
	
	
//	private static class ImageLabel extends JLabel{
//	    Image image;
//	    int width, height;
//	    
//	    public ImageLabel(Image image, int width, int height) {
//	    	this.image = image;
//	    	this.width = width;
//	    	this.height = height;
//	    }
//	    
//
//	    public void paint(Graphics g) {
//	        int x, y;
//	        //this is to center the image
//	        x = (this.getWidth() - width) < 0 ? 0 : (this.getWidth() - width);
//	        y = (this.getHeight() - width) < 0 ? 0 : (this.getHeight() - width);
//
//	        g.drawImage(image, x, y, width, height, null);
//	    }
//
//	    public void setDimensions(int width, int height) {
//	        this.height = height;
//	        this.width = width;
//
//	        image = image.getScaledInstance(width, height, Image.SCALE_FAST);
//	        Container parent = this.getParent();
//	        if (parent != null) {
//	            parent.repaint();
//	        }
//	        this.repaint();
//	    }
//	}


}
