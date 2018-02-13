package impl;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxGraphStructure;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import socialocean.parts.Histogram;
import utils.DBManager;
import utils.Lucene;


public class GraphPanelCreator3 {
	
	private static JPanel graphPanel = null;
	
	private static mxGraph graph = null;
	private static Object parent = null;
	private static MyGraphComponent graphComponent = null;
	private static mxStylesheet stylesheet;
	private static Hashtable<String, Object> followStyle;
	
	private static int topK = 5;
	static boolean ASC = true;
	static boolean DESC = false;
	
	private static ArrayList<MyEdge> edges = new ArrayList<>();
	public static boolean SELECTED = false;
	
	public static ArrayList<MyEdge> allEdges = new ArrayList<>();
	public static ArrayList<MyUser> allUser = new ArrayList<>();
	
	
	public static JPanel getGraphPanel() {
		
		
		if (graphPanel != null) {
			return graphPanel;
		} else {
			
//			graphPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5 ));
			graphPanel = new JPanel(new BorderLayout());
			graph = new mxGraph();
			graph.setCellsEditable(false);
//			graph.setMaximumGraphBounds(new mxRectangle(0, 0, 800, 600));
			parent = graph.getDefaultParent();
			
			
			stylesheet = graph.getStylesheet();
			// defaultEdge: endArrow=classic, shape=connector, fontColor=#446299, strokeColor=#6482B9, align=center, verticalAlign=middle
			followStyle = new Hashtable<String, Object>();
			followStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
			followStyle.put(mxConstants.STYLE_OPACITY, 25);
			followStyle.put(mxConstants.STYLE_DASHED, true );
			followStyle.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER );
			followStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC );
			followStyle.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE );
			followStyle.put(mxConstants.STYLE_FONTCOLOR, "#774400");
			followStyle.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
			stylesheet.putCellStyle("FollowEdge", followStyle);
			
//			graphComponent = new mxGraphComponent(graph);
			
			graphComponent = new MyGraphComponent(graph);
//			graphComponent.setVisible(false);
//			mxICellEditor editor = graphComponent.getCellEditor();
			
			graphPanel.add(graphComponent);
			
//			new mxPanningHandler(graphComponent);
			
			// Selection Handler
			new mxRubberband(graphComponent);
//			new mxSelectionCellsHandler(graphComponent);
			
			graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {

				@Override
				public void invoke(Object sender, mxEventObject evt) {
					int selected = graph.getSelectionCount();
					
					Lucene l = Lucene.INSTANCE;
					if (selected > 0) {
						//TODO show selected in map. 
						// graph .. distinguish between source and only target nodes
						
//						MyUser[] users = new MyUser[selected];
//						for (int i = 0; i<selected; i++	) {
//							Object cell = graph.getSelectionCells()[i];
//							if(cell instanceof mxCell) {
//								if (((mxCell)cell).getValue() instanceof MyUser) {
//									users[i] = (MyUser)((mxCell)cell).getValue();
//								}
//							}
//						}
						SELECTED = true;
						edges = new ArrayList<>();
						for (int i = 0; i<selected; i++	) {
							Object cell = graph.getSelectionCells()[i];
							if(cell instanceof mxCell) {
								if (((mxCell)cell).getValue() instanceof MyEdge) {
									edges.add((MyEdge)((mxCell)cell).getValue());
								}
							}
						}
						
//						System.out.println("Changed !! >> "+selected);
						l.showSelectionInMap(edges, true);
						l.showSelectionInHistogramm(edges);
					}
					// show all again
					else if (selected == 0){
						SELECTED = false;
						l.clearMap();
						l.showClustersInMap(allUser, allEdges);
//						Result lastResult = l.getLastResult();
//						l.createMapMarkers(lastResult.getData(), true);
//						l.changeHistogramm(lastResult.getHistoCounter());
					}
				}
				
			});
			
			graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
			{
				public void mouseReleased(MouseEvent e)
				{
					
//					if (e.getButton() == 3) {
//						pan.mouseReleased(e);
//					}
					
					Object cell = graphComponent.getCellAt(e.getX(), e.getY());
					if (cell != null)
					{
						if(cell instanceof mxCell) {
							if (((mxCell)cell).getValue() instanceof MyUser) {
								MyUser user = ((MyUser)((mxCell)cell).getValue());
								String name = user.toString();
//								System.out.println("User: "+name);
							}
							
							if (((mxCell)cell).getValue() instanceof MyEdge) {
								MyEdge edge = ((MyEdge)((mxCell)cell).getValue());
								String content = edge.getContent();
//								System.out.println("Edge: "+content);
							}
						}
							
					}
				}
				
			});
			
			
