package impl;

/*
* Copyright (c) 2003, The JUNG Authors
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxGraphStructure;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxStylesheet;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import socialocean.parts.Histogram;
import utils.DBManager;
import utils.Lucene;


/**
 * This simple app demonstrates how one can use our algorithms and visualization libraries in unison.
 * In this case, we generate use the Zachary karate club data set, widely known in the social networks literature, then
 * we cluster the vertices using an edge-betweenness clusterer, and finally we visualize the graph using
 * Fruchtermain-Rheingold layout and provide a slider so that the user can adjust the clustering granularity.
 * @author Scott White
 */
@SuppressWarnings("serial")
public class JungGraphImpl extends JApplet {
	
	private static Graph<MyUser, MyEdge> g;
	private static int topK = 5;
	static boolean ASC = true;
	static boolean DESC = false;

	VisualizationViewer<Number, Number> vv;

	LoadingCache<Number, Paint> vertexPaints =
			CacheBuilder.newBuilder().build(
					CacheLoader.from(Functions.<Paint>constant(Color.white))); 
	LoadingCache<Number, Paint> edgePaints =
			CacheBuilder.newBuilder().build(
					CacheLoader.from(Functions.<Paint>constant(Color.blue))); 

	
	public final Color[] similarColors =	
	{
		new Color(216, 134, 134),
		new Color(135, 137, 211),
		new Color(134, 206, 189),
		new Color(206, 176, 134),
		new Color(194, 204, 134),
		new Color(145, 214, 134),
		new Color(133, 178, 209),
		new Color(103, 148, 255),
		new Color(60, 220, 220),
		new Color(30, 250, 100)
	};
	
	public static void createGraph(ScoreDoc[] result, IndexSearcher searcher, boolean withMentions, boolean withFollows) {
		
		
		
	}
	

	

	private void setUpView(String filename) throws IOException, ParserConfigurationException, SAXException {
		
		Supplier<Number> vertexFactory = new Supplier<Number>() {
            int n = 0;
            public Number get() { return n++; }
        };
        Supplier<Number> edgeFactory = new Supplier<Number>()  {
            int n = 0;
            public Number get() { return n++; }
        };
        
        
        GraphMLReader<DirectedGraph<Number,Number>, Number, Number> gmlr = 
        	    new GraphMLReader<DirectedGraph<Number,Number>, Number, Number>(vertexFactory, edgeFactory);
        	final DirectedGraph<Number,Number> graph = new DirectedSparseMultigraph<Number,Number>();
        	gmlr.load(filename, graph);
        

//        PajekNetReader<Graph<Number, Number>, Number,Number> pnr = 
//            new PajekNetReader<Graph<Number, Number>, Number,Number>(vertexFactory, edgeFactory);
//        
//        final Graph<Number,Number> graph = new SparseMultigraph<Number, Number>();
//        
//        pnr.load(filename, graph);

		//Create a simple layout frame
        //specify the Fruchterman-Rheingold layout algorithm
		// final AggregateLayout<Number,Number> layout =
		// new AggregateLayout<Number,Number>(new
		// FRLayout<Number,Number>(graph));

//		final AggregateLayout<Number, Number> layout = new AggregateLayout<Number, Number>(
//				new KKLayout<Number, Number>(graph));
        final AggregateLayout<Number, Number> layout = new AggregateLayout<Number, Number>(
    			new KKLayout<>(graph));
        Rectangle rec =  Display.getCurrent().getBounds();
        layout.setSize(new Dimension(rec.width, rec.height));

		vv = new VisualizationViewer<Number,Number>(layout);
		vv.setBackground( Color.white );
		//Tell the renderer to use our own customized color rendering
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaints);
		vv.getRenderContext().setVertexDrawPaintTransformer(new Function<Number,Paint>() {
			public Paint apply(Number v) {
				if(vv.getPickedVertexState().isPicked(v)) {
					return Color.cyan;
				} else {
					return Color.BLACK;
				}
			}
		});

		
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaints);

		vv.getRenderContext().setEdgeStrokeTransformer(new Function<Number,Stroke>() {
                protected final Stroke THIN = new BasicStroke(1);
                protected final Stroke THICK= new BasicStroke(2);
                public Stroke apply(Number e)
                {
                    Paint c = edgePaints.getUnchecked(e);
                    if (c == Color.LIGHT_GRAY)
                        return THIN;
                    else 
                        return THICK;
                }
            });

		//add restart button
		JButton scramble = new JButton("Restart");
		scramble.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Layout<Number, Number> layout = vv.getGraphLayout();
				layout.initialize();
				Relaxer relaxer = vv.getModel().getRelaxer();
				if(relaxer != null) {
					relaxer.stop();
					relaxer.prerelax();
					relaxer.relax();
				}
			}

		});
		
		DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<Number, Number>();
		vv.setGraphMouse(gm);
		
		final JToggleButton groupVertices = new JToggleButton("Group Clusters");

		//Create slider to adjust the number of edges to remove when clustering
		final JSlider edgeBetweennessSlider = new JSlider(JSlider.HORIZONTAL);
        edgeBetweennessSlider.setBackground(Color.WHITE);
		edgeBetweennessSlider.setPreferredSize(new Dimension(210, 50));
		edgeBetweennessSlider.setPaintTicks(true);
		edgeBetweennessSlider.setMaximum(10);
		edgeBetweennessSlider.setMinimum(0);
		edgeBetweennessSlider.setValue(0);
		edgeBetweennessSlider.setMajorTickSpacing(10);
		edgeBetweennessSlider.setPaintLabels(true);
		edgeBetweennessSlider.setPaintTicks(true);

