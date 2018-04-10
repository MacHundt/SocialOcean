package impl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.ArrowFactory;
import utils.DBManager;
import utils.Lucene;
import utils.TimeLineHelper;


public class GeneralGraphCreator {
	
	private static JPanel graphPanel = null;
	private static VisualizationViewer<MyUser, MyEdge> vv;
	private static UndirectedSparseMultigraph<MyUser, MyEdge> graph;
	private static AggregateLayout<MyUser, MyEdge> layout;
	private static Set<Set<MyUser>> clusterSet;
	
	private static JSlider edgeBetweennessSlider;
	private static JSlider clusterSizeSlider;
	private static JRadioButton local;
	private static JRadioButton global;
	private static ButtonGroup group2;
	
	private static boolean isGlobal = true;
	private static boolean isLocal = false;
	
	private static JRadioButton degree;
	private static JRadioButton betweenness;
	private static ButtonGroup group1;
	
	private static boolean isBetweenness = true;
	private static boolean isDegree = false;
	
	private static JSpinner densitySpinner;
	
	private static ArrayList<MyUser> highlightUser = new ArrayList<>();
	
	private static LoadingCache<MyUser, Paint> vertexPaints =
			CacheBuilder.newBuilder().build(
					CacheLoader.from(Functions.<Paint>constant(Color.white))); 
	private static LoadingCache<MyEdge, Paint> edgePaints =
			CacheBuilder.newBuilder().build(
					CacheLoader.from(Functions.<Paint>constant(Color.blue)));
	
//	private static LoadingCache<MyUser, Paint> selectedVertexPaints =
//			CacheBuilder.newBuilder().build(
//					CacheLoader.from(Functions.<Paint>constant(Color.red))); 
	
	
	private static int topK = 5;
	static boolean ASC = true;
	static boolean DESC = false;
	
	private static Color node = new Color(124,119,119);
//	private static Color highCentrality = new Color(0, 0, 255);
	private static Color highCentrality = new Color(91, 46, 255,255);		// Broadcast
	private static Color supportColor = new Color(136,86,167);			// Support
//	private static Color highCentrality = new Color(102, 255, 51,200);
	private static Color group1Color = new Color(138, 136, 131);
	
	private static double centrThreshold = 0.59;
	
	private static Color highDensity = new Color(237,76,81,255);
//	private static Color highDensity = new Color(197,27,138,200);
//	private static double denThreshold = 0.5;
	
	private static Color edge = new Color(0,0,0);
	
	private static DecimalFormat df = new DecimalFormat("#.00");
	
	private static boolean addToSelection = false;
	
	
//	public final static Color[] similarColors =	
//		{
//			new Color(124,119,119, 200)
//			
////			new Color(216, 134, 134),
////			new Color(135, 137, 211),
////			new Color(134, 206, 189),
////			new Color(206, 176, 134),
////			new Color(194, 204, 134),
////			new Color(145, 214, 134),
////			new Color(133, 178, 209),
////			new Color(103, 148, 255),
////			new Color(60, 220, 220),
////			new Color(30, 250, 100)
//		};
	
	
	public static JPanel getGraphPanel() {
		if (graphPanel != null) {
			return graphPanel;
		} else {
			// the whole container
			graphPanel = new JPanel(true);
			graphPanel.setLayout(new BorderLayout());
			
			// the Graph
//			graph = new DirectedSparseMultigraph<MyUser, MyEdge>();
			graph = new UndirectedSparseMultigraph<MyUser, MyEdge>();
			
			// the Graph Layout			// KKLayout
			layout = new AggregateLayout<MyUser, MyEdge>(new SpringLayout2<>(graph));
			// take whole screen size 
			
			Rectangle rec = Display.getCurrent().getBounds();
			layout.setSize(new Dimension(rec.width, rec.height));
//			layout.setSize(new Dimension(1000, 900));
			
			
			vv = new VisualizationViewer<MyUser, MyEdge>(layout);
			vv.setBackground(Color.white);
			vv.setSize(new Dimension(rec.width+50, rec.height+50));
			//Tell the renderer to use our own customized color rendering
			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaints);
			
			vv.getRenderContext().setVertexDrawPaintTransformer(new Function<MyUser,Paint>() {
				public Paint apply(MyUser v) {
					if(vv.getPickedVertexState().isPicked(v)) {
						return Color.YELLOW;
					} else {
//						return new Color(node.getRed(),node.getGreen(),node.getBlue(), v.getAlpha());
						return Color.BLACK;
					}
				}
				
			});
			
			vv.getRenderContext().setVertexLabelTransformer(new Function<MyUser,String>(){
				public String apply(MyUser v) {
					
					if(vv.getPickedVertexState().isPicked(v)) {
						return v.getName();
					} else {
						return "";
					}
				}
				
			});
			
			vv.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
			
			vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaints);

			vv.getRenderContext().setArrowDrawPaintTransformer(new Function<MyEdge,Paint>() {
				public Paint apply(MyEdge v) {
					if(vv.getPickedEdgeState().isPicked(v)) {
						return Color.YELLOW;
					} else {
						return new Color(edge.getRed(),edge.getGreen(),edge.getBlue(), v.getAlpha()); 
					}
				}
				
			});
			
			
			vv.getRenderContext().setEdgeDrawPaintTransformer(new Function<MyEdge,Paint>() {
				public Paint apply(MyEdge v) {
					if(vv.getPickedEdgeState().isPicked(v)) {
						return Color.YELLOW;
					} else {
						return new Color(edge.getRed(),edge.getGreen(),edge.getBlue(), v.getAlpha()); 
					}
				}
				
			});
			
			
			vv.getRenderContext().setEdgeStrokeTransformer(new Function<MyEdge,Stroke>() {
                protected final Stroke THIN = new BasicStroke(1);
                protected final Stroke THICK= new BasicStroke(2);
                protected final Stroke HIGH= new BasicStroke(4);
                protected final Stroke HIGHER= new BasicStroke(6);
                protected final Stroke HIGHHIGH= new BasicStroke(8);
                protected final Stroke HIGHHIGHHIGH= new BasicStroke(12);
                public Stroke apply(MyEdge e)
                {
                    Paint c = edgePaints.getUnchecked(e);
                    if (c == Color.YELLOW)
                        return THIN;
                    else {
                    	if (e.getCount() > 15)
                            return HIGHHIGHHIGH;
                    	if (e.getCount() > 8)
                            return HIGHHIGH;
                    	else if (e.getCount() > 4)
                          return HIGHER;
                    	 else if (e.getCount() > 2)
                    		 return HIGH;
                    	 else
                        return THICK;
                    }
                    
                   
                }
            });
			
			
			 // Probably the most important step for the pure rendering performance:
	        // Disable anti-aliasing
	        vv.getRenderingHints().remove(RenderingHints.KEY_ANTIALIASING);

