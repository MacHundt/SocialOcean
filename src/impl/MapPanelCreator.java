package impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.AbstractTileFactory;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import com.vividsolutions.jts.geom.Coordinate;

import socialocean.controller.MapController;
import socialocean.model.MapGridRectangle;
import socialocean.model.Result;
import socialocean.painter.CountryPainter;
import socialocean.painter.GlyphPainter;
import socialocean.painter.GridPainter;
import socialocean.parts.MapMenuPanel;
import utils.FilesUtil;
import utils.Lucene;
import utils.Lucene.TimeBin;

public class MapPanelCreator {

	private static MapViewer mapPanel = null;
	private static JXMapViewer mapViewer = null;
	public static int maxZoom = 19;
	public static int zoom = 16;
	
	private static String Thunder_API = "8ab08504fd7e4a3bb4953f1080448e26";
	
	private static int tileThreads = 4;

	public static MapController mapCon;
	private static MapMenuPanel menu;

	private static Set<SwingWaypoint> waypoints = new HashSet<>();
	private static WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();

	public static ImageIcon tweetIcon_p;
	public static ImageIcon tweetIcon_n;
	public static ImageIcon tweetIcon_;

	public static ImageIcon tweetIcon_health;
	public static ImageIcon tweetIcon_pets;
	public static ImageIcon tweetIcon_music;
	public static ImageIcon tweetIcon_family;
	public static ImageIcon tweetIcon_politics;
	public static ImageIcon tweetIcon_marketing;
	public static ImageIcon tweetIcon_education;
	public static ImageIcon tweetIcon_rec_sport;
	public static ImageIcon tweetIcon_news;
	public static ImageIcon tweetIcon_computer;
	public static ImageIcon tweetIcon_food;
	public static ImageIcon tweetIcon_other;
	
	public static ImageIcon user_male;
	public static ImageIcon user;
	public static ImageIcon user_female;
	
	public static ImageIcon neg1;
	public static ImageIcon neg2;
	public static ImageIcon neg3;
	public static ImageIcon neg4;
	public static ImageIcon neg5;
	public static ImageIcon pos1;
	public static ImageIcon pos2;
	public static ImageIcon pos3;
	public static ImageIcon pos4;
	public static ImageIcon pos5;
	
	
	public static boolean iconsloaded = false;

	private static boolean loadedIcons = false;

	private static Point startPoint;
	private static Color grey = new Color(240, 240, 240, 50); // light grey, high opacity --> NORMAL
	
	private static int userTweetSwitch = 2;
	

	public static void loadTweetIcons() {
		// ## LOAD Icons
		// ImageDescriptor st =
		// AbstractUIPlugin.imageDescriptorFromPlugin("SocialOcean", "icons/tweet.png");
		// ImageDescriptor st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase",
		// "icons/neutral.png");
		// org.eclipse.swt.graphics.Image img = st.createImage();
		// BufferedImage image = Swing_SWT.convertToAWT(img.getImageData());

		BufferedImage image = FilesUtil.readIconFile("icons/neutral24y.png");
		tweetIcon_ = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/neg24_icon.png");
		tweetIcon_n = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/pos_24.png");
		tweetIcon_p = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/health24.png");
		tweetIcon_health = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/pets24.png");
		tweetIcon_pets = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/music24.png");
		tweetIcon_music = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/family24.png");
		tweetIcon_family = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/politics24.png");
		tweetIcon_politics = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/marketing24.png");
		tweetIcon_marketing = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/education24.png");
		tweetIcon_education = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/sports24.png");
		tweetIcon_rec_sport = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/news24.png");
		tweetIcon_news = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/computer24.png");
		tweetIcon_computer = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/food24.png");
		tweetIcon_food = new ImageIcon(image);

		image = FilesUtil.readIconFile("icons/cat_icons/other24.png");
		tweetIcon_other = new ImageIcon(image);
		
		// USER Icons
		image = FilesUtil.readIconFile("icons/user_female32.png");
		user_female = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/user_male32.png");
		user_male = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/user.png");
		user = new ImageIcon(image);
		
		// SENTI_STRENGTH Icons
		image = FilesUtil.readIconFile("icons/senti_strength_icons/neg1_24.png");
		neg1 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/neg2_24.png");
		neg2 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/neg3_24.png");
		neg3 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/neg4_24.png");
		neg4 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/neg5_24.png");
		neg5 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/pos1_24.png");
		pos1 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/pos2_24.png");
		pos2 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/pos3_24.png");
		pos3 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/pos4_24.png");
		pos4 = new ImageIcon(image);
		
		image = FilesUtil.readIconFile("icons/senti_strength_icons/pos5_24.png");
		pos5 = new ImageIcon(image);
		
		
		loadedIcons = true;

	}