//			graphComponent.getViewport().addMouseMotionListener(pan);
//			graphComponent.getViewport().addMouseListener(pan);
			
			
//			graphComponent.getGraphControl().addMouseMotionListener(new MouseMotionListener() {
//				
//				@Override
//				public void mouseMoved(MouseEvent e) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void mouseDragged(MouseEvent e) {
//					
//					// only drag on right click
//					if (e.getButton() == 3) {
//						// EventQueue.invokeLater(new Runnable() {
//						// public void run() {
//						
////						pan.mouseDragged(e);
//						
//						mxPoint p = new mxPoint(
//								Math.abs(lastRootView.getX() + e.getX()), 
//								Math.abs(lastRootView.getY() + e.getY())
//								);
//						
//						graph.getView().setTranslate(p);
//						
//						double absX = Math.abs(pressedPoint.getX() - p.getX());
//						double absY = Math.abs(pressedPoint.getY() - p.getY());
//						pressedPoint = new mxPoint(absX, absY);
//						
////						graph.getView().setTranslate(pressedPoint);
//						lastRootView = pressedPoint;
//						// }
//						// });
//					}
//					
//				}
//			});

			graphComponent.addMouseWheelListener(new MouseWheelListener() {
				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					double scale = graph.getView().getScale();
					if (e.getWheelRotation() < 0) {
						scale = scale + (scale*0.1);
						graphComponent.zoomTo(scale, true);
//						graph.getView().setScale(scale); 
					} else {
						scale = scale - (scale*0.1);
						graphComponent.zoomTo(scale, true);
//						graph.getView().setScale(scale);
					}
				}
			});
			
			return graphPanel;
		}
	}
	
	public static void clearGraph()	{
		
		graph.getModel().beginUpdate();
		allEdges.clear();
		allUser.clear();
		Object[] remove = graph.getChildVertices(parent);
		graph.removeCells(remove, true);
		graph.getModel().endUpdate();
	}
	
	
	public static <V,E> void createDetailGraph(Collection<V> nodes, Collection<E> edges, boolean withMention, boolean withFollows, boolean clear) {
		
		if (nodes.isEmpty())
			return;
		
		Lucene l = Lucene.INSTANCE;
		if (clear) {
			clearGraph();
			l.clearWayPoints();
//			l.clearMap();
		}
		
		HashMap<String, Object> nodeNames = new HashMap<>(); // screenName -> id
		HashMap<String, Object> sources = new HashMap<>();
		HashMap<String, Integer> edgesMap = new HashMap<>();
		
		Connection c = DBManager.getConnection(false, true);
		if (c == null)
			return;
		
		// Get User information, create nodes
		for (V n : nodes) {
			if (n instanceof MyUser) {
				MyUser user = (MyUser) n;
				String user_name = user.getName();
				String id = user.getId();
				
				createUserNode(user_name, id, c, nodeNames, sources);

//				try {
//					String creationdate = "";
//					String user_language = "";
//					String gender = "unknown";
//
//					int user_statuses = 0;
//					int user_lists = 0;
//					int user_friends = 0;
//					int user_follower = 0;
//					double desc_score = 0.0;
//
//					double lati = 0.0;
//					double longi = 0.0;
//					int geocode_type = 10;
//
//					boolean foundUser = false;
//					Statement st = c.createStatement();
//					String query = "Select * from "+DBManager.getUserTable()+" where user_screenname = '" + user_name + "';";
//					ResultSet rs = st.executeQuery(query);
//					while (rs.next()) {
//						foundUser = true;
//						creationdate = rs.getString("user_creationdate");
//						user_language = rs.getString("user_language");
//						desc_score = rs.getDouble("desc_score");
//						gender = rs.getString("gender");
//						user_statuses = rs.getInt("user_statusescount");
//						user_lists = rs.getInt("user_listedcount");
//						user_friends = rs.getInt("user_friendscount");
//						user_follower = rs.getInt("user_followerscount");
//						lati = rs.getDouble("latitude");
//						longi = rs.getDouble("longitude");
//						geocode_type = rs.getInt("geocoding_type");
//					}
//					// a sourceUser --> many details
//					if (foundUser) {
//						// ADD source node
//						Object nodeID = null;
//						if (!nodeNames.containsKey(user_name)) {
//							nodeID = new MyUser(id, user_name);
//							((MyUser)nodeID).addLanguage(user_language);
//							((MyUser)nodeID).addCredibility(desc_score);
//							((MyUser)nodeID).addGender(gender);
//							
//							if (lati != 0.0 || longi != 0.0) {
//								((MyUser)nodeID).addPoint(lati, longi);
//							}
//							
//							((MyUser)nodeID).setNameVisible();
//
//							nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
//									"ROUNDED;strokeColor=white;fillColor=white");
//							nodeNames.put(user_name, nodeID);
//							sources.put(user_name, nodeID);
//
//						} else {
//							nodeID = nodeNames.get(user_name);
//						}
//					} else {
////						NO source node -- add just the screenname
//						Object nodeID = null;
//						if (!nodeNames.containsKey(user_name)) {
//							nodeID = new MyUser(id, user_name);
//							((MyUser)nodeID).setNameVisible();
//
//							nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
//									"ROUNDED;strokeColor=white;fillColor=white");
//							nodeNames.put(user_name, nodeID);
//							sources.put(user_name, nodeID);
//
//						} else {
//							nodeID = nodeNames.get(user_name);
//						}
//					}
//					
//					st.close();
//				} catch (SQLException e1) {
//					e1.printStackTrace();
//				}

			}
		}
		
		int uCounter = 0;
		for (E e : edges) {
			if (e instanceof MyEdge) {
				MyEdge edge = (MyEdge) e;
				long tweet_id = Long.parseLong(edge.getId());
//				String source = edge.getSource();
//				String target = edge.getTarget();
				
				try {
					String source = "";
					String content = "";
					String sentiment = "neu";
					double pos = 0;
					double neg = 0;
					String category = "other";
					boolean hasUrl = false;
					String relationship = "";
					double lati = 0.0;
					double longi = 0.0;
					
					Statement tst = c.createStatement();
					String query = "Select "
							+ "t.user_screenname, t.tweet_content, t.sentiment, t.positive, t.negative, t.category, t.hasurl, t.relationship, "
							+ "t.latitude, t.longitude "
							+ " from "+DBManager.getTweetdataTable()+" as t where t.tweet_id = "+tweet_id;
					
					ResultSet rs = tst.executeQuery(query);
					boolean foundTweet = false;
					while (rs.next()) {
						foundTweet = true;
						source = rs.getString(1);
						content = rs.getString(2);
						sentiment = rs.getString(3);
						pos = rs.getDouble(4);
						neg = rs.getDouble(5);
						category = rs.getString(6);
						category = (category == null)? "other" : category.replace(" & ", "_").toLowerCase();
						hasUrl = rs.getBoolean(7);
						relationship = rs.getString(8);
						lati = rs.getDouble(9);
						longi = rs.getDouble(10);
					}
					if (foundTweet) {
						//create Edge
						
						// is followed?
						if ( relationship.equals("Followed")) {
							// add edge
							Object edgeF = null;
							String target = "";
							// ADD Edge: source to Target
							// get target from DB
							query = "Select target from "+DBManager.getTweetdataTable()+" as t where t.tweet_id = "+tweet_id;
							rs = tst.executeQuery(query);
							while (rs.next()) {
								target = rs.getString(1);
							}
							
							if (nodeNames.get(source) == null) {
								createUserNode(source, "u"+uCounter++, c, nodeNames, sources);
							}
							
							if (nodeNames.get(target) == null) {
								createUserNode(target, "u"+uCounter++, c, nodeNames, sources);
							}

							String edgesNames = "" + ((mxCell) nodeNames.get(source)).getId() + "_"
									+ ((mxCell) nodeNames.get(target)).getId();
							if (edgesMap.containsKey(edgesNames)) {
								edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
							} else {
								edgesMap.put(edgesNames, new Integer(1));
							}

							edgeF = new MyEdge(tweet_id+"");
							((MyEdge) edgeF).addCredibility(0);
							((MyEdge) edgeF).addCategory("");
							((MyEdge) edgeF).addSentiment("neu");
							((MyEdge) edgeF).addDate(null);
							((MyEdge) edgeF).setRelationsip("Followed");

							graph.insertEdge(parent, null, edgeF, nodeNames.get(source), nodeNames.get(target), "FollowEdge"
							// +
							// "exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1;"
							);
						}
						
						
						// GET Colors
						String colorString = "";
						if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
							Color color = Histogram.getCategoryColor(category);
							colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
						} else {
							Color color = new Color(Display.getDefault(), 255, 255, 191);
							colorString = "#fee08b";
							if (sentiment.equals("pos")) {
								color = color = new Color(Display.getDefault(), 26, 152, 80);
								// colorString = "green";
								colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
							} else if (sentiment.equals("neg")) {
								color = color = new Color(Display.getDefault(), 215, 48, 39);
								// colorString = "red";
								colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
							}
						}

						String mentionString = (content != null) ? getMentionsFromTweets(content) : "";
						// continue, no target, no mentions
						if (mentionString.length() < 2) {
							continue;
						}
						
						boolean hasGeo = false;
						if (lati != 0.0 || longi != 0.0) {
							hasGeo = true;
						}
						
						// ADD target nodes
						String[] mentions = mentionString.split(" ");
						if (withMention) {
							for (String target : mentions) {

								if (target.isEmpty())
									continue;

								target = target.replace(":", "");
								Object tnode = null;

								if (!nodeNames.containsKey(target)) {
									tnode = new MyUser(tweet_id+"", target);

									tnode = graph.insertVertex(parent, null, tnode, 0, 0, 40, 40,
											"ROUNDED;strokeColor=white;fillColor=white");
									nodeNames.put(target, tnode);

								} else {
									tnode = nodeNames.get(target);
								}

								Object gedge = null;
								Object ob = nodeNames.get(source);
								
								if (nodeNames.get(source) == null) {
									createUserNode(source, "u"+uCounter++, c, nodeNames, sources);
								}
								
								if (nodeNames.get(target) == null) {
									createUserNode(target, "u"+uCounter++, c, nodeNames, sources);
								}
								Object sourceID = nodeNames.get(source);
								
								// ADD Edge: source to Target
								String edgesNames = "" + ((mxCell) sourceID).getId() + "_" + ((mxCell) tnode).getId();
								if (edgesMap.containsKey(edgesNames)) {
									edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
								} else {
									edgesMap.put(edgesNames, new Integer(1));
								}

								gedge = new MyEdge(tweet_id+"");
								((MyEdge) gedge).addCategory(category);
								((MyEdge) gedge).addSentiment(sentiment);
								((MyEdge) gedge).addPos((int)pos);
								((MyEdge) gedge).addNeg((int)neg);
								((MyEdge) gedge).changeToString(MyEdge.LabelType.SentiStrenth);

								if (hasGeo)
									((MyEdge) gedge).addPoint(lati, longi);

								graph.insertEdge(parent, null, gedge, sourceID, tnode,
										"edgeStyle=elbowEdgeStyle;elbow=horizontal;" + "strokeWidth=3;"
												+ "STYLE_PERIMETER_SPACING;" + "strokeColor=" + colorString
								// +
								// "exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1;"
								);
							}
						}
						
					}
					
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
			}
		}
		
		mxAnalysisGraph anaGraph = new mxAnalysisGraph();
		anaGraph.setGraph(graph);

//		mxGraphStructure struc = new mxGraphStructure();
//		Object[][] cc = struc.getGraphComponents(anaGraph);
//
//		ArrayList<Object[]> filtered = new ArrayList<>();
//		if (cc != null) {
//			for (Object[] o : cc) {
//				if (o != null && o.length >= 3) {
//					filtered.add(o);
//				}
//				 else {
//				 graph.removeCells(o, true);
//				 }
//			}
//
//			// DESC
//			filtered.sort(new Comparator<Object[]>() {
//
//				@Override
//				public int compare(Object[] o1, Object[] o2) {
//					return Integer.compare(o2.length, o1.length);
//				}
//			});
//
//			// Object[] most = filtered.get(0);
//			// filterOutComponents(cc, 2);
//		}
		morphGraph(graph, graphComponent);

		// USERS of EDGES?
		Object[] allnodes = graph.getChildVertices(parent);
//		Object[] alledges = graph.getAllEdges(allnodes);
		
		allEdges = new ArrayList<>();
		allUser = new ArrayList<>();
		
		for (Object o : allnodes) {
			if (o instanceof mxCell) {
				mxCell myCell = (mxCell) o;
				// Add Users
				if (myCell.getValue() instanceof MyUser) {
					MyUser myUs = (MyUser) myCell.getValue();
					allUser.add(myUs);

					Object[] nEdge = graph.getEdges(o);
					for (Object e : nEdge) {
						if (e instanceof mxCell) {
							mxCell myECell = (mxCell) e;
							// Add Edges
							if (myECell.getValue() instanceof MyEdge) {
								MyEdge myEd = (MyEdge) myECell.getValue();
								allEdges.add(myEd);
							}
						}
					}
				}
			}

		}
		
		System.out.println("# Users "+allUser.size()+" selected");
		System.out.println("# Edges "+allEdges.size()+" selected");
		
		// TODO show Edges // Users in Map .. Heatmap, Countries -- take allEdges // allUsers as parameter!!
		l.showClustersInMap(allUser, allEdges);
		
		l.initCountriesMap();
		
	}
	
	
	
	
	
	private static void createUserNode(String user_name, String id, Connection c, HashMap<String, Object> nodeNames,
			HashMap<String, Object> sources) {
		
		try {
			String creationdate = "";
			String user_language = "";
			String gender = "unknown";

			int user_statuses = 0;
			int user_lists = 0;
			int user_friends = 0;
			int user_follower = 0;
			double desc_score = 0.0;

			double lati = 0.0;
			double longi = 0.0;
			int geocode_type = 10;

			boolean foundUser = false;
			Statement st = c.createStatement();
			String query = "Select * from "+DBManager.getUserTable()+" where user_screenname = '" + user_name + "';";
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				foundUser = true;
				creationdate = rs.getString("user_creationdate");
				user_language = rs.getString("user_language");
				desc_score = rs.getDouble("desc_score");
				gender = rs.getString("gender");
				user_statuses = rs.getInt("user_statusescount");
				user_lists = rs.getInt("user_listedcount");
				user_friends = rs.getInt("user_friendscount");
				user_follower = rs.getInt("user_followerscount");
				lati = rs.getDouble("latitude");
				longi = rs.getDouble("longitude");
				geocode_type = rs.getInt("geocoding_type");
			}
			// a sourceUser --> many details
			if (foundUser) {
				// ADD source node
				Object nodeID = null;
				if (!nodeNames.containsKey(user_name)) {
					nodeID = new MyUser(id, user_name);
					((MyUser)nodeID).addLanguage(user_language);
					((MyUser)nodeID).addCredibility(desc_score);
					((MyUser)nodeID).addGender(gender);
					
					if (lati != 0.0 || longi != 0.0) {
						((MyUser)nodeID).addPoint(lati, longi);
					}
					
					((MyUser)nodeID).setNameVisible();

					nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
							"ROUNDED;strokeColor=white;fillColor=white");
					nodeNames.put(user_name, nodeID);
					sources.put(user_name, nodeID);

				} else {
					nodeID = nodeNames.get(user_name);
				}
			} else {
//				NO source node -- add just the screenname
				Object nodeID = null;
				if (!nodeNames.containsKey(user_name)) {
					nodeID = new MyUser(id, user_name);
					((MyUser)nodeID).setNameVisible();

					nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
							"ROUNDED;strokeColor=white;fillColor=white");
					nodeNames.put(user_name, nodeID);
					sources.put(user_name, nodeID);

				} else {
					nodeID = nodeNames.get(user_name);
				}
			}
			
			st.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	
	
	private static MyUser getFullUser(String user_name, Connection c) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void createSimpleGraph(ScoreDoc[] result, IndexSearcher searcher, boolean withMention, boolean withFollows) {
		// CLEAR UP
		Object[] remove = graph.getChildVertices(parent);
		graph.removeCells(remove, true);

		HashMap<String, Object> nodeNames = new HashMap<>(); // screenName -> id
		HashMap<String, Object> sources = new HashMap<>();
		HashMap<String, Integer> edgesMap = new HashMap<>();
		int nodesCounter = 0;
		Lucene l = Lucene.INSTANCE;

		try {
			Connection c = DBManager.getConnection();
			String table = DBManager.getTweetdataTable();
//			String userTable = DBManager.getUserTable();
			Statement stmt = c.createStatement();
			
			for (ScoreDoc doc : result) {
				int docID = doc.doc;
				Document document;
				document = searcher.doc(docID);

				String type = (document.getField("type")).stringValue();
				String id = (document.getField("id")).stringValue();
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
				String screenName = ""; 	// (document.getField("name")).stringValue();
				String content = "";
				// double sentiment = 0;
				String posStrength = ""; // (document.getField("pos")).stringValue();
				String negStrength = ""; // (document.getField("neg")).stringValue();
				int pos = 0; 			// (posStrength.isEmpty()) ? 0 : Integer.parseInt((document.getField("pos")).stringValue());
				int neg = 0; 			// (negStrength.isEmpty()) ? 0 : Integer.parseInt((document.getField("neg")).stringValue());
				String sentiment = "neu"; // (document.getField("sentiment")).stringValue();
//				sentiment = (sentiment.isEmpty()) ? "neu" : sentiment;
				String category = "other"; // (document.getField("category")).stringValue();
//				category = (category.isEmpty()) ? "other" : category;

//				boolean hasUrl = Boolean.getBoolean((document.getField("hasURL")).stringValue());
				boolean hasUrl = false;
				// text language should be english
				// String language = (document.getField("language")).stringValue();
				// language = (language.isEmpty()) ? "en" : language;
				
				
				String query = "";
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
				
				String geom = "";
				boolean isEmpty = true;
				while (rs.next()) {
					isEmpty = false;
//					screenName = rs.getString("user_screenname");
//					content = rs.getString("tweet_content");
//					sentiment = rs.getString("sentiment");
//					category = rs.getString("category");
//					category = category.replace(" & ", "_").toLowerCase();
//					hasUrl = rs.getBoolean("hasurl");
//					pos = rs.getInt("positive");
//					neg = rs.getInt("negative");
					
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
				
				// GET Colors
				String colorString = "";
				if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
					Color color = Histogram.getCategoryColor(category);
					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
				} else {
					Color color = new Color(Display.getDefault(), 255, 255, 191);
					colorString = "#fee08b";
					if (sentiment.equals("pos")) {
						color = color = new Color(Display.getDefault(), 26, 152, 80);
						// colorString = "green";
						colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
					} else if (sentiment.equals("neg")) {
						color = color = new Color(Display.getDefault(), 215, 48, 39);
						// colorString = "red";
						colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
					}
				}

				String mentionString = (content != null) ? getMentionsFromTweets(content) : "";
				// continue, no target, no mentions
				if (mentionString.length() < 2) {
					continue;
				}

				// ADD source node
				Object nodeID = null;
				if (!nodeNames.containsKey(screenName)) {
					nodeID = new MyUser("n" + nodesCounter++, screenName);
					// ...

					nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
							"ROUNDED;strokeColor=white;fillColor=white");
					nodeNames.put(screenName, nodeID);
					sources.put(screenName, nodeID);

				} else {
					nodeID = nodeNames.get(screenName);
				}
				Object sourceID = nodeID;

				// ADD target nodes
				String[] mentions = mentionString.split(" ");
				if (withMention) {
					for (String target : mentions) {

						if (target.isEmpty())
							continue;

						target = target.replace(":", "");

						if (!nodeNames.containsKey(target)) {
							nodeID = new MyUser("n" + nodesCounter++, screenName);

							nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
									"ROUNDED;strokeColor=white;fillColor=white");
							nodeNames.put(target, nodeID);

						} else {
							nodeID = nodeNames.get(target);
						}

						Object edge = null;
						// ADD Edge: source to Target
						String edgesNames = "" + ((mxCell) sourceID).getId() + "_" + ((mxCell) nodeID).getId();
						if (edgesMap.containsKey(edgesNames)) {
							edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
						} else {
							edgesMap.put(edgesNames, new Integer(1));
						}

						edge = new MyEdge(id);
						((MyEdge) edge).addCategory(category);
						((MyEdge) edge).addSentiment(sentiment);
						((MyEdge) edge).addPos(pos);
						((MyEdge) edge).addNeg(neg);
						((MyEdge) edge).changeToString(MyEdge.LabelType.SentiStrenth);

						if (hasGeo)
							((MyEdge) edge).addPoint(lat, lon);

						graph.insertEdge(parent, null, edge, sourceID, nodeID,
								"edgeStyle=elbowEdgeStyle;elbow=horizontal;" + "strokeWidth=3;"
										+ "STYLE_PERIMETER_SPACING;" + "strokeColor=" + colorString
						// +
						// "exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1;"
						);
					}
				}
			}
			
			stmt.close();
			c.close();
			if (withFollows) {
				addAllFollows(nodeNames, edgesMap);
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}

		
		mxAnalysisGraph anaGraph = new mxAnalysisGraph();
		anaGraph.setGraph(graph);

		mxGraphStructure struc = new mxGraphStructure();
		Object[][] cc = struc.getGraphComponents(anaGraph);

		ArrayList<Object[]> filtered = new ArrayList<>();
		if (cc != null) {
			for (Object[] o : cc) {
				if (o != null && o.length >= 3) {
					filtered.add(o);
				}
				 else {
				 graph.removeCells(o, true);
				 }
			}

			// DESC
			filtered.sort(new Comparator<Object[]>() {

				@Override
				public int compare(Object[] o1, Object[] o2) {
					return Integer.compare(o2.length, o1.length);
				}
			});

			// Object[] most = filtered.get(0);
			// filterOutComponents(cc, 2);
		}
		morphGraph(graph, graphComponent);

	}
	
	
	
	
	public static void createGraph(ScoreDoc[] result, IndexSearcher searcher, boolean withMentions, boolean withFollows) {
		
		Object[] remove = graph.getChildVertices(parent);
		graph.removeCells(remove, true);
		
		HashMap<String, Object> nodeNames = new HashMap<>(); // screenName -> id
		HashMap<String, Object> sources = new HashMap<>();

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
				long hashgeo = (document.getField("geo")).numericValue().longValue();
				double lat = GeoPointField.decodeLatitude(hashgeo);
				double lon = GeoPointField.decodeLongitude(hashgeo);
				
				boolean hasGeo = false;
				if (lat != 0.0 || lon != 0.0) {
					hasGeo = true;
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
//					query = "Select "
//							+ "t.\"tweetScreenName\", t.\"tweetContent\", t.sentiment, t.category, t.\"containsUrl\", t.replytousername, "
//							+ "t.userlistedcount, t.\"userCreationdate\", t.\"userFriendscount\" , "
//							+ "t.\"userFollowers\", t.\"userStatusCount\", t.language, st_astext(t.polygeo) as geom  from "+table+" as t where t.tweetid = "
//							+ Long.parseLong(id);
					
					query = "Select "
							+ "t.user_screenname, t.tweet_content, t.sentiment, t.category, t.hasurl, t.relationship, "
							+ "t.user_creationdate, t.friends , "
							+ "t.followers, t.status_count, t.target from "+table+" as t where t.tweet_id = "
							+ Long.parseLong(id);
					
					break;
				case "flickr":
					query = "Select t.sentiment from flickrdata as t where t.\"photoID\" = " + Long.parseLong(id);
					break;
				default:
//					query = "Select t.sentiment from tweetdata as t where t.tweetid = " + Long.parseLong(id);
					query = "Select "
							+ "t.user_screenname, t.tweet_content, t.sentiment, t.category, t.hasurl, t.relationship, "
							+ "t.user_creationdate, t.friends , "
							+ "t.followers, t.status_count, t.target from "+table+" as t where t.tweet_id = "
							+ Long.parseLong(id);
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
					
					isEmpty = false;
					screenName = rs.getString(1);
					content = rs.getString(2);
					sentiment = rs.getString(3);
					category = rs.getString(4);
					category = category.replace(" & ", "_").toLowerCase();
					hasUrl = rs.getBoolean(5);
					relationship = rs.getString(6);
					// DATE
					String cd = rs.getString(7).split(" ")[0];
					Date dt = new Date(Integer.parseInt(cd.split("-")[0]), (Integer.parseInt(cd.split("-")[1])) -1 , Integer.parseInt(cd.split("-")[2]));
					time = dt.getTime(); 	// the bigger the 'older' the user
					friends = rs.getInt(8);
					followers = rs.getInt(9);
					tweetCount = rs.getInt(10);
					nodexl_target = rs.getString("target");
					
				}
				
				// get Colors
				String colorString = "";
				if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
					Color color = Histogram.getCategoryColor(category);
					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
				}
				else {
					Color color = new Color(Display.getDefault(), 255,255,191);
					colorString = "#fee08b";
//					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
					
					if (sentiment.equals("pos")) {
						color = color = new Color(Display.getDefault(), 26,152,80);
//						colorString = "green";
						colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
					}
					else if (sentiment.equals("neg")) {
						color = color = new Color(Display.getDefault(), 215,48,39);
//						colorString = "red";
						colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
					}
				}
				
				
				
				
				double sc_follow =  Math.log10(followers) / maxFollow;
				double sc_friend =  Math.log10(friends) / maxFriends;
				double sc_tweets =  Math.log10(tweetCount) / maxMessage;
				double sc_time = ((Math.log(time) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000))) / ((Math.log(maxDate) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)));
				
				double credible  = 1 - ((sc_follow + sc_friend +sc_tweets + sc_time) / 4.0);
				
				String mentionString = (content != null) ? getMentionsFromTweets(content) : "";
				// continue, no target, no mentions
				if (nodexl_target == null && mentionString.length() < 2) {
					continue;
				}
				