	        MyModalGraphMouse<MyUser, MyEdge> gm = new MyModalGraphMouse<MyUser, MyEdge>();
			vv.setGraphMouse(gm);
			
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
			p.add(gm.getModeComboBox());
			p.setToolTipText("Switch Mouse Mode with pushing SHIFT");
			
			
			vv.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					if (e.getKeyCode() == 18 ) {
						if (gm.getModeComboBox().getSelectedItem().equals(Mode.PICKING))
							gm.setMode(Mode.TRANSFORMING);
						else if (gm.getModeComboBox().getSelectedItem().equals(Mode.TRANSFORMING))
							gm.setMode(Mode.PICKING);
					}
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == 16 ) {
						addToSelection = false;
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					
//					System.out.println(e.getKeyCode());
					if (e.getKeyCode() == 16 ) {
						addToSelection = true;
					}
					
				}
			});
			
			vv.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// MouseEvent.BUTTON3 --> Right Click
					
					if  (e.getButton() == 3 && gm.getModeComboBox().getSelectedItem().equals(Mode.TRANSFORMING)) {
						gm.setMode(Mode.PICKING);
						gm.getModeComboBox().updateUI();
					} 
//					else if (e.getButton() == 3 && gm.getModeComboBox().getSelectedItem().equals(Mode.PICKING) 
//							&& !addToSelection) {
//						gm.setMode(Mode.TRANSFORMING);
//						gm.getModeComboBox().updateUI();
//					}
					else if (e.getButton() == 1) {
						gm.setMode(Mode.TRANSFORMING);
						gm.getModeComboBox().updateUI();
					}
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					
//					System.out.println("Pressed Button: "+e.getButton());
//					if (e.getButton() == 3 ) {
//						gm.setMode(Mode.PICKING);
//						gm.getModeComboBox().updateUI();
//					}
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					
					if (e.getButton() == 3) {
						gm.setMode(Mode.PICKING);
						gm.getModeComboBox().updateUI();
					}
					else if (e.getButton() == 1) {
						gm.setMode(Mode.TRANSFORMING);
						gm.getModeComboBox().updateUI();
					}
					
				}
			});
			
			
			// The clustSizeSlider
			clusterSizeSlider = new JSlider(JSlider.HORIZONTAL);
			clusterSizeSlider.setBackground(Color.WHITE);
			clusterSizeSlider.setPreferredSize(new Dimension(170, 50));
			clusterSizeSlider.setPaintTicks(true);
			clusterSizeSlider.setMaximum(10);				// set Max again, after graph changed
			clusterSizeSlider.setMinimum(1);
			clusterSizeSlider.setValue(3);
			clusterSizeSlider.setMajorTickSpacing(1);
			clusterSizeSlider.setPaintLabels(true);
			clusterSizeSlider.setPaintTicks(true);
			
			final JPanel clusterControls = new JPanel();
			clusterControls.setOpaque(true);
			clusterControls.setLayout(new BoxLayout(clusterControls, BoxLayout.Y_AXIS));
			
			clusterControls.add(Box.createVerticalGlue());
			clusterControls.add(clusterSizeSlider);
			
			final String MINCLUSTER = "min Cluster size: ";
			final String clusterSize = MINCLUSTER + clusterSizeSlider.getValue();
			
			final TitledBorder csliderBorder = BorderFactory.createTitledBorder(clusterSize);
			clusterControls.setBorder(csliderBorder);
			clusterControls.add(Box.createVerticalGlue());
			clusterControls.add(Box.createVerticalGlue());
			clusterControls.add(clusterSizeSlider);
			
			clusterSizeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						clusterAndRecolor(true);
						csliderBorder.setTitle(
								MINCLUSTER + clusterSizeSlider.getValue());
						clusterControls.repaint();
						vv.validate();
						vv.repaint();
					}
				}
			});
			
			
			final JPanel group1Panel = new JPanel();
			group1Panel.setOpaque(true);
			group1Panel.setLayout(new BoxLayout(group1Panel, BoxLayout.Y_AXIS));
			group1Panel.setBackground(highCentrality);
//			group1Panel.setForeground(highCentrality);
			
			// Graph
			group1 = new ButtonGroup();
			betweenness = new JRadioButton("betweenness");
			betweenness.setSelected(isBetweenness);
			betweenness.setToolTipText("The betweenness score counts how often a node has to traverse this node, \nin order to reach all others by the shortest path. ");
			degree = new JRadioButton("degree");
			degree.setSelected(isDegree);
			degree.setToolTipText("The degree score counts the amount of edges per node");
			
			group1.add(betweenness);
			group1.add(degree);
			
			betweenness.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					isBetweenness = betweenness.isSelected();
					isDegree = degree.isSelected();
					recolorUsers();
				}
			});
			
			
			degree.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					isBetweenness = betweenness.isSelected();
					isDegree = degree.isSelected();
					recolorUsers();
				}
			});
			
			
			group1Panel.add(Box.createVerticalGlue());
			group1Panel.add(betweenness);
			group1Panel.add(degree);
			final String centrality = "Centrality: ";
			final TitledBorder scoreBorder = BorderFactory.createTitledBorder(centrality);
			group1Panel.setBorder(scoreBorder);
			group1Panel.setToolTipText("BLUE: <Broadcast> (high Out-Degree), PURPLE: <Support> (high In-Degree)");
			
			
			final JPanel group2Panel = new JPanel();
			group2Panel.setOpaque(true);
			group2Panel.setLayout(new BoxLayout(group2Panel, BoxLayout.Y_AXIS));
			group2Panel.setBackground(highCentrality);
			
			
			// Global - Local Switch
			group2 = new ButtonGroup();
			global = new JRadioButton("global");
			global.setToolTipText("Min-Max scaling for whole graph");
			global.setSelected(isGlobal);
			local = new JRadioButton("local");
			local.setToolTipText("Min-Max scaling for each connected component");
			local.setSelected(isLocal);
			
			group2.add(global);
			group2.add(local);
			
			global.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					isGlobal = global.isSelected();
					isLocal = local.isSelected();
					recolorUsers();
				}
			});
			
			
			local.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					isGlobal = global.isSelected();
					isLocal = local.isSelected();
					recolorUsers();
				}
			});
			
			
			group2Panel.add(Box.createVerticalGlue());
			group2Panel.add(global);
			group2Panel.add(local);
			final String EXTENT = "Extent: ";
			final TitledBorder extentBorder = BorderFactory.createTitledBorder(EXTENT);
			group2Panel.setBorder(extentBorder);
			
			
			final JPanel densityPanel = new JPanel();
			densityPanel.setOpaque(true);
			densityPanel.setLayout(new BoxLayout(densityPanel, BoxLayout.Y_AXIS));
			
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0.5, 0.01, 1, 0.01);
			densitySpinner = new JSpinner(spinnerModel);
			
			densitySpinner.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					recolorUsers();
				}
			});
			
			densitySpinner.addMouseWheelListener(new MouseWheelListener() {
				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					if (e.getWheelRotation() > 0 ) {
						double value = (double)densitySpinner.getValue() + 0.01;
						if (value >= 1)
							value = 1;
						densitySpinner.setValue(value);
						recolorUsers();
					} else {
						double value = (double)densitySpinner.getValue() - 0.01;
						if (value <= 0)
							value = 0;
						densitySpinner.setValue(value);
						recolorUsers();
					}
				}
			});
			
			densitySpinner.setToolTipText("Set the density threshold for a graph declared as dense.");
			
			densityPanel.add(Box.createVerticalGlue());
			densityPanel.add(densitySpinner);
			densityPanel.setBackground(highDensity);
