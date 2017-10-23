package impl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.eclipse.e4.ui.workbench.UIEvents.Application;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import socialocean.parts.Histogram;
import utils.DBManager;
import utils.Lucene;


public class GraphPanelCreator {

	private static JPanel graphPanel = null;
	private static VisualizationViewer<MyUser, MyEdge> vv;
	private static DirectedGraph<MyUser, MyEdge> graph;
	private static AggregateLayout<MyUser, MyEdge> layout;
	private static JSlider edgeBetweennessSlider;
	private static JSlider clusterSizeSlider;
	
	private static LoadingCache<MyUser, Paint> vertexPaints =
			CacheBuilder.newBuilder().build(
					CacheLoader.from(Functions.<Paint>constant(Color.white))); 
	private static LoadingCache<MyEdge, Paint> edgePaints =
			CacheBuilder.newBuilder().build(
					CacheLoader.from(Functions.<Paint>constant(Color.blue)));
	
	
	private static int topK = 5;
	static boolean ASC = true;
	static boolean DESC = false;
	
	public final static Color[] similarColors =	
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
	
	
	public static JPanel getGraphPanel() {
		if (graphPanel != null) {
			return graphPanel;
		} else {
			// the whole container
			graphPanel = new JPanel(true);
			graphPanel.setLayout(new BorderLayout());
			
			// the Graph
			graph = new DirectedSparseMultigraph<MyUser, MyEdge>();
			
			// the Graph Layout
			layout = new AggregateLayout<MyUser, MyEdge>(new KKLayout<>(graph));
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
						return Color.cyan;
					} else {
						return Color.BLACK;
					}
				}
			});
			
			vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaints);

			vv.getRenderContext().setEdgeStrokeTransformer(new Function<MyEdge,Stroke>() {
	                protected final Stroke THIN = new BasicStroke(1);
	                protected final Stroke THICK= new BasicStroke(2);
	                public Stroke apply(MyEdge e)
	                {
	                    Paint c = edgePaints.getUnchecked(e);
	                    if (c == Color.LIGHT_GRAY)
	                        return THIN;
	                    else 
	                        return THICK;
	                }
	            });

			DefaultModalGraphMouse<MyUser, MyEdge> gm = new DefaultModalGraphMouse<MyUser, MyEdge>();
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
			//eastControls.add(eastSize);
			clusterControls.add(Box.createVerticalGlue());
			clusterControls.add(Box.createVerticalGlue());
			clusterControls.add(clusterSizeSlider);
			
			clusterSizeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						int numEdgesToRemove = source.getValue();
//						clusterAndRecolor(layout, numEdgesToRemove, similarColors,
//								groupVertices.isSelected());
						clusterAndRecolor(numEdgesToRemove, similarColors, true);
						csliderBorder.setTitle(
								MINCLUSTER + clusterSizeSlider.getValue());
						clusterControls.repaint();
						vv.validate();
						vv.repaint();
					}
				}
			});
			
			
			// The edgeBetweenessSlider
			edgeBetweennessSlider = new JSlider(JSlider.HORIZONTAL);
	        edgeBetweennessSlider.setBackground(Color.WHITE);
			edgeBetweennessSlider.setPreferredSize(new Dimension(210, 50));
			edgeBetweennessSlider.setPaintTicks(true);
			edgeBetweennessSlider.setMaximum(50);				// set Max again, after graph changed
			edgeBetweennessSlider.setMinimum(0);
			edgeBetweennessSlider.setValue(0);
			edgeBetweennessSlider.setMajorTickSpacing(10);
			edgeBetweennessSlider.setPaintLabels(true);
			edgeBetweennessSlider.setPaintTicks(true);
			
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
			
//			final JToggleButton groupVertices = new JToggleButton("Group Clusters");
//			groupVertices.addItemListener(new ItemListener() {
//				public void itemStateChanged(ItemEvent e) {
//						clusterAndRecolor(layout, edgeBetweennessSlider.getValue(), 
//								similarColors, e.getStateChange() == ItemEvent.SELECTED);
//						vv.repaint();
//				}});


			edgeBetweennessSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						int numEdgesToRemove = source.getValue();
//						clusterAndRecolor(layout, numEdgesToRemove, similarColors,
//								groupVertices.isSelected());
						clusterAndRecolor(numEdgesToRemove, similarColors, true);
						sliderBorder.setTitle(
							COMMANDSTRING + edgeBetweennessSlider.getValue());
						eastControls.repaint();
						vv.validate();
						vv.repaint();
					}
				}
			});
			
			graphPanel.add(new GraphZoomScrollPane(vv), BorderLayout.CENTER);
			JPanel south = new JPanel();