	private static class MapViewer extends JPanel implements Observer {

		private List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		private JXMapViewer map;

		public MapViewer(JXMapViewer map) {
			super(new BorderLayout());
			this.map = map;
			this.add(map, BorderLayout.CENTER);
		}

		@Override
		public void update(Observable o, Object arg) {
			drawGrid();
		}
		
		
		

		public void drawGrid() {
			
			boolean both = false;
			if (Lucene.SHOWTweet && Lucene.SHOWUser) {
				both = true;
				userTweetSwitch--;
			}
			
			
			if (DetailedGraphCreator.SELECTED) {
				painters.removeIf(
					p -> p instanceof GlyphPainter || p instanceof GridPainter || p instanceof CountryPainter);
				showWayPointsOnMap();
				return;
			}
			// remove, with both - only once!
			else if (!both || (both && userTweetSwitch > 0))
				painters.removeIf(
						p -> p instanceof GlyphPainter || p instanceof GridPainter || p instanceof WaypointPainter
						|| p instanceof CountryPainter);

//			System.out.println("ZOOM >> " + map.getZoom());

			if (map.getZoom() < 14 && swingWaypointPainter != null) {
				swingWaypointPainter.setWaypoints(waypoints);

				for (SwingWaypoint p : waypoints) {

					Rectangle viewport = mapViewer.getViewportBounds();
					Point2D point = mapViewer.getTileFactory().geoToPixel(p.getPosition(), mapViewer.getZoom());

					int buttonX = (int) (point.getX() - viewport.getX());
					int buttonY = (int) (point.getY() - viewport.getY());

					if (buttonY <= 0 || buttonX <= 0 || buttonY > viewport.getHeight() || buttonX > viewport.getWidth())
						continue;

					mapViewer.add(p.getButton());

					// if (p instanceof TweetWayPoint)
					// mapViewer.add(((TweetWayPoint)p).getButton());
					
				}
			} else if (!both || (both && userTweetSwitch > 0)) {
				mapViewer.removeAll();
			}
			
			if (Lucene.INITCountries && Lucene.SHOWCountries){
				CountryPainter cp = new CountryPainter(mapCon);
				painters.add(cp);
			}
			if (Lucene.SHOWHeatmap) {
				GridPainter gp = new GridPainter(mapCon);
//				GlyphPainter glp = new GlyphPainter(mapCon);
				painters.add(gp);
//				painters.add(glp);
			} 
			
			painters.add(swingWaypointPainter);
			CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
			map.setOverlayPainter(painter);
			map.revalidate();
			map.repaint();
			
			if (userTweetSwitch <= 0) {
				userTweetSwitch = 2;
			}
			
		}

	}
	
	

	public static JPanel getMapPanel() {
		if (mapPanel != null) {
			return mapPanel;
		} else {

			final List<TileFactory> factories = new ArrayList<TileFactory>();

			TileFactoryInfo osmInfo = new OSMTileFactoryInfo();

			TileFactoryInfo veInfo = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);

			TileFactoryInfo googlemaps = new TileFactoryInfo("" + "GoogleMaps", 2, // min
					18, // max allowed zoom level
					maxZoom, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"http://mt" + (int) (Math.random() * 3 + 0.5) + ".google.com/vt/v=w2.106&hl=de", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = maxZoom - zoom;
					return this.baseURL + "&x=" + x + "&y=" + y + "&z=" + zoom;
				}
			};

