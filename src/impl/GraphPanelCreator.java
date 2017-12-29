package impl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
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
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import utils.DBManager;


public class GraphPanelCreator {
	
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
	private static Color highCentrality = new Color(0, 0, 255);
	private static double centrThreshold = 0.59;
	
	private static Color edge = new Color(0,0,0);
	
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
                public Stroke apply(MyEdge e)
                {
                    Paint c = edgePaints.getUnchecked(e);
                    if (c == Color.YELLOW)
                        return THIN;
                    else 
                        return THICK;
                }
            });
			
			
			 // Probably the most important step for the pure rendering performance:
	        // Disable anti-aliasing
	        vv.getRenderingHints().remove(RenderingHints.KEY_ANTIALIASING);
			

	        MyModalGraphMouse<MyUser, MyEdge> gm = new MyModalGraphMouse<MyUser, MyEdge>();
			vv.setGraphMouse(gm);
			
			
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
			
			
			final JPanel group2Panel = new JPanel();
			group2Panel.setOpaque(true);
			group2Panel.setLayout(new BoxLayout(group2Panel, BoxLayout.Y_AXIS));
			
			
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
			
			
			graphPanel.add(new GraphZoomScrollPane(vv), BorderLayout.CENTER);
			JPanel south = new JPanel();
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
			p.add(gm.getModeComboBox());
			south.add(p);
			south.add(clusterControls);
//			south.add(eastControls);
			south.add(group1Panel);
			south.add(group2Panel);
			graphPanel.add(south, BorderLayout.SOUTH);
			
			return graphPanel;
		}
		
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
				String mentionString = (document.getField("mention")).stringValue();
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
				if (withMention) {
					for (String target : mentions) {

						if (target.isEmpty())
							continue;

						target = target.replace(":", "").trim();

						if (!nodeNames.containsKey(target)) {
							nodeID = new MyUser("n" + nodesCounter++, target);

							graph.addVertex(nodeID);
							nodeNames.put(target, nodeID);

						} else {
							nodeID = nodeNames.get(target);
						}

						MyEdge edge = null;
						// ADD Edge: source to Target
						String edgesNames = "" + sourceID.getId() + "_" + nodeID.getId();
						if (edgesMap.containsKey(edgesNames)) {
							edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
						} else {
							edgesMap.put(edgesNames, new Integer(1));
						}

						edge = new MyEdge(id); 
						edge.changeToString(MyEdge.LabelType.SentiStrenth);

						if (hasGeo)
							edge.addPoint(lat, lon);
						
						// No self-Edges
						if (!sourceID.getName().equals(nodeID.getName())) {
							if (graph.containsEdge(edge)) {
								// TODO add edges .. tweet_id to MyEdge
								System.out.println("TEST");
							}
							else 
								graph.addEdge(edge, sourceID, nodeID);
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
					double lon = rs.getDouble("longitude");
					double lat = rs.getDouble("latitude");
					boolean hasGeo = false;
					if (lat != 0.0 || lon != 0.0) {
						hasGeo = true;
					}
					if (nodeNames.keySet().contains(target)) {
						// add edge
						MyEdge edge = null;
						// ADD Edge: source to Target

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
						if (hasGeo)
							edge.addPoint(lat, lon);
						
						graph.addEdge(edge, nodeNames.get(name), nodeNames.get(target));

					}
				}
			}
			stmt.close();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
		}
		
		if (isBetweenness) {
			globalCentrality =  (UBScore / sum);
		}
		else if (isDegree) {
			globalCentrality = (maxDeg / (double) sum);
		}
		