//			JPanel grid = new JPanel(new GridLayout(2,1));
//			grid.add(groupVertices);
//			south.add(grid);
			south.add(clusterControls);
			south.add(eastControls);
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
			p.add(gm.getModeComboBox());
			south.add(p);
			graphPanel.add(south, BorderLayout.SOUTH);
			
			return graphPanel;
		}
		
	}


	public static void createGraph(ScoreDoc[] result, IndexSearcher searcher, boolean withMention,
			boolean withFollows) {
		
		clearGraph();
		
		vv.setVisible(false);
		
		
		HashMap<String, MyUser> nodeNames = new HashMap<>(); // screenName -> id
		HashMap<String, MyUser> sources = new HashMap<>();

		HashMap<String, Integer> edgesMap = new HashMap<>();
		int nodesCounter = 0;
		Lucene l = Lucene.INSTANCE;
		
		try {
			Connection c = DBManager.getConnection();
			String table = DBManager.getTweetdataTable();
			String userTable = DBManager.getUserTable();
			Statement stmt = c.createStatement();
			
			// Calibrate Min-Max for normalized user_creation
			ZoneId zonedId = ZoneId.of( "Europe/Berlin" );
			LocalDate today = LocalDate.now( zonedId );
			long now = Date.UTC(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0, 0, 0);
			double maxFollow = Math.log10(8448672);				// loged
			double maxFriends = Math.log10(290739);
			double maxMessage = Math.log10(625638);
			long maxDate = l.getUser_maxDate();
			long minDate = l.getUser_minDate();
			
			
			for (ScoreDoc doc : result) {
				int docID = doc.doc;
				Document document = searcher.doc(docID);
				String type = (document.getField("type")).stringValue();
				String id = (document.getField("id")).stringValue();
				long tweetdate = Long.parseLong((document.getField("date")).stringValue());

				// EDGE information
				String query = "";
				String screenName = "";
				String content = "";
//				double sentiment = 0;
				String posStrength = (document.getField("pos")).stringValue();
				String negStrength = (document.getField("neg")).stringValue();
				int pos = (posStrength.isEmpty()) ? 0 : Integer.parseInt((document.getField("pos")).stringValue());
				int neg = (negStrength.isEmpty()) ? 0 : Integer.parseInt((document.getField("neg")).stringValue());
				
				String sentiment = "neu";
				
				String category = "other";
				boolean hasUrl = false;
				boolean isRetreet = false; // --> replyusername != null
				String relationship = "";
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
					

				// NODE information --> user meta data ..
				int listedCount = 0;
				// Datetime
				long datetime = 0;
				int friends = 0;
				int followers = 0;
				int tweetCount = 0;
				// today - userCreationDate
				long time = 0;
				java.util.Date date = null;
				String nodexl_target = "";
				
				switch (type) {
				// get the tweet
				case "twitter":
					query = "Select "
						+ "t.user_screenname, t.tweet_content, t.sentiment, t.positive, t.negative, t.category, t.hasurl, t.relationship "
						+ " from "+table+" as t where t.tweet_id = "
						+ Long.parseLong(id);
					
					break;
				case "flickr":
//					query = "Select t.sentiment from flickrdata as t where t.\"photoID\" = " + Long.parseLong(id);
					break;
				}
				ResultSet rs = stmt.executeQuery(query);
				
				String language = "en";
				String geom = "";
				boolean isEmpty = true;
				while (rs.next()) {
//					isEmpty = false;
//					screenName = rs.getString(1);
//					content = rs.getString(2);
//					sentiment = rs.getInt(3);
//					category = rs.getString(4);
//					hasUrl = rs.getBoolean(5);
//					isRetreet = (rs.getString(6).equals("null")) ? false : true;
//					listedCount = rs.getInt(7);
//					// DATE
//					String cd = rs.getString(8).split(" ")[0];
//					Date dt = new Date(Integer.parseInt(cd.split("-")[0]), (Integer.parseInt(cd.split("-")[1])) -1 , Integer.parseInt(cd.split("-")[2]));
//					time = dt.getTime(); 	// the bigger the 'older' the user
//					friends = rs.getInt(9);
//					followers = rs.getInt(10);
//					tweetCount = rs.getInt(11);
//					language = rs.getString(12);
//					geom = rs.getString(13);
					
					screenName = rs.getString(1);
					content = rs.getString(2);
					sentiment = rs.getString(3);
					pos = rs.getInt(4);
					neg = rs.getInt(5);
					category = rs.getString(6);
					category = category.replace(" & ", "_").toLowerCase();
					hasUrl = rs.getBoolean(7);
					
				}
				if (sentiment == null)
					sentiment = "neu";
				
				// get Colors
				String colorString = "";
				if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
					org.eclipse.swt.graphics.Color color = Histogram.getCategoryColor(category);
					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
				}
				else {
					org.eclipse.swt.graphics.Color color = new org.eclipse.swt.graphics.Color(Display.getDefault(), 255,255,191);
					colorString = "#fee08b";
//					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
					
					if (sentiment.equals("pos")) {
						color = color = new org.eclipse.swt.graphics.Color(Display.getDefault(), 26,152,80);
//						colorString = "green";
						colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
					}
					else if (sentiment.equals("neg")) {
						color = color = new org.eclipse.swt.graphics.Color(Display.getDefault(), 215,48,39);
//						colorString = "red";
						colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
					}
				}
				
				
//				double sc_follow =  Math.log10(followers) / maxFollow;
//				double sc_friend =  Math.log10(friends) / maxFriends;
//				double sc_tweets =  Math.log10(tweetCount) / maxMessage;
//				double sc_time = ((Math.log(time) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000))) / ((Math.log(maxDate) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)));
//				double credible  = 1 - ((sc_follow + sc_friend +sc_tweets + sc_time) / 4.0);
				
				String mentionString = (content != null) ? getMentionsFromTweets(content) : "";
				// continue, no target, no mentions
				if (mentionString.length() < 2) {
					continue;
				}
				
				// ADD source node
				MyUser nodeID = null;
				if (!nodeNames.containsKey(screenName)) {
					nodeID = new MyUser("n" + nodesCounter++, screenName);
					nodeID.addLanguage(language);
					
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

						target = target.replace(":", "");

						if (!nodeNames.containsKey(target)) {
							nodeID = new MyUser("n" + nodesCounter++, screenName);

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
						edge.addCategory(category);
						edge.addSentiment(sentiment);
						edge.addPos(pos);
						edge.addNeg(neg);
						edge.changeToString(MyEdge.LabelType.SentiStrenth);

						if (hasGeo)
							((MyEdge) edge).addPoint(lat, lon);
						
						graph.addEdge(edge, sourceID, nodeID);

					}
				}
			}
			
			stmt.close();
			c.close();
			if (withFollows) {
//				addAllFollows(nodeNames, edgesMap);
			}
			
			
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		
		clusterAndRecolor(edgeBetweennessSlider.getValue(), similarColors, true);
		vv.validate();
		vv.repaint();
		vv.setVisible(true);
		
	}


	/**
	 * Removes all vertices and edges from the graph
	 */
	private static void clearGraph() {
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


	public static void clusterAndRecolor(int numEdgesToRemove,
		Color[] colors, boolean groupClusters) {
		//Now cluster the vertices by removing the top 50 edges with highest betweenness
		//		if (numEdgesToRemove == 0) {
		//			colorCluster( g.getVertices(), colors[0] );
		//		} else {
		
		if (graph.getEdgeCount() == 0)
			return;
		
		
		Graph<MyUser, MyEdge> g = layout.getGraph();
        layout.removeAll();

		EdgeBetweennessClusterer<MyUser, MyEdge> clusterer =
			new EdgeBetweennessClusterer<MyUser, MyEdge>(numEdgesToRemove);
		Set<Set<MyUser>> clusterSet = clusterer.apply(g);
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
			System.out.println(i+"  >> Cluster with # Vertices: "+vertices.size());
			
			Color c = colors[i % colors.length];

			colorCluster(vertices, c);
			if(groupClusters == true) {
				groupCluster(layout, vertices);
			}
			i++;
		}
		for (MyEdge e : g.getEdges()) {

			if (edges.contains(e)) {
				edgePaints.put(e, Color.lightGray);
			} else {
				edgePaints.put(e, Color.black);
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
			Layout<MyUser,MyEdge> subLayout = 
				new CircleLayout<MyUser, MyEdge>(subGraph);
			
			subLayout.setInitializer(vv.getGraphLayout());
			subLayout.setSize(new Dimension(60,60));

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