//				if (mentionString.length() < 2) {
//					continue;
//				}
				
				// ADD source node
				Object nodeID = null;
				if (!nodeNames.containsKey(screenName)) {
					nodeID = new MyUser("n" + nodesCounter++, screenName);
					((MyUser)nodeID).addLanguage(language);
					((MyUser)nodeID).addCredibility(credible);
					// ...
					nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40, "ROUNDED;strokeColor=white;fillColor=white");
					nodeNames.put(screenName, nodeID);
					sources.put(screenName, nodeID);
	// TODO			// create an Object with Properties
					
				} else {
					nodeID = nodeNames.get(screenName);
//					Element nodes = graph.element(nodeID);
//					Element node = nodes.element(""+nodeID);
//					Element data2 = node.addElement("data", getXMLNamesspace()); 
//					data2.addAttribute(QName.get("key", "", getXMLNamesspace()), "fol").addText(""+followers); 
					
				}
				Object sourceID = nodeID;
				
				String[] mentions = mentionString.split(" ");
				
				// ADD target nodes
				language = "";
				double descriptionScore = 0.5;
				String location = "";
				String timezone = "";
				int utcoffset = 0;
				
				if (nodexl_target != null) {
					if (nodeNames.containsKey(nodexl_target)) {
						nodeID = nodeNames.get(nodexl_target);
					}
					else {
						query = "Select "
								+ "t.user_name, t.pagerank, t.user_location, t.user_timezone, t.user_utcoffset, "
								+ " t.user_creationdate, t.friends, "
								+ "t.followers, t.status_count from "+userTable+" as t where t.user_name = '"
								+ nodexl_target+"'";
						
						rs = stmt.executeQuery(query);
						boolean userFound = false;
						while (rs.next()) {
							userFound = true;
							screenName = rs.getString(1);
							descriptionScore = rs.getDouble(2);
							location = rs.getString(3);
							timezone = rs.getString(4);
							utcoffset = rs.getInt(5);
							String cd = rs.getString(6);
							String TWITTER = "yyyy-dd-MM HH:mm:ss";
							SimpleDateFormat sf = new SimpleDateFormat(TWITTER,Locale.ENGLISH);
							sf.setLenient(true);
						    date = sf.parse(cd);
//							Date dt = new Date
//							String cd = rs.getString(8).split(" ")[0];
							Date dt = new Date(date.getYear(), date.getMonth(), date.getDate());
							time = dt.getTime(); 	// the bigger the 'older' the user
							friends = rs.getInt(7);
							followers = rs.getInt(8);
							tweetCount = rs.getInt(9);
						}
						// ID did not exist
						double targetCred = -1.0;
						if (userFound) {
							sc_follow =  Math.log10(followers) / maxFollow;
							sc_friend =  Math.log10(friends) / maxFriends;
							sc_tweets =  Math.log10(tweetCount) / maxMessage;
							sc_time = ((Math.log(time) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000))) / ((Math.log(maxDate) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)));
							
							if (sc_time < 0)
								sc_time = 0;
							
							targetCred  = 1.0 - ((sc_follow + sc_friend + sc_tweets + sc_time + descriptionScore) / 5.0) ;
						}
						nodeID = new MyUser("n"+nodesCounter++, screenName);
						((MyUser)nodeID).addLanguage(language);
						((MyUser)nodeID).addCredibility(targetCred);
						nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40, "ROUNDED;strokeColor=white;fillColor=white");
						nodeNames.put(nodexl_target, nodeID);
					}

					Object edge = null;
					// ADD Edge: source to Target

					// edgesNames sourceID_targetID --> count
					String edgesNames = "" + ((mxCell)sourceID).getId() + "_" + ((mxCell)nodeID).getId();
					if (edgesMap.containsKey(edgesNames)) {
						edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
					} else {
						edgesMap.put(edgesNames, new Integer(1));
					}
					
					if (!relationship.equals("Mentions")) {

						double edgeCredebility = createZipfScore(content) + ((hasUrl) ? 0.4 : 0);
						// if (sources.containsKey(nodeID)) {
						// System.out.println(edgesNames);
						// }

						edge = new MyEdge(id);
						((MyEdge) edge).addCredibility(edgeCredebility);
						category = category.replace(" & ", "_").toLowerCase();
						((MyEdge) edge).addCategory(category);
						((MyEdge) edge).addSentiment(sentiment);
						((MyEdge) edge).addDate(date);
						((MyEdge)edge).addContent(content);
						((MyEdge)edge).addPos(pos);
						((MyEdge)edge).addNeg(neg);
						((MyEdge)edge).changeToString(MyEdge.LabelType.SentiStrenth);
						
						
						if (hasGeo)
							((MyEdge) edge).addPoint(lat, lon);
//						if (!geom.isEmpty()) {
//							geom = geom.toLowerCase().replace("point(", "").replace(")", "");
//							double lati = Double.parseDouble(geom.split(" ")[1]);
//							double longi = Double.parseDouble(geom.split(" ")[0]);
//							((MyEdge) edge).addPoint(lati, longi);
//						}
						// ...

						// TODO // create an Edge Object for Properties
						graph.insertEdge(parent, null, edge, sourceID, nodeID,
								"edgeStyle=elbowEdgeStyle;elbow=horizontal;"
								+ "strokeWidth=3;" 
								+ "STYLE_PERIMETER_SPACING;"+"strokeColor="+colorString
						// +
						// "exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1;"
						);
					}

				}
				
				if (withMentions) {
					for (String target : mentions) {

						if (target.isEmpty())
							continue;

						target = target.replace(":", "");

						switch (type) {
						// get the tweet
						case "twitter":
							// query = "Select "
							// + "t.user_screenname, t.cscore, t.user_location,
							// t.user_timezone, t.user_language,
							// t.user_utcoffset, "
							// + "t.user_listedcount, t.user_creationdate,
							// t.user_friendscount , "
							// + "t.user_followerscount, t.user_statusescount
							// from "+userTable+" as t where t.user_screenname =
							// '"
							// + target+"'";

							query = "Select "
									+ "t.user_name, t.pagerank, t.user_location, t.user_timezone, t.user_utcoffset, "
									+ " t.user_creationdate, t.friends, " + "t.followers, t.status_count  from "
									+ userTable + " as t where t.user_name = '" + target + "'";
							break;
						case "flickr":
							// query = "Select t.sentiment from flickrdata as t
							// where t.\"photoID\" = " + Long.parseLong(id);
							break;
						default:
							// query = "Select t.sentiment from tweetdata as t
							// where t.tweetid = " + Long.parseLong(id);
							query = "Select "
									+ "t.user_name, t.pagerank, t.user_location, t.user_timezone, t.user_utcoffset, "
									+ " t.user_creationdate, t.friends, " + "t.followers, t.status_count  from "
									+ userTable + " as t where t.user_name = '" + target + "'";
						}
						try {
							rs = stmt.executeQuery(query);
						} catch (Exception e ) {
							continue;
						}

						boolean userFound = false;
						while (rs.next()) {
							// userFound = true;
							// screenName = rs.getString(1);
							// descriptionScore = rs.getDouble(2);
							// location = rs.getString(3);
							// timezone = rs.getString(4);
							// language = rs.getString(5);
							// utcoffset = rs.getInt(6);
							// listedCount = rs.getInt(7);
							// // Creation DATE
							// String cd = rs.getString(8);
							//// DateTimeFormatter format =
							// DateTimeFormatter.ofPattern("E LL dd HH:mm:ss zzz
							// yyyy");
							// String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ
							// yyyy";
							//// DateTimeFormatter TWITTER =
							// DateTimeFormatter.ofPattern("yyyy-MM-dd
							// HH:mm:ss.S");
							// SimpleDateFormat sf = new
							// SimpleDateFormat(TWITTER,Locale.ENGLISH);
							// sf.setLenient(true);
							// java.util.Date date =sf.parse(cd);
							//// Date dt = new Date
							//// String cd = rs.getString(8).split(" ")[0];
							// Date dt = new Date(date.getYear(),
							// date.getMonth(), date.getDate());
							// time = dt.getTime(); // the bigger the 'older'
							// the user
							// friends = rs.getInt(9);
							// followers = rs.getInt(10);
							// tweetCount = rs.getInt(11);

							userFound = true;
							screenName = rs.getString(1);
							descriptionScore = rs.getDouble(2);
							location = rs.getString(3);
							timezone = rs.getString(4);
							utcoffset = rs.getInt(5);
							// Creation DATE
							String cd = rs.getString(6);
							// DateTimeFormatter format =
							// DateTimeFormatter.ofPattern("E LL dd HH:mm:ss zzz
							// yyyy");
							// String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ
							// yyyy";

							String TWITTER = "yyyy-dd-MM HH:mm:ss";

							// DateTimeFormatter TWITTER =
							// DateTimeFormatter.ofPattern("yyyy-MM-dd
							// HH:mm:ss.S");
							SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
							sf.setLenient(true);
							date = sf.parse(cd);
							// Date dt = new Date
							Date dt = new Date(date.getYear(), date.getMonth(), date.getDate());
							time = dt.getTime(); // the bigger the 'older' the user
							friends = rs.getInt(7);
							followers = rs.getInt(8);
							tweetCount = rs.getInt(9);
						}

						// ID did not exist
						double targetCred = -1.0;
						if (userFound) {
							sc_follow = Math.log10(followers) / maxFollow;
							sc_friend = Math.log10(friends) / maxFriends;
							sc_tweets = Math.log10(tweetCount) / maxMessage;
							sc_time = ((Math.log(time) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)))
									/ ((Math.log(maxDate) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)));

							if (sc_time < 0)
								sc_time = 0;

							targetCred = 1.0 - ((sc_follow + sc_friend + sc_tweets + sc_time + descriptionScore) / 5.0);
						}

						if (!nodeNames.containsKey(target)) {
							nodeID = new MyUser("n" + nodesCounter++, screenName);
							((MyUser) nodeID).addLanguage(language);
							((MyUser) nodeID).addCredibility(targetCred);
							nodeID = graph.insertVertex(parent, null, nodeID, 0, 0, 40, 40,
									"ROUNDED;strokeColor=white;fillColor=white");
							nodeNames.put(target, nodeID);

						} else {
							nodeID = nodeNames.get(target);
						}

						Object edge = null;
						// ADD Edge: source to Target

						// edgesNames sourceID_targetID --> count
						String edgesNames = "" + ((mxCell) sourceID).getId() + "_" + ((mxCell) nodeID).getId();
						if (edgesMap.containsKey(edgesNames)) {
							edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
						} else {
							edgesMap.put(edgesNames, new Integer(1));
						}

						double edgeCredebility = createZipfScore(content) + ((hasUrl) ? 0.4 : 0);

						if (sources.containsKey(nodeID)) {
							System.out.println(edgesNames);
						}

						edge = new MyEdge(id);
						((MyEdge) edge).addCredibility(edgeCredebility);
						((MyEdge) edge).addCategory(category);
						((MyEdge) edge).addSentiment(sentiment);
						((MyEdge)edge).addContent(content);
						((MyEdge)edge).addPos(pos);
						((MyEdge)edge).addNeg(neg);
						((MyEdge)edge).changeToString(MyEdge.LabelType.SentiStrenth);
						
						if (hasGeo)
							((MyEdge) edge).addPoint(lat, lon);
						