//		edgeBetweennessSlider.setBorder(BorderFactory.createLineBorder(Color.black));
		//TO DO: edgeBetweennessSlider.add(new JLabel("Node Size (PageRank With Priors):"));
		//I also want the slider value to appear
		final JPanel eastControls = new JPanel();
		eastControls.setOpaque(true);
		eastControls.setLayout(new BoxLayout(eastControls, BoxLayout.Y_AXIS));
		eastControls.add(Box.createVerticalGlue());
		eastControls.add(edgeBetweennessSlider);

		final String COMMANDSTRING = "Edges removed for clusters: ";
		final String eastSize = COMMANDSTRING + edgeBetweennessSlider.getValue();
		
		final TitledBorder sliderBorder = BorderFactory.createTitledBorder(eastSize);
		eastControls.setBorder(sliderBorder);
		//eastControls.add(eastSize);
		eastControls.add(Box.createVerticalGlue());
		
		groupVertices.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
					clusterAndRecolor(layout, edgeBetweennessSlider.getValue(), 
							similarColors, e.getStateChange() == ItemEvent.SELECTED);
					vv.repaint();
			}});


		clusterAndRecolor(layout, 0, similarColors, groupVertices.isSelected());

		edgeBetweennessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int numEdgesToRemove = source.getValue();
					clusterAndRecolor(layout, numEdgesToRemove, similarColors,
							groupVertices.isSelected());
					sliderBorder.setTitle(
						COMMANDSTRING + edgeBetweennessSlider.getValue());
					eastControls.repaint();
					vv.validate();
					vv.repaint();
				}
			}
		});

		Container content = getContentPane();
		content.add(new GraphZoomScrollPane(vv));
		JPanel south = new JPanel();
		JPanel grid = new JPanel(new GridLayout(2,1));
		grid.add(scramble);
		grid.add(groupVertices);
		south.add(grid);
		south.add(eastControls);
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		p.add(gm.getModeComboBox());
		south.add(p);
		content.add(south, BorderLayout.SOUTH);
	}

	public void clusterAndRecolor(AggregateLayout<Number,Number> layout,
		int numEdgesToRemove,
		Color[] colors, boolean groupClusters) {
		//Now cluster the vertices by removing the top 50 edges with highest betweenness
		//		if (numEdgesToRemove == 0) {
		//			colorCluster( g.getVertices(), colors[0] );
		//		} else {
		
		Graph<Number,Number> g = layout.getGraph();
        layout.removeAll();

		EdgeBetweennessClusterer<Number,Number> clusterer =
			new EdgeBetweennessClusterer<Number,Number>(numEdgesToRemove);
		Set<Set<Number>> clusterSet = clusterer.apply(g);
		List<Number> edges = clusterer.getEdgesRemoved();
		
		List<Number> tooSmallClusterNodes = new ArrayList<>();
		List<Number> deleteEdges = new ArrayList<>();

		int i = 0;
		//Set the colors of each node so that each cluster's vertices have the same color
		for (Iterator<Set<Number>> cIt = clusterSet.iterator(); cIt.hasNext();) {

			Set<Number> vertices = cIt.next();
				
			if (vertices.size() < 5) {
				for (Number n : vertices) {
					tooSmallClusterNodes.add(n);
					// revmove IN Edgtes
					for (Number inEdge : g.getInEdges(n)) {
						deleteEdges.add(inEdge);
					}
					// remove OUT Edges
					for (Number outEdge : g.getOutEdges(n)) {
						deleteEdges.add(outEdge);
					}
				}
				continue;
			}
			System.out.println(i+"  >> Cluster with # Vertices: "+vertices.size());
			
			Color c = colors[i % colors.length];

			colorCluster(vertices, c);
			if(groupClusters == true) {
				groupCluster(layout, vertices);
			}
			i++;
		}
		for (Number e : g.getEdges()) {

			if (edges.contains(e)) {
				edgePaints.put(e, Color.lightGray);
			} else {
				edgePaints.put(e, Color.black);
			}
		}
		
		removeCluster(tooSmallClusterNodes, deleteEdges, layout);

	}

	private void removeCluster(List<Number> tooSmallClusterNodes, List<Number> deleteEdges, AggregateLayout<Number, Number> layout) {
		
		Graph<Number,Number> g = layout.getGraph();
		
		for (Number n : tooSmallClusterNodes) {
			g.removeVertex(n);
		}
		for (Number edge : deleteEdges) {
			g.removeEdge(edge);
		}
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				
//				for (Number n : tooSmallClusterNodes) {
//					g.removeVertex(n);
//				}
//				for (Number edge : deleteEdges) {
//					g.removeEdge(edge);
//				}
//			}
//		});

//		
		
		
	}

	private void colorCluster(Set<Number> vertices, Color c) {
		for (Number v : vertices) {
			vertexPaints.put(v, c);
		}
	}
	
	private void groupCluster(AggregateLayout<Number,Number> layout, Set<Number> vertices) {
		if(vertices.size() < layout.getGraph().getVertexCount()) {
			Point2D center = layout.apply(vertices.iterator().next());
			Graph<Number,Number> subGraph = SparseMultigraph.<Number,Number>getFactory().get();
			for(Number v : vertices) {
				subGraph.addVertex(v);
			}
			Layout<Number,Number> subLayout = 
				new CircleLayout<Number,Number>(subGraph);
			subLayout.setInitializer(vv.getGraphLayout());
			subLayout.setSize(new Dimension(60,60));

			layout.put(subLayout,center);
			vv.repaint();
		}
	}
}