//		System.out.println("Global_Centrality: "+globalCentrality);
		
		vertexPaints.cleanUp();
		
		for (Iterator<Set<MyUser>> cIt = clusterSet.iterator(); cIt.hasNext();) {
			Set<MyUser> vertices = cIt.next();
			
			Color nodeColor = node;
			if (isGlobal) {
				if (globalCentrality > centrThreshold) {
					nodeColor = highCentrality;
				}
			}
			
			double maxBet = 0.0;
			int localDeg = 0;
			long localSum = 0;
			double localCentrality = 0.0;
			for (MyUser u : vertices) {
				if (isBetweenness) 
					localSum += u.getBetweennessScore();
				else if (isDegree) 
					localSum += u.getDegree();
				
				if (u.getBetweennessScore() > maxBet)
					maxBet = u.getBetweennessScore();
				if (u.getDegree() > localDeg)
					localDeg = u.getDegree();
			}
			
			maxBet = (maxBet == 0) ? 1 : maxBet;
			localDeg = (localDeg == 0) ? 1 : localDeg;
			
			if (isBetweenness) {
				localCentrality =  (localSum == 0)? 0 : (maxBet / (double) localSum);
			}
			else if (isDegree) {
				localCentrality = (localSum == 0)? 0 : (localDeg / (double) localSum);
			}
			
//			System.out.println("Local_Centrality of Components: "+localCentrality);
			if (isLocal) {
				if (localCentrality > centrThreshold) {
					nodeColor = highCentrality;
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
		
		BetweennessCentrality<MyUser, MyEdge> bc = new BetweennessCentrality<MyUser, MyEdge>(graph);
		double EBscore = 0.0;
		for (MyEdge e : graph.getEdges()) {
			double score = bc.getEdgeScore(e);
			if ( score > EBscore)
            {
				EBscore = score;
				e.addBetweennessScore(EBscore);
            }
		}
		
		DegreeScorer<MyUser> deg = new DegreeScorer<>(graph);
		
		double UBScore = 0.0;
		int maxDeg = 0;
		double globalCentrality = 0.0;
		long sum = 0;
		for (MyUser u : graph.getVertices()) {
			int degScore = deg.getVertexScore(u);
			double betScore = bc.getVertexScore(u);
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
		}
		
		if (isBetweenness) {
			globalCentrality =  (UBScore / sum);
		}
		else if (isDegree) {
			globalCentrality = (maxDeg / (double) sum);
		}
		
		System.out.println("Global_Centrality: "+globalCentrality);
		
		Color nodeColor = node;
		if (isGlobal)
			if (globalCentrality > centrThreshold) {
				nodeColor = highCentrality;
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
		//Set the colors of each node so that each cluster's vertices have the same color
		for (Iterator<Set<MyUser>> cIt = clusterSet.iterator(); cIt.hasNext();) {

			Set<MyUser> vertices = cIt.next();
				
			if (vertices.size() < clusterSizeSlider.getValue()) {
				for (MyUser n : vertices) {
					tooSmallClusterNodes.add(n);
					// revmove IN Edgtes
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
			for (MyUser u : vertices) {
				if (isBetweenness) 
					localSum += u.getBetweennessScore();
				else if (isDegree) 
					localSum += u.getDegree();
				
				if (u.getBetweennessScore() > maxBet)
					maxBet = u.getBetweennessScore();
				if (u.getDegree() > localDeg)
					localDeg = u.getDegree();
			}
			
			
			
			if (isBetweenness) {
				localCentrality =  (localSum == 0)? 0 : (maxBet / (double) localSum);
			}
			else if (isDegree) {
				localCentrality = (localSum == 0)? 0 : (localDeg / (double) localSum);
			}
			
			maxBet = (maxBet == 0) ? 1 : maxBet;
			localDeg = (localDeg == 0) ? 1 : localDeg;
			
			System.out.println(" >> Centrality: "+localCentrality);
			
			if (isLocal) {
				nodeColor = node;
				if (localCentrality > centrThreshold) {
					nodeColor = highCentrality;
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
					if (isBetweenness) {
						a = u.getBetweennessScore() / UBScore;	
						
					}
					else if (isDegree)
						a = u.getDegree() / maxDeg;
				}
				
				// set Default ( no division by 0 )
				a = (a == 0.0) ? 2 : a * 255;
				a = Math.abs((Math.log(a) / Math.log(255)) * 255);
				u.addAlpha((int) a);
				
				Color co = new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(), (int) a );
				vertexPaints.put(u, co);
			}
			
			if(groupClusters == true) {
				groupCluster(layout, vertices);
			}
			i++;
		}
		for (MyEdge e : g.getEdges()) {

			if (edges.contains(e)) {
				edgePaints.put(e, Color.cyan);
			} else {
				double b = 0.0;
				b = e.getBetweennessScore() / EBscore;
				b = (b == 0.0) ? 5 : b * 255;
				b = Math.abs((Math.log(b) / Math.log(255)) * 255);
				
//				System.out.println(b);
				
				Color c = new Color(edge.getRed(),edge.getGreen(),edge.getBlue(), (int) b);
				e.addAlpha((int) b);
				
				edgePaints.put(e, c);
			}
		}
		
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
			subLayout.setSize(new Dimension(70,70));

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
}