			TileFactoryInfo stamen = new TileFactoryInfo("" + "Stamen B/W", 2, // min
					18, // max allowed zoom level
					maxZoom, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"http://tile.stamen.com/toner", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = maxZoom - zoom;
					String url = this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
					// http://tile.stamen.com/toner/{z}/{x}/{y}.png
					return url;
				}
			};
			
//			http://c.tiles.wmflabs.org/hillshading/${z}/${x}/${y}.png
			TileFactoryInfo hillshading = new TileFactoryInfo("" + "Hillshading", 2, // min
					18, // max allowed zoom level
					maxZoom, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"http://c.tiles.wmflabs.org/hillshading", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = maxZoom - zoom;
					String url = this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
					return url;
				}
			};
			
			 /* Start of Fix */
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

	        } };

	        SSLContext sc;
			try {
				sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (NoSuchAlgorithmException | KeyManagementException e1) {
				e1.printStackTrace();
			}

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) { return true; }
	        };
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        /* End of the fix*/
			
//			https://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png?apikey=8ab08504fd7e4a3bb4953f1080448e26
			TileFactoryInfo outdoor = new TileFactoryInfo("" + "Thunderforest_Outdoor", 2, // min
					18, // max allowed zoom level
					maxZoom, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"https://tile.thunderforest.com/outdoors", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = maxZoom - zoom;
					String url = this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png?apikey="+Thunder_API+"&ssl=true";
					return url;
				}
			};
			
//			https://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png?apikey=<insert-your-apikey-here>
			TileFactoryInfo transport = new TileFactoryInfo("" + "Thunderforest_Transport", 2, // min
					18, // max allowed zoom level
					maxZoom, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"https://tile.thunderforest.com/transport", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = maxZoom - zoom;
					String url = this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png?apikey="+Thunder_API+"&ssl=true";
					return url;
				}
			};
			
//			https://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png?apikey=<insert-your-apikey-here>
			TileFactoryInfo landscape = new TileFactoryInfo("" + "Thunderforest_Landscape", 2, // min
					18, // max allowed zoom level
					maxZoom, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"https://tile.thunderforest.com/landscape", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = maxZoom - zoom;
					String url = this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png?apikey="+Thunder_API+"&ssl=true";
					return url;
				}
			};


			// TileFactoryInfo osm_grey = new TileFactoryInfo(""
			// + "Osm Grey", 2, // min
			// 18, // max allowed zoom level
			// max, // max zoom level
			// 256, // tile size (must be square!!)
			// true, true, // x/y orientation is normal
			// "https://a.tiles.wmflabs.org/bw-mapnik", // baseURL
			// "x", "y", "z") {
			// public String getTileUrl(int x, int y, int zoom) {
			// zoom = max - zoom;
			// String url = this.baseURL + "/"+ zoom+ "/" + x + "/" + y
			// +".png?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
			//// https://a.tiles.wmflabs.org/bw-mapnik/$%7Bz%7D/$%7Bx%7D/$%7By%7D.png
			// return url;
			// }
			// };

			// factories.add(new DefaultTileFactory(osm_grey));

			factories.add(new DefaultTileFactory(stamen));
			factories.add(new DefaultTileFactory(googlemaps));
			factories.add(new DefaultTileFactory(landscape));
			factories.add(new DefaultTileFactory(transport));