//			densityPanel.setForeground(highDensity);
			final String DENSITY = "Density: ";
			final TitledBorder densityBorder = BorderFactory.createTitledBorder(DENSITY);
			densityPanel.setBorder(densityBorder);
			
			
			graphPanel.add(new GraphZoomScrollPane(vv), BorderLayout.CENTER);
			JPanel south = new JPanel();
			
			
			south.add(p);
			south.add(clusterControls);
//			south.add(eastControls);
			south.add(group1Panel);
			south.add(group2Panel);
			south.add(densityPanel);
			graphPanel.add(south, BorderLayout.SOUTH);
			
			return graphPanel;
		}
		
	}
	
	
	public static void highlightUsers(ArrayList<MyUser> foundUsers) {
		highlightUser = foundUsers;
		vv.fireStateChanged();
		
//		vv.getRenderContext().setVertexLabelTransformer(new Function<MyUser,String>(){
//			
//			public String apply(MyUser v) {
//				if(highlightUser.contains(v)) {
//					return "***"+v.getName()+"***";
//				} else {
//					return "";
//				}
//			}
//		});
	}
	
	public static void highlightEdges(ArrayList<MyEdge> foundEdges) {
		
	}


	public static void createSimpleGraph(ScoreDoc[] result, IndexSearcher searcher, boolean withMention,
			boolean withFollows) {
		
		clearGraph();
		
		vv.setVisible(false);
		HashMap<String, MyUser> nodeNames = new HashMap<>(); // screenName -> id
		HashMap<String, MyUser> sources = new HashMap<>();

		HashMap<String, Integer> edgesMap = new HashMap<>();
		int nodesCounter = 0;
		
		try {
			for (ScoreDoc doc : result) {
				int docID = doc.doc;
				Document document = searcher.doc(docID);
//				String type = (document.getField("type")).stringValue();
//				String uId = (document.getField("uid")).stringValue();
//				long tweetdate = Long.parseLong((document.getField("date")).stringValue());
			
				String id = (document.getField("id")).stringValue();						// tweet_id
				String hasMentionSrg = (document.getField("has@") != null) ? (document.getField("has@")).stringValue() : null;
				if (hasMentionSrg == null) 
					continue;
				boolean hasMention =  ( hasMentionSrg == "true") ? true : false;
				String mentionString = (document.getField("mention") != null) ? (document.getField("mention")).stringValue() : null;
				if (mentionString == null)
					continue;
				String screenName = (document.getField("name")).stringValue().trim();

				// EDGE information
				boolean hasGeo = false;
				double lat = 0;
				double lon = 0;
				if (document.getField("geo") != null) {
					long hashgeo = (document.getField("geo")).numericValue().longValue();
					lat = GeoPointField.decodeLatitude(hashgeo);
					lon = GeoPointField.decodeLongitude(hashgeo);
					if (lat != 0.0 || lon != 0.0) {
						hasGeo = true;
					}
				}
					
				// ADD source node
				MyUser nodeID = null;
				if (!nodeNames.containsKey(screenName)) {
					nodeID = new MyUser("n" + nodesCounter++, screenName);
					// ...
					graph.addVertex(nodeID);
					nodeNames.put(screenName, nodeID);
					sources.put(screenName, nodeID);
					
				} else {
					nodeID = nodeNames.get(screenName);
				}
				MyUser sourceID = nodeID;
				
				
				// ADD target nodes
				String[] mentions = mentionString.split(" ");
				
				if (!hasMention && mentionString.isEmpty()) {
					Lucene l = Lucene.INSTANCE;
					if (l.isOnlyGeo() && !hasGeo ) 
						continue;
					
					// Self-Edge for every tweet without mention
					MyEdge edge = null;
					// ADD Edge: source to Target
					String edgesNames = "" + sourceID.getId() + "_" + sourceID.getId();
					sourceID.incOutDegree();
					sourceID.incInDegree();
					int count = 1;
					if (edgesMap.containsKey(edgesNames)) {
						edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
						count = edgesMap.get(edgesNames);
					} else {
						edgesMap.put(edgesNames, new Integer(1));
					}

					// Every Edge Unique!
					edge = new MyEdge(id);
					edge.changeToString(MyEdge.LabelType.SentiStrength);
					
					edge.setCount(count);

					if (hasGeo) {
						edge.addPoint(lat, lon);
					}
					
					// Self-Edge
					graph.addEdge(edge, sourceID, sourceID);
				}
				
				if (withMention) {
					for (String target : mentions) {

						if (target.isEmpty())
							continue;
						
						Lucene l = Lucene.INSTANCE;
						if (l.isOnlyGeo() && !hasGeo ) 
							continue;

						target = target.replaceAll("[:']", "").trim();

						if (!nodeNames.containsKey(target)) {
							nodeID = new MyUser("n" + nodesCounter++, target);

							graph.addVertex(nodeID);
							nodeNames.put(target, nodeID);

						} else {
							nodeID = nodeNames.get(target);
						}

						MyEdge edge = null;
						// ADD Edge: source to Target
						sourceID.incOutDegree();
						nodeID.incInDegree();
						String edgesNames = "" + sourceID.getId() + "_" + nodeID.getId();
						int count = 1;
						if (edgesMap.containsKey(edgesNames)) {
							edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
							count = edgesMap.get(edgesNames);
						} else {
							edgesMap.put(edgesNames, new Integer(1));
						}

						// Every Edge Unique!
						edge = new MyEdge(id); 
						edge.changeToString(MyEdge.LabelType.SentiStrength);
						
						edge.setCount(count);
						

						if (hasGeo)
							edge.addPoint(lat, lon);
						
						// No self-mention-Edges
						if (!sourceID.getName().equals(nodeID.getName())) {
							if (graph.containsEdge(edge)) {
								// TODO add edges .. tweet_id to MyEdge
								System.out.println("Self-Mention-Edge: "+edge.getId()+" already exists ..");
							}
							else {
								graph.addEdge(edge, sourceID, nodeID);
							}
						} 

					}
				}
			}
			if (withFollows) {
				addAllFollows(nodeNames, edgesMap);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		clusterAndRecolor(edgeBetweennessSlider.getValue(), similarColors, true);
		clusterAndRecolor(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				vv.validate();
				vv.repaint();
				vv.setVisible(true);
//				System.out.println(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale());
//				System.out.println(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale());
				vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setScale(0.2, 0.2, vv.getCenter());
				
				
//				double amount = 1.0;    // Or negative to zoom out.
//				ScalingControl scaler = new CrossoverScalingControl();
//				scaler.scale(vv, amount > 0 ? 1.1f : 1 / 1.1f, vv.getCenter());
//				vv.scaleToLayout(scaler);
				
				double amount = -1.0;    // Or negative to zoom out.
				ScalingControl scaler = new CrossoverScalingControl();
				scaler.scale(vv, amount < 0 ? 1.1f : 1 / 1.1f, vv.getCenter());
				vv.scaleToLayout(scaler);
			}
		});
		
	}


	/**
	 * Removes all vertices and edges from the graph
	 */
	public static void clearGraph() {
		MyEdge[] edgearr = new MyEdge[graph.getEdgeCount()];
		graph.getEdges().toArray(edgearr);
		for (int i = 0; i < edgearr.length; i++) {
			MyEdge e = edgearr[i];
			graph.removeEdge(e);
		}
		
		MyUser[] userarr = new MyUser[graph.getVertexCount()];
		graph.getVertices().toArray(userarr);
		for (int i = 0; i < userarr.length; i++) {
			MyUser v = userarr[i];
			graph.removeVertex(v);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				vv.repaint();
			}
			
		});
		
		
		
	}
	
	
	private static void addAllFollows(HashMap<String, MyUser> nodeNames, HashMap<String, Integer> edgesMap) {

		try {
			Connection c = DBManager.getConnection();
			String table = DBManager.getTweetdataTable();
			Statement stmt = c.createStatement();
			for (String name : nodeNames.keySet()) {

				// get all follows
				String query = "Select target, tweet_id, latitude, longitude From " + table
						+ " where user_screenname = '" + name + "' and relationship = 'Followed'";
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					// see if target is in keySet --> true add an edge
					String target = rs.getString("target");
					String id = rs.getString("tweet_id");
					if (nodeNames.keySet().contains(target)) {
						// add edge
						MyEdge edge = null;
						// ADD Edge: source to Target
						((MyUser) nodeNames.get(name)).incOutDegree();
						((MyUser) nodeNames.get(target)).incInDegree();
						String edgesNames = "" + ((MyUser) nodeNames.get(name)).getId() + "_"
								+ ((MyUser) nodeNames.get(target)).getId();
						if (edgesMap.containsKey(edgesNames)) {
							edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
						} else {
							edgesMap.put(edgesNames, new Integer(1));
						}

						edge = new MyEdge(id);
						edge.addCredibility(0);
						edge.addCategory("");
						edge.addSentiment("neu");
						edge.addDate(null);
						edge.setRelationsip("Followed");
//						edge.addSource(name);
//						edge.addTarget(target);
						
						graph.addEdge(edge, nodeNames.get(name), nodeNames.get(target));
					}
				}
			}
			stmt.close();
			c.close();
		} catch (SQLException e) {
			System.out.println("No Followed Edges!!");
			return;
		}

	}
	
	
	
	/**
	 * Finds mentions within a text
	 * @param text_content
	 * @return mentions concatenated with " "
	 */
	private static String getMentionsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("@")) {
				output += token.substring(1) + " ";
			}
		}
		return output.trim();
	}
	
	
	private static double createZipfScore(String inputContent) {
		
		double score = 1.0;   	// --> this is normal
		
		// dic --> remove whitespace
		// to lower case
		String input_wo_white = inputContent.replaceAll(" ", "").toLowerCase();
		int ch_count = 0;
		HashMap<String, Integer> dic = new HashMap<>();
		for (char c : input_wo_white.toCharArray()) {
			ch_count++;
			if (dic.containsKey(c+"")) {
				int old = dic.get(c+"");
				dic.put(c+"", old + 1);
			} else {
				dic.put(c+"", 1);
			}
		}
//		System.out.println(dic.toString());
		// Sort
		Map<String, Integer> sortedMapAsc = sortByComparator(dic, DESC);
//		System.out.println(sortedMapAsc.toString());
		
		
		int disjointChars = dic.size();
		// very few used chars: 3  
		if ( disjointChars < 4 ) {
//			System.out.println("ABNORMAL >> "+inputContent);
			score = score - 0.7;										
		}
		
		int topScore = 0;
		for (Entry<String, Integer> e : sortedMapAsc.entrySet()) {
			topScore = e.getValue();
			break;
		}
		
		double topScoreOccurrenc = Math.round(topScore / (double) ch_count );
		// 1/3 of all chars is this char
		if (topScoreOccurrenc > 0.4) {
//			System.out.println("ABNORMAL >> "+inputContent);
			score = score - 0.8;	
		}
		
		
		int charsToLookat = (int)Math.min(topK, Math.ceil(Math.log(topScore)));
		if (charsToLookat < 2) {
//			System.out.println("ABNORMAL >> "+inputContent);
			score = score - 0.3;
		}
		
		
		return Math.abs(score);
	}
	
	
	
	public static void recolorUsers() {
		
		if (graph.getEdgeCount() == 0)
			return;
		
		if (clusterSet == null) {
			return;
		}
		
		double UBScore = 0.0;
		int maxDeg = 0;
		double globalCentrality = 0.0;
		long sum = 0;
		int hInDegree = 0;
		int hOoutDegree = 0;
		for (MyUser u : graph.getVertices()) {
			int degScore = u.getDegree();
			double betScore = u.getBetweennessScore();
			u.addBetweennessScore(betScore);
			u.addDegree(degScore);
			if (isBetweenness)
				sum += betScore;
			else if (isDegree)
				sum += degScore;
			
			if (betScore > UBScore)
            {
				UBScore = betScore;
            }
			if (degScore > maxDeg) {
				maxDeg = degScore;
			}
			if (u.getInDegree() > hInDegree) {
				hInDegree = u.getInDegree();
			}
			if (u.getOutDegree() > hOoutDegree) {
				hOoutDegree = u.getOutDegree();
			}
		}
		
		if (isBetweenness) {
			globalCentrality =  (UBScore / sum);
		}
		else if (isDegree) {
			globalCentrality = (maxDeg / (double) sum);
		}
		String degreeType = ( hOoutDegree > hInDegree) ? "Broadcast" : "Support";
		
		vertexPaints.cleanUp();
		int isolateCounter = 0;
		int i = 0;
		for (Iterator<Set<MyUser>> cIt = clusterSet.iterator(); cIt.hasNext();) {
			i++;
			HashSet<MyEdge> uniqueEdges = new HashSet<>();
			
			Set<MyUser> vertices = cIt.next();
			if (vertices.size() == 1) 
				isolateCounter++;
			
			Color nodeColor = node;
			if (isGlobal) {
				if (globalCentrality > centrThreshold) {
					nodeColor = highCentrality;
					if (degreeType.equals("Support"))
						nodeColor = supportColor;
				}
			}
			
			double maxBet = 0.0;
			int localDeg = 0;
			long localSum = 0;
			double localCentrality = 0.0;
			int lhinDegree = 0;
			int lhOutDegree = 0;
			for (MyUser u : vertices) {
				
				u.addClusterID(i);
				Collection<MyEdge> local_edges = graph.getOutEdges(u);
				if (local_edges != null )
					for (MyEdge e : local_edges) {
						uniqueEdges.add(e);
					}
				
				if (isBetweenness) 
					localSum += u.getBetweennessScore();
				else if (isDegree) 
					localSum += u.getDegree();
				
				if (u.getBetweennessScore() > maxBet)
					maxBet = u.getBetweennessScore();
				if (u.getDegree() > localDeg)
					localDeg = u.getDegree();
				if (u.getInDegree() > lhinDegree) {
					lhinDegree = u.getInDegree();
				}
				if (u.getOutDegree() > lhOutDegree) {
					lhOutDegree = u.getOutDegree();
				}
			}
			
			maxBet = (maxBet == 0) ? 1 : maxBet;
			localDeg = (localDeg == 0) ? 1 : localDeg;
			
			if (isBetweenness) {
				localCentrality =  (localSum == 0)? 0 : (maxBet / (double) localSum);
			}
			else if (isDegree) {
				localCentrality = (localSum == 0) ? 0 : (localDeg / (double) localSum);
			}
			String localdegreeType = ( lhOutDegree > lhinDegree) ? "Broadcast" : "Support";
			
			double localDensity = 0.0;
			if ( vertices.size() > 1 )
				localDensity = (2 * uniqueEdges.size()) / (double) (vertices.size() * (vertices.size()-1));
//			System.out.println("Local_Centrality of Components: "+localCentrality);
			if (isLocal) {
				if (localCentrality > centrThreshold) {
					nodeColor = highCentrality;
					if (localdegreeType.equals("Support"))
						nodeColor = supportColor;
				} else {
					if (localDensity >= (double)densitySpinner.getValue()) {
						nodeColor = highDensity;
					}
				}
			}
			
			for (MyUser u : vertices) {
				// scale
				double a = 0.0;
				
				// LOCAL
				if (isLocal) {
					if (isBetweenness)
						a = u.getBetweennessScore() / maxBet;
					else if (isDegree)
						a = u.getDegree() / localDeg;
				}
				// GLOABL
				else if(isGlobal) {
					if (isBetweenness)
						a = u.getBetweennessScore() / UBScore;	
					else if (isDegree)
						a = u.getDegree() / maxDeg;
				}
				
				// set Default ( no division by 0 )
				a = (a == 0.0) ? 2 : a * 255;
				a = Math.abs((Math.log(a) / Math.log(255)) * 255);
				a = ( a > 255 ) ? 255 : a;
				u.addAlpha((int) a);
				
				Color co = new Color(nodeColor.getRed(),nodeColor.getGreen(),nodeColor.getBlue(), (int) a );
				vertexPaints.put(u, co);
			}
			
		}
		
		vv.repaint();
		
	}


	public static void clusterAndRecolor(boolean groupClusters) {
		
		if (graph.getEdgeCount() == 0)
			return;
		
		boolean tooMuch = false;
		if (graph.getEdgeCount() > 15000 & graph.getVertexCount() > 10000)
			tooMuch = true;
		
		double UBScore = 0.0;
		double EBscore = 0.0;
		int maxDeg = 0;
		double globalCentrality = 0.0;
		long sum = 0;
		int hInDegree = 0;
		int hOoutDegree = 0;
		
		if (!tooMuch) {
			BetweennessCentrality<MyUser, MyEdge> bc = new BetweennessCentrality<MyUser, MyEdge>(graph);
			for (MyEdge e : graph.getEdges()) {
				double score = bc.getEdgeScore(e);
				if (score > EBscore) {
					EBscore = score;
					e.addBetweennessScore(EBscore);
				}
			}

			DegreeScorer<MyUser> deg = new DegreeScorer<>(graph);

			for (MyUser u : graph.getVertices()) {
				int degScore = deg.getVertexScore(u);
				double betScore = bc.getVertexScore(u);
				u.addBetweennessScore(betScore);
				u.addDegree(degScore);
				if (isBetweenness)
					sum += betScore;
				else if (isDegree)
					sum += degScore;

				if (betScore > UBScore) {
					UBScore = betScore;
				}
				if (degScore > maxDeg) {
					maxDeg = degScore;
				}
				if (u.getInDegree() > hInDegree) {
					hInDegree = u.getInDegree();
				}
				if (u.getOutDegree() > hOoutDegree) {
					hOoutDegree = u.getOutDegree();
				}
				
			}
		} else {
			// just do Degree Centrality
			DegreeScorer<MyUser> deg = new DegreeScorer<>(graph);

			for (MyUser u : graph.getVertices()) {
				int degScore = deg.getVertexScore(u);
				u.addDegree(degScore);
				if (isDegree)
					sum += degScore;

				if (degScore > maxDeg) {
					maxDeg = degScore;
				}
				if (u.getInDegree() > hInDegree) {
					hInDegree = u.getInDegree();
				}
				if (u.getOutDegree() > hOoutDegree) {
					hOoutDegree = u.getOutDegree();
				}
			}
		}
		
		if (isBetweenness) {
			globalCentrality =  (UBScore / sum);
		}
		else if (isDegree) {
			globalCentrality = (maxDeg / (double) sum);
		}
		
		double globalDensitiy = (2 * graph.getEdgeCount()) / (double) ((graph.getVertexCount() * (graph.getVertexCount()-1)));
		String degreeType = ( hOoutDegree > hInDegree) ? "Broadcast" : "Support";
	
		System.out.println("Global_Centrality: "+df.format(globalCentrality));
		System.out.println("Global_Density: "+ df.format(globalDensitiy));
				
		Color nodeColor = node;
		if (isGlobal)
			if (globalCentrality > centrThreshold) {
				nodeColor = highCentrality;
				if (degreeType.equals("Support"))
					nodeColor = supportColor;
			}
			
		Graph<MyUser, MyEdge> g = layout.getGraph();
        layout.removeAll();

		EdgeBetweennessClusterer<MyUser, MyEdge> clusterer =
			new EdgeBetweennessClusterer<MyUser, MyEdge>(0);
		clusterSet = clusterer.apply(g);
		List<MyEdge> edges = clusterer.getEdgesRemoved();
		
		List<MyUser> tooSmallClusterNodes = new ArrayList<>();
		List<MyEdge> deleteEdges = new ArrayList<>();

		int i = 0;
		int isolateCounter = 0;
		for (Iterator<Set<MyUser>> cIt = clusterSet.iterator(); cIt.hasNext();) {
			
			HashSet<MyEdge> uniqueEdges = new HashSet<>();
			
			Set<MyUser> vertices = cIt.next();
			if (vertices.size() == 1) 
				isolateCounter++;
			
			if (vertices.size() < clusterSizeSlider.getValue()) {
				for (MyUser n : vertices) {
					tooSmallClusterNodes.add(n);
					// revmove IN Edges
					for (MyEdge inEdge : g.getInEdges(n)) {
						deleteEdges.add(inEdge);
					}
					// remove OUT Edges
					for (MyEdge outEdge : g.getOutEdges(n)) {
						deleteEdges.add(outEdge);
					}
				}
				continue;
			}
			System.out.print(i+"  >> Cluster with # Vertices: "+vertices.size());
			
//			Color c = colors[i % colors.length];
//			colorCluster(vertices, c);
			
			double maxBet = 0.0;
			int localDeg = 0;
			long localSum = 0;
			double localCentrality = 0.0;
			int lhinDegree = 0;
			int lhOutDegree = 0;
			
			for (MyUser u : vertices) {
				
				Collection<MyEdge> local_edges = graph.getOutEdges(u);
				for (MyEdge e : local_edges) {
					uniqueEdges.add(e);
					if (edges.contains(e)) {
						edgePaints.put(e, Color.cyan);
					} else {
						double b = 0.0;
						b = e.getBetweennessScore() / EBscore;
						b = (b == 0.0) ? 5 : b * 255;
						b = (int) Math.abs((Math.log(b) / Math.log(255)) * 255);
						
						b = (b > 255) ? 255 : b;
						b = (b < 0) ? 0 : b;

						Color c = new Color(edge.getRed(), edge.getGreen(), edge.getBlue(), (int) b);
						e.addAlpha((int) b);

						edgePaints.put(e, c);
					}
				}
				
				if (isBetweenness) 
					localSum += u.getBetweennessScore();
				else if (isDegree) 
					localSum += u.getDegree();
				
				if (u.getBetweennessScore() > maxBet)
					maxBet = u.getBetweennessScore();
				if (u.getDegree() > localDeg)
					localDeg = u.getDegree();
				if (u.getInDegree() > lhinDegree) {
					lhinDegree = u.getInDegree();
				}
				if (u.getOutDegree() > lhOutDegree) {
					lhOutDegree = u.getOutDegree();
				}
			}
			
			if (isBetweenness) {
				localCentrality =  (localSum == 0)? 0 : (maxBet / (double) localSum);
			}
			else if (isDegree) {
				localCentrality = (localSum == 0)? 0 : (localDeg / (double) localSum);
			}
			String localdegreeType = ( lhOutDegree > lhinDegree) ? "Broadcast" : "Support";
			
			maxBet = (maxBet == 0) ? 1 : maxBet;
			localDeg = (localDeg == 0) ? 1 : localDeg;
			
			double localDensity = 0.0;
			if ( vertices.size() > 1 )
				localDensity = (2 * uniqueEdges.size()) / (double) (vertices.size() * (vertices.size()-1));
			String denseOut = (localCentrality > centrThreshold ) ? "" : " >> Local_Density: "+df.format(localDensity );
			System.out.println(" >> Centrality: "+df.format(localCentrality) + " " + denseOut );
			
			if (isLocal) {
				nodeColor = node;
				if (localCentrality > centrThreshold) {
					nodeColor = highCentrality;
					if (localdegreeType.equals("Support"))
						nodeColor = supportColor;
				} else {
					if (localDensity >= (double)densitySpinner.getValue()) {
						nodeColor = highDensity;
					}
				}
			}
			
			for (MyUser u : vertices) {
				// scale
				double a = 0.0;
				u.addClusterID(i);
				
				// LOCAL
				if (isLocal) {
					if (isBetweenness)
						a = u.getBetweennessScore() / maxBet;
					else if (isDegree)
						a = u.getDegree() / localDeg;
				}
				// GLOABL
				else if(isGlobal) {
					if (isBetweenness) {
						a = u.getBetweennessScore() / UBScore;	
						
					}
					else if (isDegree)
						a = u.getDegree() / maxDeg;
				}
				
				// set Default ( no division by 0 )
				a = (a == 0.0) ? 2 : a * 255;
				a = Math.abs((Math.log(a) / Math.log(255)) * 255);
				a = ( a > 255 ) ? 255 : a;
				u.addAlpha((int) a);
				
				Color co = new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(), (int) a );
				vertexPaints.put(u, co);
			}
			
			if(groupClusters == true) {
				groupCluster(layout, vertices);
			}
			i++;
			
		}
		
