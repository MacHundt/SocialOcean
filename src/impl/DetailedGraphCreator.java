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
import java.text.DecimalFormat;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
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
import com.mxgraph.swing.handler.mxPanningHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import socialocean.parts.Histogram;
import utils.DBManager;
import utils.Lucene;
import utils.Lucene.TimeBin;
import utils.TimeLineHelper;


public class DetailedGraphCreator {
	
	private static JPanel graphPanel = null;
	private static DecimalFormat df = new DecimalFormat("#.00");
	
	private static mxGraph graph = null;
	private static Object parent = null;
	private static MyGraphComponent graphComponent = null;
	private static mxStylesheet stylesheet;
	private static Hashtable<String, Object> followStyle;
	
	private static int topK = 5;
	static boolean ASC = true;
	static boolean DESC = false;
	
	private static ArrayList<MyEdge> geo_edges = new ArrayList<>();
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
			
//			mxPanningHandler panning = new mxPanningHandler(graphComponent);
//			panning.setEnabled(true);
			// Selection Handler
			mxRubberband band = new mxRubberband(graphComponent);
			
			
//			new mxSelectionCellsHandler(graphComponent);
			
			graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {

				@SuppressWarnings("null")
				@Override
				public void invoke(Object sender, mxEventObject evt) {
					int selected = graph.getSelectionCount();
					
					Lucene l = Lucene.INSTANCE;
					// check Icons
					if (!MapPanelCreator.iconsloaded)
						MapPanelCreator.loadTweetIcons();
					
					if (selected > 0) {
						// Max values: hard coded
						double maxFollow = Math.log10(8448672);				// loged
						double maxFriends = Math.log10(290739);
						double maxMessage = Math.log10(625638);
						long maxDate = l.getUser_maxDate();
						long minDate = l.getUser_minDate();

						String details = "";
						String usercontent ="";
						String average ="";
						ImageIcon icon = null;
						SELECTED = true;
						geo_edges = new ArrayList<>();		// for the map visualization
						ArrayList<MyEdge> detailsEdges = new ArrayList<>();
						ArrayList<MyUser> detailUsers = new ArrayList<>();
						Connection c = DBManager.getConnection();
						try {
							Statement stmt = c.createStatement();
							// SINGLE Selection
							if (selected == 1) {
								Object cell = graph.getSelectionCells()[0];
								if (cell instanceof mxCell) {
									if (((mxCell) cell).getValue() instanceof MyEdge) {
										MyEdge edge = (MyEdge) ((mxCell) cell).getValue();
										GeneralGraphCreator.showSingleDetailofEdge(graphPanel, edge);
									} else if (((mxCell) cell).getValue() instanceof MyUser) {
										MyUser user = (MyUser) ((mxCell) cell).getValue();
										GeneralGraphCreator.showSingleDetailofUser(graphPanel, user);
									}
								}
							} 			
							// MULTI Selection
							else if (selected > 1) {
								
								for (int i = 0; i < selected; i++) {
									Object cell = graph.getSelectionCells()[i];
									
									if (cell instanceof mxCell) {
										if (((mxCell) cell).getValue() instanceof MyEdge) {
											MyEdge edge = (MyEdge) ((mxCell) cell).getValue();

											double lat = edge.getLatitude();
											double lon = edge.getLongitude();

											if (lat != 0.0 || lon != 0.0) {
												geo_edges.add(edge);
											}
											detailsEdges.add(edge);

											int breakLineAT = 80;
											String table = DBManager.getTweetdataTable();

											String query = "select t.user_screenname, t.relationship, t.tweet_content, t.tweet_creationdate, t.sentiment, "
													+ "t.category,"
													+ (table.startsWith("nodexl") ? "" : "t.tweet_source, ")
													+ " cscore, t.hasurl, t.urls from " + table
													+ " as t where t.tweet_id = " + edge.getId();
											ResultSet rs = stmt.executeQuery(query);
											while (rs.next()) {

												String scName = rs.getString("user_screenname");
												String relationship = rs.getString("relationship");
												String content = rs.getString("tweet_content");
												String hashtag = "";

												Pattern p = Pattern.compile("#\\S+");
												List<String> hashTags = new ArrayList<>();
												Matcher matcher = p.matcher(content);
//												while (matcher.find()) {
//													System.out.println(hashTags.add(matcher.group(0)));
//												}

												double cscore = rs.getDouble("cscore");
												double betweenness = edge.getBetweennessScore();

												String date = rs.getString("tweet_creationdate");
												String cd = date.split(" ")[0];
												Date dt = new Date(Integer.parseInt(cd.split("-")[0]),
														(Integer.parseInt(cd.split("-")[1])) - 1,
														Integer.parseInt(cd.split("-")[2]));
												String sentiment = rs.getString("sentiment");
												sentiment += " (" + edge.getPos() + "," + edge.getNeg() + ")";
												String category = rs.getString("category");
												boolean hasUrl = rs.getBoolean("hasurl");
												String urls = rs.getString("urls");
												String device = (table.startsWith("nodexl") ? ""
														: rs.getString("tweet_source"));

												if (relationship.equals("Followed")) {
													details += "\n" + scName + " follows ";
													break;
												}
												device = device.substring(device.lastIndexOf("/") + 1);

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
											}

											icon = getTweetIcon(edge);

										} else if (((mxCell) cell).getValue() instanceof MyUser) {
											MyUser user = (MyUser) ((mxCell) cell).getValue();
											detailUsers.add(user);
											details = "";
											icon = getUserIcon(user);
											int breakLineAT = 80;
											String table = DBManager.getUserTable();

											String query = "select u.user_creationdate, u.user_language, u.user_statusescount, u.user_followerscount, "
													+ "u.user_friendscount, u.user_listedcount, u.desc_score, u.user_location, "
													+ "geocoding_type from " + table
													+ " as u where u.user_screenname = '" + user.getName() + "'";
											ResultSet rs = stmt.executeQuery(query);
											while (rs.next()) {
												String scName = user.getName();
												String gender = user.getGender();
												String creationdate = rs.getString("user_creationdate");
												// // DATE
												String cd = creationdate.split(" ")[0];
												Date dt = new Date(Integer.parseInt(cd.split("-")[0]),
														(Integer.parseInt(cd.split("-")[1])) - 1,
														Integer.parseInt(cd.split("-")[2]));
												long time = dt.getTime(); // the bigger the 'older' the user
												String user_language = rs.getString("user_language");
												String user_location = rs.getString("user_location");

												int statuses = rs.getInt("user_statusescount");
												int followers = rs.getInt("user_followerscount");
												int friends = rs.getInt("user_friendscount");
												int listed = rs.getInt("user_listedcount");

												double desc_score = rs.getDouble("desc_score");
												int geocoding_type = rs.getInt("geocoding_type");

												double sc_follow = Math.log10(followers) / maxFollow;
												double sc_friend = Math.log10(friends) / maxFriends;
												double sc_tweets = Math.log10(statuses) / maxMessage;
												double sc_time = ((Math.log(time) / Math.log(1000))
														- (Math.log(minDate) / Math.log(1000)))
														/ ((Math.log(maxDate) / Math.log(1000))
																- (Math.log(minDate) / Math.log(1000)));

												double credible = 1
														- ((sc_follow + sc_friend + sc_tweets + sc_time + desc_score)
																/ 5.0);

												// ratio = 1: I follow nobody (no friends), but many follow me
												// ratio -> 0: I follow many (many friends), but nobody follows me
												double follow_friend_ratio = followers / (double) (followers + friends);

												user.addGender(gender);
												user.addStatuses(statuses);
												user.addFollowers(followers);
												user.addFriends(friends);
												user.addListed(listed);

												user.addDescScore(desc_score);
												user.addGeocodingType(geocoding_type);
												user.addCreationDate(creationdate);

												details += "\nUSER: " + scName + ", (" + gender + "):";
												details += "\nCreated on: \t" + creationdate;
												// Get "Age" of user
												details += "\nAge: \t" + " .. calcuate from maxDate";
												details += "\nUser Language: \t" + user_language;
												details += "\nUser Location: \t" + user_location;
												// details += "\nCredibility: \t" + df.format(credible);

												// get more cred-values
												details += "\nFollow-Friend Ratio: \t" + df.format(follow_friend_ratio);
												details += "\nDescription Score: \t" + df.format(desc_score);

												details += "\nStatistics (Statuses, Followers, Friends, Listed):  \t ("
														+ statuses + ", " + followers + ", " + friends + ", " + listed
														+ ")";

												average += "\nStatistics (Statuses, Followers, Friends, Listed):  \t ("
														+ statuses + ", " + followers + ", " + friends + ", " + listed
														+ ")";
												;

											}
										}
									}
								}

								// ####### Create a Detail View Of A Group ########
								GroupDetailsWindow groupDetails = new GroupDetailsWindow(detailsEdges, detailUsers);
								CreateGroupDetailsThread changeDataset = new CreateGroupDetailsThread(l) {

									@Override
									public void execute() {
										groupDetails.changeHashtagDataset();
									}
								};
								changeDataset.start();

							}
							
							stmt.close();
							c.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
					// NO Selection -->  show all again
					else {
						SELECTED = false;
						l.clearMap();
						TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
							@Override
							public void execute() {
								ArrayList<TimeLineHelper> tl_helper = l.createTimeBins(TimeBin.HOURS, allEdges);
								l.showInTimeLine(tl_helper);
							}
						};
						lilt.start();

						l.showClustersInMap(allUser, allEdges);
						l.showSelectionInHistogramm(allEdges);
						l.initCountriesMap();
					}
				}
			});
			
			
			graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
				
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == 3) {
//						panning.setEnabled(false);
						band.mousePressed(e);
						band.start(e.getPoint());
					}
					else if (e.getButton() == 1) {
//						panning.setEnabled(true);
						band.reset();
					}
				}
			});
			
			
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
	
	public static ImageIcon getTweetIcon(MyEdge edge) {
		ImageIcon icon = null;
		
		Lucene l = Lucene.INSTANCE;
		String type = edge.getSentiment();
		if (l.getColorScheme().name().toLowerCase().equals("category"))
			type = edge.getCategory();
		// Check the sentiment for the right icon
		// if (sentiment.equals("positive"))
		// icon = tweetIcon_p;
		// else if (sentiment.equals("negative"))
		// icon = tweetIcon_n;
		// else
		// icon = tweetIcon_;

		switch (type.toLowerCase()) {
		case "pos":
			icon = MapPanelCreator.tweetIcon_p;
			break;
		case "neg":
			icon = MapPanelCreator.tweetIcon_n;
			break;
		case "neu":
			icon = MapPanelCreator.tweetIcon_;
			break;

		case "family":
			icon = MapPanelCreator.tweetIcon_family;
			break;
		case "food":
			icon = MapPanelCreator.tweetIcon_food;
			break;
		case "computers_technology":
			icon = MapPanelCreator.tweetIcon_computer;
			break;
		case "other":
			icon = MapPanelCreator.tweetIcon_other;
			break;
		case "recreation_sports":
			icon = MapPanelCreator.tweetIcon_rec_sport;
			break;
		case "politics":
			icon = MapPanelCreator.tweetIcon_politics;
			break;
		case "marketing":
			icon = MapPanelCreator.tweetIcon_marketing;
			break;
		case "education":
			icon = MapPanelCreator.tweetIcon_education;
			break;
		case "pets":
			icon = MapPanelCreator.tweetIcon_pets;
			break;
		case "health":
			icon = MapPanelCreator.tweetIcon_health;
			break;
		case "news_media":
			icon = MapPanelCreator.tweetIcon_news;
			break;
		case "music":
			icon = MapPanelCreator.tweetIcon_music;
			break;

		default:
			icon = MapPanelCreator.tweetIcon_;
			break;
		}
		
		return icon;
	}
	
	
	public static ImageIcon getUserIcon(MyUser user) {
		ImageIcon icon = null;
		String gender = user.getGender();

		switch (gender.toLowerCase()) {
		case "male":
			icon = MapPanelCreator.user_male;
			break;
		case "female":
			icon = MapPanelCreator.user_female;
			break;
		case "other":
			icon = MapPanelCreator.user;
			break;

		default:
			icon = MapPanelCreator.user;
			break;
		}
		
		return icon;
	};
	
	
	public static void clearGraph()	{
		
		if (graph != null) {
			graph.getModel().beginUpdate();
			allEdges.clear();
			allUser.clear();
			Object[] remove = graph.getChildVertices(parent);
			graph.removeCells(remove, true);
			graph.getModel().endUpdate();
		}
	}
	
	
	public static <V,E> void createDetailGraph(Collection<V> nodes, Collection<E> edges, boolean withMention,
			boolean withFollows, boolean clear) {
		
		if (nodes.isEmpty())
			return;
		
		Connection c = DBManager.getConnection(false, true);
		if (c == null)
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
		
		// Get User information, create nodes
		for (V n : nodes) {
			if (n instanceof MyUser) {
				MyUser user = (MyUser) n;
				String user_name = user.getName();
				String id = user.getId();
				createUserNode(user_name, id, c, nodeNames, sources);
			}
		}
		
		int uCounter = 0;
		for (E e : edges) {
			if (e instanceof MyEdge) {
				MyEdge edge = (MyEdge) e;
				long tweet_id = Long.parseLong(edge.getId());
				
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
					String cdate = "";
					
					Statement tst = c.createStatement();
					String query = "Select "
							+ "t.user_screenname, t.tweet_content, t.sentiment, t.positive, t.negative, t.category, t.hasurl, t.relationship, "
							+ "t.latitude, t.longitude, tweet_creationdate "
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
						cdate = rs.getString(11);
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
						if (mentionString.isEmpty()) {
							mentionString = source;
						}
//						// continue, no target, no mentions
//						if (mentionString.length() < 2) {
//							continue;
//						}
						
						boolean hasGeo = false;
						if (lati != 0.0 || longi != 0.0) {
							hasGeo = true;
						}
						
						// ADD target nodes
						String[] mentions = mentionString.split(" ");
						if (withMention) {
							for (String target : mentions) {

								if (target.isEmpty()) {
									continue;
								}

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
								((MyEdge) gedge).changeToString(MyEdge.LabelType.SentiStrength);
								((MyEdge) gedge).setUtimestamp( TimeLineHelper.creationDateToLong(cdate));

								if (hasGeo)
									((MyEdge) gedge).addPoint(lati, longi);

								graph.insertEdge(parent, null, gedge, sourceID, tnode,
										"edgeStyle=elbowEdgeStyle;elbow=horizontal;" + "strokeWidth=3;"
												+ "STYLE_PERIMETER_SPACING;" + "strokeColor=" + colorString
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

		morphGraph(graph, graphComponent);

		// USERS of EDGES?
		Object[] allnodes = graph.getChildVertices(parent);
		
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
		
		TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
			@Override
			public void execute() {
				ArrayList<TimeLineHelper> tl_helper =  l.createTimeBins(TimeBin.HOURS, allEdges);
				l.showInTimeLine(tl_helper);
			}
		};
		lilt.start();
		
		l.showSelectionInHistogramm(allEdges);
		l.initCountriesMap();
		
		l.showSelectionInMap(allEdges, true);
		l.showClustersInMap(allUser, allEdges);
		
		MapPanelCreator.getMapPanel().validate();
		MapPanelCreator.getMapPanel().repaint();
		
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
			
			String s_strength = (ed.getPos() + ed.getNeg())+"";
			
		 	Lucene l = Lucene.INSTANCE;
			// get Colors
			String colorString = "";
			
			if (l.getColorScheme().equals(Lucene.ColorScheme.CATEGORY)) {
				Color color = Histogram.getCategoryColor(category);
				colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
			}
			else if (l.getColorScheme().equals(Lucene.ColorScheme.SENTISTRENGTH)) {
				Color color = Histogram.getSentiStrengthColor(s_strength);
				colorString = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
			}
			else {
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