//						if (!geom.isEmpty()) {
//							geom = geom.toLowerCase().replace("point(", "").replace(")", "");
//							double lati = Double.parseDouble(geom.split(" ")[1]);
//							double longi = Double.parseDouble(geom.split(" ")[0]);
//							((MyEdge) edge).addPoint(lati, longi);
//						}
						// ...
						
						
						graph.insertEdge(parent, null, edge, sourceID, nodeID,
								"edgeStyle=elbowEdgeStyle;elbow=horizontal;" 
										+ "strokeWidth=3;" 
										+ "STYLE_PERIMETER_SPACING;"+"strokeColor="+colorString
						// +
						// "exitX=0.5;exitY=1;exitPerimeter=1;entryX=0;entryY=0;entryPerimeter=1;"
						);
					}

				}
			}
			
			stmt.close();
			c.close();
			// ADD follows
			if (withFollows)
				addAllFollows(nodeNames, edgesMap);
			
			
		} catch (IOException | SQLException | java.text.ParseException e) {
			e.printStackTrace();
		}
			
		
		
		mxAnalysisGraph anaGraph = new mxAnalysisGraph();
		anaGraph.setGraph(graph);
		
		mxGraphStructure struc = new mxGraphStructure();
		Object[][] cc = struc.getGraphComponents(anaGraph);
		
		
		ArrayList<Object[]> filtered = new ArrayList<>();
		if (cc != null) {
			for (Object[] o : cc) {
				if (o != null && o.length >= 4) {
					filtered.add(o);
				}
			}
			
			// DESC
			filtered.sort(new Comparator<Object[]>() {
				
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return Integer.compare( o2.length, o1.length);
				}
			});
			