//			factories.add(new DefaultTileFactory(outdoor));
			factories.add(new DefaultTileFactory(hillshading));
			factories.add(new DefaultTileFactory(osmInfo));
			factories.add(new DefaultTileFactory(veInfo));

			// faster Tile loading
			for (TileFactory tf : factories)
				((AbstractTileFactory) tf).setThreadPoolSize(tileThreads);

			// Setup JXMapViewer
			mapViewer = new JXMapViewer();
			mapViewer.setTileFactory(factories.get(0));

			// GeoPosition frankfurt = new GeoPosition(50.11, 8.68);
			GeoPosition boston = new GeoPosition(42.367391, -71.065063);
			
			// Set the focus
			mapViewer.setZoom(zoom);
			mapViewer.setAddressLocation(boston);

			JPanel panel = new JPanel();
			JLabel label = new JLabel("Select a TileFactory ");

			String[] tfLabels = new String[factories.size()];
			for (int i = 0; i < factories.size(); i++) {
				tfLabels[i] = factories.get(i).getInfo().getName();
			}

			final JComboBox combo = new JComboBox(tfLabels);
			combo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					TileFactory factory = factories.get(combo.getSelectedIndex());
					mapViewer.setTileFactory(factory);
					// System.out.println(factory.getInfo().getName());
				}
			});

			panel.setLayout(new GridLayout());
			panel.add(label);
			panel.add(combo);

			mapPanel = new MapViewer(mapViewer);

			// mapPanel.add(south, BorderLayout.SOUTH);
			mapPanel.add(mapViewer, BorderLayout.CENTER);
			mapPanel.add(panel, BorderLayout.NORTH);

			mapCon = new MapController(mapViewer);
			menu = new MapMenuPanel(mapCon);
			mapPanel.add(menu, BorderLayout.SOUTH);
			
			menu.setVisible(Lucene.SHOWHeatmap);
			

			// AddObserver
			mapCon.addObserver(mapPanel);
			mapViewer.addMouseWheelListener(new MyZoomMouseWheelListener(mapViewer, mapCon));
			mapViewer.addMouseListener(new MyCenterMapListener(mapViewer, mapCon));
			// Add interactions
			MouseInputListener mia = new MyPanMouseListener(mapViewer, mapCon);
			mapViewer.addMouseListener(mia);
			mapViewer.addMouseMotionListener(mia);

			mapViewer.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
					// System.out.println("Button "+e.getButton()+" Released at: "+e.getPoint() );

					// Right Click Release Geo Search!
					if (e.getButton() == 3) {
						// System.out.println("Button "+e.getButton()+" Released at: "+e.getPoint());

						int topX = (int) Math.min(startPoint.getX(), e.getPoint().getX());
						int topY = (int) Math.min(startPoint.getY(), e.getPoint().getY());
						int width = (int) Math.abs(startPoint.getX() - e.getPoint().getX());
						int height = (int) Math.abs(startPoint.getY() - e.getPoint().getY());
						
						if (width == 0 || height == 0 )
							return;

						Rectangle rec = new Rectangle((int) topX, (int) topY, (int) width, (int) height);
						System.out.println(">>> GEO-Selection: " + rec.toString());

						// CONVERT to Lat Long
						// mapViewer.convertGeoPositionToPoint(pos)
						GeoPosition p1 = mapViewer.convertPointToGeoPosition(new Point(topX, topY));
						GeoPosition p2 = mapViewer.convertPointToGeoPosition(new Point(topX + width, topY + height));

						Lucene l = Lucene.INSTANCE;
						while (!l.isInitialized) {
							continue;
						}

						Graphics g = mapViewer.getGraphics();
						g.clearRect(topX, topY, width, height);
						mapViewer.paint(g);
						// mapViewer.validate();

						// GEO Test
						// result = l.ADDGeoQuery(42.2279, 42.3969, -71.1908, -70.9235);

						// int afterDot = 5;
						// double minLat = Math.min(normalizedCoordinate(p2.getLatitude(), afterDot),
						// normalizedCoordinate(p1.getLatitude(), afterDot));
						// double maxLat = Math.max(normalizedCoordinate(p2.getLatitude(), afterDot),
						// normalizedCoordinate(p1.getLatitude(), afterDot));
						// double minLong = Math.min(normalizedCoordinate(p1.getLongitude(), afterDot),
						// normalizedCoordinate(p2.getLongitude(), afterDot));
						// double maxLong = Math.max(normalizedCoordinate(p1.getLongitude(), afterDot),
						// normalizedCoordinate(p2.getLongitude(), afterDot));
						double minLat = Math.min(p2.getLatitude(), p1.getLatitude());
						double maxLat = Math.max(p2.getLatitude(), p1.getLatitude());
						double minLong = Math.min(p1.getLongitude(), p2.getLongitude());
						double maxLong = Math.max(p1.getLongitude(), p2.getLongitude());

						// System.out.println(">>> BBOX Rec( "+ geoSelection[0] +" "+geoSelection[1]+ ",
						// "+geoSelection[2]+" "+geoSelection[3]+")");
						System.out.println(
								">>> BBOX for Lucene(" + minLat + " " + maxLat + " , " + minLong + " " + maxLong + ")");

						Result result = l.ADDGeoQuery(minLat, maxLat, minLong, maxLong);
						ScoreDoc[] data = result.getData();
						TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
							@Override
							public void execute() {
								result.setTimeCounter(l.createTimeBins(TimeBin.HOURS, data));
								l.showInTimeLine(result.getTimeCounter());
								// l.changeTimeLine(TimeBin.MINUTES);
							}
						};
						lilt.start();
						
						GraphCreatorThread graphThread = new GraphCreatorThread(l) {

							@Override
							public void execute() {
								l.createGraphView(data);
//								l.createSimpleGraphView(data);
							}
						};
						graphThread.start();

						l.createMapMarkers(data, true);
						l.changeHistogramm(result.getHistoCounter());