//		DEBUG:
//		for (MyEdge e : g.getEdges()) {
//
//			if (edges.contains(e)) {
//				edgePaints.put(e, Color.cyan);
//			} else {
//				double b = 0.0;
//				b = e.getBetweennessScore() / EBscore;
//				b = (b == 0.0) ? 5 : b * 255;
//				b = Math.abs((Math.log(b) / Math.log(255)) * 255);
//				
////				System.out.println(b);
//				
//				Color c = new Color(edge.getRed(),edge.getGreen(),edge.getBlue(), (int) b);
//				e.addAlpha((int) b);
//				
//				edgePaints.put(e, c);
//			}
//		}
		
		Lucene l = Lucene.INSTANCE;
		System.out.println("Isolates: "+isolateCounter + " -- Isolate ratio: "+ String.format( "%.2f",  (isolateCounter)/ (double) graph.getVertexCount() ));
		l.printlnToConsole("Isolate ratio: "+ String.format( "%.2f",  (isolateCounter)/ (double) graph.getVertexCount() ));
		
		removeCluster(tooSmallClusterNodes, deleteEdges, layout);

		// set Size depending on #cluster
//		int dim = i*i;
//		if (i < 21)
//			layout.setSize(new Dimension(400, 400 ));
//		else
//			layout.setSize(new Dimension(i*12, i*10 ));
	}

	
	
	private static void colorCluster(Set<MyUser> vertices, Color c) {
		for (MyUser v : vertices) {
			vertexPaints.put(v, c);
		}
	}
	
	
	private static void groupCluster(AggregateLayout<MyUser, MyEdge> layout, Set<MyUser> vertices) {
		if(vertices.size() < layout.getGraph().getVertexCount()) {
			Point2D center = layout.apply(vertices.iterator().next());
			Graph<MyUser,MyEdge> subGraph = SparseMultigraph.<MyUser,MyEdge>getFactory().get();
			for(MyUser v : vertices) {
				subGraph.addVertex(v);
			}
//			Layout<MyUser,MyEdge> subLayout = 
//				new CircleLayout<MyUser, MyEdge>(subGraph);
			
			Layout<MyUser,MyEdge> subLayout = 
					new ISOMLayout<MyUser, MyEdge>(subGraph);
			
			
			subLayout.setInitializer(vv.getGraphLayout());
			subLayout.setSize(new Dimension(80,80));

			layout.put(subLayout,center);
			vv.repaint();
		}
	}
	

	private static void removeCluster(List<MyUser> tooSmallClusterNodes, List<MyEdge> deleteEdges, AggregateLayout<MyUser, MyEdge> layout) {
		
		Graph<MyUser, MyEdge> g = layout.getGraph();
		
		for (MyUser n : tooSmallClusterNodes) {
			g.removeVertex(n);
		}
		for (MyEdge edge : deleteEdges) {
			g.removeEdge(edge);
		}
		
	}
	
	public static void showSingleDetailofEdge(JComponent root, MyEdge edge) {
		
		if (root == null)
			root = graphPanel;
		
		int breakLineAT = 80;
		String table = DBManager.getTweetdataTable();
		String details = "";
		ImageIcon icon = null;
		Connection c = DBManager.getConnection();
		try {
			Statement stmt = c.createStatement();
			String query = "select t.user_screenname, t.relationship, t.tweet_content, t.positive, t.negative, t.tweet_creationdate, t.sentiment, "
					+ "t.category," + (table.startsWith("nodexl") ? "" : "t.tweet_source, ")
					+ " cscore, t.tweet_retweetcount, t.hasurl, t.urls from " + table
					+ " as t where t.tweet_id = " + edge.getId();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String scName = rs.getString("user_screenname");
				String relationship = rs.getString("relationship");
				String content = rs.getString("tweet_content");
				double cscore = rs.getDouble("cscore");
				double betweenness = edge.getBetweennessScore();
				String date = rs.getString("tweet_creationdate");
				String cd = date.split(" ")[0];
				Date dt = new Date(Integer.parseInt(cd.split("-")[0]), (Integer.parseInt(cd.split("-")[1])) -1 , Integer.parseInt(cd.split("-")[2]));
				String sentiment = rs.getString("sentiment");
				double pos = rs.getDouble("positive");
				double neg = rs.getDouble("negative");
				int reweeted = rs.getInt("tweet_retweetcount");
				
				String category = rs.getString("category");
				boolean hasUrl = rs.getBoolean("hasurl");
				String urls = rs.getString("urls");
				String device = (table.startsWith("nodexl") ? "" : rs.getString("tweet_source"));

				if (relationship.equals("Followed")) {
					details += "\n" + scName + " follows ";
					break;
				}
				device = device.substring(device.lastIndexOf("/")+1);

				details += "\n" + scName + " (" + device + ") wrote on " + date + ":\n";
				if (content.length() > breakLineAT) {
					// next line
					details += "\n\"" + content.substring(0, breakLineAT);
					details += "\n" + content.substring(breakLineAT, content.length()) + "\"\n";
				} else {
					details += "\n\"" + content +"\"\n";
				}
				
				edge.addSentiment(sentiment);
				edge.addCategory(category);
				edge.addContent(content);
				edge.addCredibility(cscore);
				edge.addDate(dt);
				edge.addDevice(device);
				ArrayList<String> url_list = new ArrayList<>();
				if (!urls.isEmpty()) {
					for (String url : urls.split(" ")) {
						url_list.add(url);
					}
				}
				edge.addUrls(url_list);
				
				icon =  DetailedGraphCreator.getTweetIcon(edge);
				
				sentiment += " (" + (int)pos + "," + (int)neg + ")";
				
				details += "\nCategory: \t" + category;
				details += "\nSentiment: \t" + sentiment;
				details += "\n\nCredibilty: ";
				details += "\nContent Credibility Score: \t" + df.format(cscore);
				details += "\nBetweenness Score: \t" + df.format(cscore);
				details += "\nRetweeted: \t" + reweeted+" times";
				// details += "\nhasURL: \t" + ((hasUrl) ? "true" : "false");
				details += (hasUrl) ? "\nURLs: \t" + urls : "";
			}
			stmt.close();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(root, details, "Details Tweet", JOptionPane.PLAIN_MESSAGE, icon );
	}
	
	
	public static void showSingleDetailofUser(JComponent root,  MyUser user) {
		
		if (root == null)
			root = graphPanel;
		
		Lucene l = Lucene.INSTANCE;
		// Max values: hard coded
		double maxFollow = Math.log10(8448672);				// loged
		double maxFriends = Math.log10(290739);
		double maxMessage = Math.log10(625638);
		long maxDate = l.getUser_maxDate();
		long minDate = l.getUser_minDate();
		
		LocalDateTime latestTweet = l.getLatestTweetDate();
		LocalDateTime dt = latestTweet;
		int breakLineAT = 80;
		String table = DBManager.getUserTable();

		ImageIcon icon = null;
		String details = "";
		Connection c = DBManager.getConnection();
			try {
				Statement stmt = c.createStatement();
				String query = "select u.user_creationdate, u.user_language, u.user_statusescount, u.user_followerscount, "
						+ "u.user_friendscount, u.user_listedcount, u.gender, u.desc_score, u.user_location, "
						+ "geocoding_type from " + table
						+ " as u where u.user_screenname = '" + user.getName()+"'";
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					String scName = user.getName();
					String gender = rs.getString("gender");
					String creationdate = rs.getString("user_creationdate");
//					// DATE
//					String cd = creationdate.split(" ")[0];
					long time = TimeLineHelper.creationDateToLong(creationdate); 	// the bigger the 'older' the user
					
					dt = TimeLineHelper.longTOLocalDateTime(time);
					
					String user_language = rs.getString("user_language");
					String user_location = rs.getString("user_location");
					
					int statuses = rs.getInt("user_statusescount");
					int followers = rs.getInt("user_followerscount");
					int friends = rs.getInt("user_friendscount");
					int listed = rs.getInt("user_listedcount");
					
					double desc_score = rs.getDouble("desc_score");
					int geocoding_type = rs.getInt("geocoding_type");
					
//					double sc_follow =  Math.log10(followers) / maxFollow;
//					double sc_friend =  Math.log10(friends) / maxFriends;
//					double sc_tweets =  Math.log10(statuses) / maxMessage;
//					double sc_time = ((Math.log(time) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000))) / 
//							((Math.log(maxDate) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)));
////					
//					double credible  = 1 - ((sc_follow + sc_friend +sc_tweets + sc_time + desc_score) / 5.0);
					
					// ratio = 1: I follow nobody (no friends), but many follow me
					// ratio -> 0: I follow many (many friends), but nobody follows me
					double follow_friend_ratio = followers / (double) (followers+friends);
					
					int Age_year = latestTweet.minusYears(dt.getYear()).getYear();
					int Age_month = latestTweet.minusMonths(dt.getMonthValue()).getMonthValue();
					int Age_day = latestTweet.minusDays(dt.getDayOfMonth()).getDayOfMonth();
					
					user.addGender(gender);
					user.addStatuses(statuses);
					user.addFollowers(followers);
					user.addFriends(friends);
					user.addListed(listed);
					
					user.addDescScore(desc_score);
					user.addGeocodingType(geocoding_type);
					
					user.addCreationDate(creationdate);
					
					icon =  DetailedGraphCreator.getUserIcon(user);
					
					details += "\nUSER: " + scName + " ("+gender+"):";
					details += "\n\nCreated on: \t" + creationdate;
					// Get "Age" of user
					details += "\nAge: \t" + ((Age_year > 0) ?  Age_year+" years " : "") + 
							((Age_month > 0) ?  Age_month+" months " : "") + 
							((Age_day > 0) ?  Age_day+" days " : "");
					details += "\nUser Language: \t" + user_language;
					details += "\nUser Location: \t" + user_location;
					// get more cred-values
					details += "\n\nCredibility:";
					details += "\nDescription Score: \t" + df.format(desc_score);
					details += "\nGeocoding of Location, Type: \t" + geocoding_type +"\n\t ("+getExplanation(geocoding_type)+")";
//					details += "\nCredibility: \t" + df.format(credible);
					details += "\nFollow-Friend Ratio: \t" + df.format(follow_friend_ratio);
					
					details += "\n\nStatistics:\n (#Tweets, #Followers, #Friends, #Listed):  \t (" + statuses+", "+followers+", "+friends+", "+listed+")";
				}
				stmt.close();
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if (details.isEmpty()) {
				details += "\nUSER: " + user.getName();
			}
			
			JOptionPane.showMessageDialog(root, details, "Details User", JOptionPane.PLAIN_MESSAGE, icon );
	}
	
	
	private static String getExplanation(int geocoding_type) {
		String geocoding = "Unknown";
		
		switch (geocoding_type) {
		case 1:
			geocoding = "Valid coordinates with matching timezone";
			break;
		case 2:
			geocoding = "Valid coordinates";
			break;
		case 3:
			geocoding = "Valid city with matching timezone";
			break;
		case 4:
			geocoding = "Matching timezone: centroid of timezone-shape";
			break;
		case 5:
			geocoding = "Valid city, take highest population";
			break;
		case 6:
			geocoding = "Like a cityname, take longest match and highest population";
			break;
		case 7:
			geocoding = "Like a timezone or a countryname: centroid of timezone shape";
			break;

		default:
			geocoding = "Unknown";
			break;
		}
		
		return geocoding;
	}


	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
	{
	    List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());
	    // Sorting the list based on values
	    Collections.sort(list, new Comparator<Entry<String, Integer>>()
	    {
	        public int compare(Entry<String, Integer> o1,
	                Entry<String, Integer> o2)
	        {
	            if (order)
	            {
	                return o1.getValue().compareTo(o2.getValue());
	            }
	            else
	            {
	                return o2.getValue().compareTo(o1.getValue());

	            }
	        }
	    });

	    // Maintaining insertion order with the help of LinkedList
	    Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	    for (Entry<String, Integer> entry : list)
	    {
	        sortedMap.put(entry.getKey(), entry.getValue());
	    }

	    return sortedMap;
	}


	public static Collection<MyEdge> getEdges() {
		
		return graph.getEdges();
	}
}