//			Object[] most = filtered.get(0);
//			filterOutComponents(cc, 2);
		}
		
		morphGraph(graph, graphComponent);
		
	}
	
	
	private static void addAllFollows(HashMap<String, Object> nodeNames, HashMap<String, Integer> edgesMap) {

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
						Object edge = null;
						// ADD Edge: source to Target

						String edgesNames = "" + ((mxCell) nodeNames.get(name)).getId() + "_"
								+ ((mxCell) nodeNames.get(target)).getId();
						if (edgesMap.containsKey(edgesNames)) {
							edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
						} else {
							edgesMap.put(edgesNames, new Integer(1));
						}

						edge = new MyEdge(id);
						((MyEdge) edge).addCredibility(0);
						((MyEdge) edge).addCategory("");
						((MyEdge) edge).addSentiment("neu");
						((MyEdge) edge).addDate(null);
						((MyEdge) edge).setRelationsip("Followed");

						graph.insertEdge(parent, null, edge, nodeNames.get(name), nodeNames.get(target), "FollowEdge"

						);
					}
				}
			}
			stmt.close();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private static void filterOutComponents(Object[][] cc, int minNodes) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try { 
					graph.getModel().beginUpdate();
					System.out.println(graph.getChildVertices(parent).length);
					for (Object[] o : cc) {
						if (o != null && o.length < minNodes) {
	//				graph.removeCells(o, true);
							graph.removeCells(o, false);
						}
				}
				graph.orderCells(true);
				} finally {
					graph.getModel().endUpdate();
				}
				System.out.println("Filtered to >> "+graph.getChildVertices(parent).length);
				
			}
		});
	}


	private static void morphGraph(mxGraph graph, mxGraphComponent graphComponent) {
		// define layout
		mxIGraphLayout layout = new mxFastOrganicLayout(graph);
		
		graph.setAutoOrigin(true);
		graph.setAutoSizeCells(true);
		graph.setBorder(65);
//		graph.setOrigin(new mxPoint(200, 80));
		// layout using morphing
		graph.getModel().beginUpdate();
		try {
//			layout.moveCell(graph.getDefaultParent(), 50, 50);
			layout.execute(graph.getDefaultParent());
		} finally {
			
			mxMorphing morph = new mxMorphing(graphComponent, 20, 1.5, 10);
			morph.addListener(mxEvent.DONE, new mxIEventListener() {

				@Override
				public void invoke(Object arg0, mxEventObject arg1) {
					graph.getModel().endUpdate();
					//fitViewport();
				}

			});

			morph.startAnimation();
		}

	}
	
	
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

	 
	public static void changeEdgeColor() {
		
		Object[] edges = graph.getChildEdges(parent);
		
		graph.getModel().beginUpdate();
		for (Object edge : edges) {
			MyEdge ed = ((MyEdge)((mxCell)edge).getValue());
			String category = ed.getCategory();
			String sentiment = ed.getSentiment();
			String relationship = ed.getRelationsip();
			
			
		 	Lucene l = Lucene.INSTANCE;
			// get Colors
			String colorString = "";
			
			if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
				Color color = Histogram.getCategoryColor(category);
				colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
			} else {
				Color color = new Color(Display.getDefault(), 255, 255, 191);
				colorString = "#fee08b";
				// colorString = String.format("#%02x%02x%02x", color.getRed(),
				// color.getGreen(), color.getBlue());

				if (sentiment.equals("pos")) {
					color = color = new Color(Display.getDefault(), 26, 152, 80);
					// colorString = "green";
					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
				} else if (sentiment.equals("neg")) {
					color = color = new Color(Display.getDefault(), 215, 48, 39);
					// colorString = "red";
					colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
				}
			}
			
			if (relationship.equals("Followed"))
				((mxCell)edge).setStyle("FollowEdge");
			else
				((mxCell)edge).setStyle("edgeStyle=elbowEdgeStyle;elbow=horizontal;" + "strokeWidth=3;" 
						+ "STYLE_PERIMETER_SPACING;"+"strokeColor="+colorString);
			
			// update node
//			((mxCell)edge).getSource().setStyle(((mxCell)edge).getSource().getStyle());
//			((mxCell)edge).getTarget().setStyle(((mxCell)edge).getTarget().getStyle());
		}
		
		graph.getModel().endUpdate();
		graph.refresh();
		
		
		
	}


}