//						l.initCountriesMap();

						// l.createGraphML_Mention(result, true);
						// l.createGraphML_Retweet(result, true);
					}

				}

				// private double normalizedCoordinate(double latitude, int afterDot) {
				//
				// String s = latitude+"";
				// int dotIndex = s.indexOf(".");
				// if (s.length() > (dotIndex+afterDot))
				// s = s.substring(0, (dotIndex+afterDot));
				//
				// return Double.parseDouble(s);
				// }

				@Override
				public void mousePressed(MouseEvent e) {
					// System.out.println("Button "+e.getButton()+" Pressed at: "+e.getPoint());
					// Right Click
					if (e.getButton() == 3) {
						startPoint = e.getPoint();
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					Map<MapGridRectangle, List<String>> cells = mapCon.getGridCells(mapViewer.getZoom());
					Rectangle recViewPort = mapViewer.getViewportBounds();
					Lucene l = Lucene.INSTANCE;
					
					if (e.getButton() == 1) {
						
						ArrayList<MyEdge> foundEdges = new ArrayList<>();
						ArrayList<MyUser> foundUsers = new ArrayList<>();

						if (cells == null) {
							return;
						}
						for (MapGridRectangle rec : cells.keySet()) {

							if (rec.contains(e.getX() + recViewPort.getX(), e.getY() + recViewPort.getY())) {
								int x = rec.x-(int)recViewPort.getX();
								int y = rec.y-(int)recViewPort.getY();
								// get LatLong from Rectangle
								GeoPosition p1 = mapViewer.convertPointToGeoPosition(new Point(x,y));
								GeoPosition p2 = mapViewer.convertPointToGeoPosition(new Point(x + rec.width, y + rec.height));
								double minLat = Math.min(p2.getLatitude(), p1.getLatitude());
								double maxLat = Math.max(p2.getLatitude(), p1.getLatitude());
								double minLong = Math.min(p1.getLongitude(), p2.getLongitude());
								double maxLong = Math.max(p1.getLongitude(), p2.getLongitude());
								
								// Highlight
								// isSelection
								if (MapPanelCreator.mapCon.isSelection()) {
									
									for (MyEdge edge : DetailedGraphCreator.allEdges) {
										//Edge geo in Rectangle
										double lat = edge.getLatitude();
										double lon = edge.getLongitude();
										if (lat == 0.0 && lon == 0.0) {
											continue;
										}
										// is in Rectangle --> ADD
										if (lat > minLat && lat < maxLat && 
												lon > minLong && lon < maxLong) {
											foundEdges.add(edge);
										}
									}
									for (MyUser user : DetailedGraphCreator.allUser) {
										//Edge geo in Rectangle
										double lat = user.getLatitude();
										double lon = user.getLongitude();
										if (lat == 0.0 && lon == 0.0) {
											continue;
										}
										// is in Rectangle --> ADD
										if (lat > minLat && lat < maxLat && 
												lon > minLong && lon < maxLong) {
											foundUsers.add(user);
										}
									}
									
									System.out.println("Highlight "+foundEdges.size()+" Edge and "+foundUsers.size()+" Users");
									l.printlnToConsole(foundEdges.size()+" Edge and "+foundUsers.size()+" Users");
									
								}
								else {
									// no Selection, has no User-location, get Geo from last result
								
									Collection<MyEdge> edges = GeneralGraphCreator.getEdges();
									for (MyEdge edge : edges) {
										//Edge geo in Rectangle
										double lat = edge.getLatitude();
										double lon = edge.getLongitude();
										if (lat == 0.0 && lon == 0.0) {
											continue;
										}
										// is in Rectangle --> ADD
										if (lat > minLat && lat < maxLat && 
												lon > minLong && lon < maxLong) {
											foundEdges.add(edge);
										}
									}
									
									System.out.println("Highlight "+foundEdges.size()+" Edge and "+foundUsers.size()+" Users");
									l.printlnToConsole(foundEdges.size()+" Edge and "+foundUsers.size()+" Users");
									
								}
								
								GeneralGraphCreator.highlightUsers(foundUsers);
								GeneralGraphCreator.highlightEdges(foundEdges);
								
								break;
								
							}
						}
						
					}
					else if (e.getButton() == 3) {
						
						for (MapGridRectangle rec : cells.keySet()) {

							if (rec.contains(e.getX() + recViewPort.getX(), e.getY() + recViewPort.getY())) {

								int x = rec.x-(int)recViewPort.getX();
								int y = rec.y-(int)recViewPort.getY();
								// get LatLong from Rectangle
								GeoPosition p1 = mapViewer.convertPointToGeoPosition(new Point(x,y));
								GeoPosition p2 = mapViewer.convertPointToGeoPosition(new Point(x + rec.width, y + rec.height));
								
								double minLat = Math.min(p2.getLatitude(), p1.getLatitude());
								double maxLat = Math.max(p2.getLatitude(), p1.getLatitude());
								double minLong = Math.min(p1.getLongitude(), p2.getLongitude());
								double maxLong = Math.max(p1.getLongitude(), p2.getLongitude());

								System.out.println(">>> GEO-Selection: " + rec.toString());

								String queryType = l.getQeryType();
								l.setQeryType("FUSE");
								Result result = l.ADDGeoQuery(minLat, maxLat, minLong, maxLong);
								ScoreDoc[] data = result.getData();
								l.setQeryType(queryType);
								TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
									@Override
									public void execute() {
										result.setTimeCounter(l.createTimeBins(TimeBin.HOURS, data));
										l.showInTimeLine(result.getTimeCounter());
										// l.changeTimeLine(TimeBin.MINUTES);
									}
								};
								lilt.start();

								GraphCreatorThread graphThread = new GraphCreatorThread(l) {

									@Override
									public void execute() {
										l.createGraphView(data);
										// l.createSimpleGraphView(data);
									}
								};
								graphThread.start();

								l.createMapMarkers(data, true);
								l.changeHistogramm(result.getHistoCounter());

							}
						}
//						// Filter For the correct rectangle
//					else if (e.getButton() == 3) {
//						Point p = mapViewer.getMousePosition();
//						int zooml = mapViewer.getZoom();
//						Map<MapGridRectangle, List<String>> cells = mapCon.getGridCells(mapViewer.getZoom());
//						Rectangle recViewPort = mapViewer.getViewportBounds();
//						
//						Lucene l = Lucene.INSTANCE;
//
//						if (cells == null) {
//							return;
//						}
//						for (MapGridRectangle rec : cells.keySet()) {
//
//							if (rec.contains(e.getX() + recViewPort.getX(), e.getY() + recViewPort.getY())) {
//
//								// mapViewer.convertPointToGeoPosition(pt)
//								//Bounding Box
//								GeoPosition p1 = mapViewer.convertPointToGeoPosition(new Point(rec.x, rec.y));
//								GeoPosition p2 = mapViewer.convertPointToGeoPosition(new Point(rec.x+rec.width, rec.y+rec.height));
//								geoSelection[0] = p1.getLongitude();
//								geoSelection[1] = p1.getLatitude();
//								geoSelection[2] = p2.getLongitude();
//								geoSelection[3] = p2.getLatitude();
//								double minLat = Math.min(p2.getLatitude(), p1.getLatitude());
//								double maxLat = Math.max(p2.getLatitude(), p1.getLatitude());
//								double minLong = Math.min(p1.getLongitude(), p2.getLongitude());
//								double maxLong = Math.max(p1.getLongitude(), p2.getLongitude());
//								Result result = l.ADDGeoQuery(minLat, maxLat, minLong, maxLong);
//								ScoreDoc[] data = result.getData();
//								TimeLineCreatorThread lilt = new TimeLineCreatorThread(l) {
//									@Override
//									public void execute() {
//										result.setTimeCounter(l.createTimeBins(TimeBin.HOURS, data));
//										l.showInTimeLine(result.getTimeCounter());
//										// l.changeTimeLine(TimeBin.MINUTES);
//									}
//								};
//								lilt.start();
//								
//								GraphCreatorThread graphThread = new GraphCreatorThread(l) {
//
//									@Override
//									public void execute() {
//										l.createGraphView(data);
////										l.createSimpleGraphView(data);
//									}
//								};
//								graphThread.start();
//
//								l.createMapMarkers(data, true);
//								l.changeHistogramm(result.getHistoCounter());
//								
//								break;
//							}
//						}
					}
				}
			});

			
			
			mapViewer.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
				}

				@Override
				public void mouseDragged(MouseEvent e) {

					// Paint Rectangle --> Geo Selection
					if (e.getButton() == 3) {
						// System.out.println("From "+startPoint.toString()+" TO
						// "+e.getPoint().toString());
						int topX = (int) Math.min(startPoint.getX(), e.getPoint().getX());
						int topY = (int) Math.min(startPoint.getY(), e.getPoint().getY());
						int width = (int) Math.abs(startPoint.getX() - e.getPoint().getX());
						int height = (int) Math.abs(startPoint.getY() - e.getPoint().getY());

						Graphics g = mapViewer.getGraphics();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								Color c;
								Lucene l = Lucene.INSTANCE;
								if (!l.isInitialized) {
									c = grey;
								} else {
									c = l.getColor();
								}
								g.setColor(c);
								g.fillRect(topX, topY, width, height);
								g.setColor(Color.BLACK);
								g.drawRect(topX, topY, width, height);
								g.setColor(new Color(0, 0, 0, 100));
								g.fillRect(topX, topY, width, height);
							}
						});
						mapViewer.paint(g);
						// mapViewer.validate();
					}
				}
			});
			
			return mapPanel;
		}
	}
	
	
	public static void showHeatmapMenu() {
		menu.setVisible(Lucene.SHOWHeatmap);
		mapPanel.revalidate();
	}
	
	

	public static TweetWayPoint createTweetWayPoint(String label, String type, double lat, double lon) {

		if (!loadedIcons) {
			loadTweetIcons();
			loadedIcons = true;
		}

		GeoPosition geo = new GeoPosition(lat, lon);

		ImageIcon icon = null;
		// Check the sentiment for the right icon
		// if (sentiment.equals("positive"))
		// icon = tweetIcon_p;
		// else if (sentiment.equals("negative"))
		// icon = tweetIcon_n;
		// else
		// icon = tweetIcon_;

		switch (type.toLowerCase()) {
		case "pos":
			icon = tweetIcon_p;
			break;
		case "neg":
			icon = tweetIcon_n;
			break;
		case "neu":
			icon = tweetIcon_;
			break;

		case "family":
			icon = tweetIcon_family;
			break;
		case "food":
			icon = tweetIcon_food;
			break;
		case "computers_technology":
			icon = tweetIcon_computer;
			break;
		case "other":
			icon = tweetIcon_other;
			break;
		case "recreation_sports":
			icon = tweetIcon_rec_sport;
			break;
		case "politics":
			icon = tweetIcon_politics;
			break;
		case "marketing":
			icon = tweetIcon_marketing;
			break;
		case "education":
			icon = tweetIcon_education;
			break;
		case "pets":
			icon = tweetIcon_pets;
			break;
		case "health":
			icon = tweetIcon_health;
			break;
		case "news_media":
			icon = tweetIcon_news;
			break;
		case "music":
			icon = tweetIcon_music;
			break;
		case "-1":
			icon = neg1;
			break;
		case "-2":
			icon = neg2;
			break;
		case "-3":
			icon = neg3;
			break;
		case "-4":
			icon = neg4;
			break;
		case "-5":
			icon = neg5;
			break;
		case "1":
			icon = pos1;
			break;
		case "2":
			icon = pos2;
			break;
		case "3":
			icon = pos3;
			break;
		case "4":
			icon = pos4;
			break;
		case "5":
			icon = pos5;
			break;
		case "0":
			icon = pos1;
			break;

		default:
			icon = tweetIcon_;
			break;
		}

		return new TweetWayPoint(label, icon, geo);
	}

	public static void addWayPoint(SwingWaypoint marker) {
		waypoints.add(marker);

		// mapViewer.add(marker.getButton());

		// FSAVE points
	}

	public static void showWayPointsOnMap() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				swingWaypointPainter.setWaypoints(waypoints);

				for (SwingWaypoint p : waypoints) {

					Rectangle viewport = mapViewer.getViewportBounds();
					Point2D point = mapViewer.getTileFactory().geoToPixel(p.getPosition(), mapViewer.getZoom());

					int buttonX = (int) (point.getX() - viewport.getX());
					int buttonY = (int) (point.getY() - viewport.getY());

					// SHOW only viewport
					if (buttonY <= 0 || buttonX <= 0 || buttonY > viewport.getHeight() || buttonX > viewport.getWidth())
						continue;
					
					// TODO .. no overplot of buttons .. 
					mapViewer.add(p.getButton());

					// if (p instanceof TweetWayPoint)
					// mapViewer.add(((TweetWayPoint)p).getButton());
				}

				 mapViewer.setAddressLocation(mapViewer.getCenterPosition());
				 mapViewer.setOverlayPainter(swingWaypointPainter);
				 mapViewer.revalidate();
				 mapViewer.repaint();

//				mapPanel.drawGrid();
			
				// Set the focus
				// mapViewer.setAddressLocation(mapViewer.getCenterPosition());

			}
		});

	}

	public static void addWayPointsToMap(ArrayList<SwingWaypoint> markers) {

		for (SwingWaypoint point : markers) {
			waypoints.add(point);
		}
		swingWaypointPainter.setWaypoints(waypoints);
		mapViewer.repaint();
	}

	public static void clearWayPoints(boolean clearList) {
		if (mapPanel == null) {
			MapPanelCreator.getMapPanel();
		}
		if (!waypoints.isEmpty())
			mapViewer.removeAll();
		
		if (clearList) {
			waypoints.clear();
			mapCon.clearSelection();
		}
		
//		int componets = mapViewer.getComponentCount();
//		for (int i = 0; i < componets; i++) {
//			mapViewer.remove(mapViewer.getComponent(i));
//		}
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				mapViewer.removeAll();
////				mapViewer.setZoom(16);
//			}
//		});
		mapViewer.repaint();
//		mapViewer.updateUI();
		mapViewer.revalidate();
		
	}
	
	public static void addDataChanged() {
		mapCon.addDataChanged();
	}
	

	public static void dataChanged() {
		mapCon.dataChanged();
	}
	
	
	public static void zoomToBestFit(Set<GeoPosition> points) {
		mapViewer.zoomToBestFit(points , 1);
//		mapViewer.repaint();
	}
	
	
	public static void centerMapGeoPoint(Coordinate src) {
		
		GeoPosition g = new GeoPosition(src.y, src.x);
		Point2D gp = mapViewer.getTileFactory().geoToPixel(g, mapViewer.getZoom());
		mapViewer.setCenter(gp);
		mapViewer.repaint();
	}
	
	public static int getZoomLevel() {
		return mapViewer.getZoom();
	}


}
