package impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
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
	private static int max = 19;
	public static int zoom = 16;
	
	private static int tileThreads = 4;

	private static MapController mapCon;
	private static MapMenuPanel menu;

	private static Set<SwingWaypoint> waypoints = new HashSet<>();
	private static WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();

	private static ImageIcon tweetIcon_p;
	private static ImageIcon tweetIcon_n;
	private static ImageIcon tweetIcon_;

	private static ImageIcon tweetIcon_health;
	private static ImageIcon tweetIcon_pets;
	private static ImageIcon tweetIcon_music;
	private static ImageIcon tweetIcon_family;
	private static ImageIcon tweetIcon_politics;
	private static ImageIcon tweetIcon_marketing;
	private static ImageIcon tweetIcon_education;
	private static ImageIcon tweetIcon_rec_sport;
	private static ImageIcon tweetIcon_news;
	private static ImageIcon tweetIcon_computer;
	private static ImageIcon tweetIcon_food;
	private static ImageIcon tweetIcon_other;

	private static boolean loadedIcons = false;

	private static Point startPoint;

	private static double[] geoSelection = new double[4];

	private static Color grey = new Color(240, 240, 240, 50); // light grey, high opacity --> NORMAL

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
			if (GraphPanelCreator3.SELECTED) {
				painters.removeIf(
					p -> p instanceof GlyphPainter || p instanceof GridPainter || p instanceof CountryPainter);
				showWayPointsOnMap();
				return;
			}
			else 
				painters.removeIf(
						p -> p instanceof GlyphPainter || p instanceof GridPainter || p instanceof WaypointPainter
						|| p instanceof CountryPainter);

			System.out.println("ZOOM >> " + map.getZoom());

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
			} else {
				mapViewer.removeAll();
			}
			
			if (Lucene.SHOWHeatmap) {
				GridPainter gp = new GridPainter(mapCon);
//				GlyphPainter glp = new GlyphPainter(mapCon);
				painters.add(gp);
//				painters.add(glp);
			} else {
				CountryPainter cp = new CountryPainter(mapCon);
				painters.add(cp);
			}
			
			painters.add(swingWaypointPainter);
			CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
			map.setOverlayPainter(painter);
			map.revalidate();
			map.repaint();
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
					max, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"http://mt" + (int) (Math.random() * 3 + 0.5) + ".google.com/vt/v=w2.106&hl=de", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = max - zoom;
					return this.baseURL + "&x=" + x + "&y=" + y + "&z=" + zoom;
				}
			};

			TileFactoryInfo stamen = new TileFactoryInfo("" + "Stamen B/W", 2, // min
					18, // max allowed zoom level
					max, // max zoom level
					256, // tile size (must be square!!)
					true, true, // x/y orientation is normal
					"http://tile.stamen.com/toner", // baseURL
					"x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = max - zoom;
					String url = this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
					// http://tile.stamen.com/toner/{z}/{x}/{y}.png
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
			factories.add(new DefaultTileFactory(osmInfo));
			factories.add(new DefaultTileFactory(veInfo));
			factories.add(new DefaultTileFactory(googlemaps));

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

						Rectangle rec = new Rectangle((int) topX, (int) topY, (int) width, (int) height);
						System.out.println(">>> GEO-Selection: " + rec.toString());

						// CONVERT to Lat Long
						// mapViewer.convertGeoPositionToPoint(pos)
						GeoPosition p1 = mapViewer.convertPointToGeoPosition(new Point(topX, topY));
						GeoPosition p2 = mapViewer.convertPointToGeoPosition(new Point(topX + width, topY + height));

						geoSelection[0] = p1.getLongitude();
						geoSelection[1] = p1.getLatitude();
						geoSelection[2] = p2.getLongitude();
						geoSelection[3] = p2.getLatitude();

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
					// clicked in Cell?
//					Point p = e.getPoint();
					Point p = mapViewer.getMousePosition();
					int zooml = mapViewer.getZoom();
					Map<MapGridRectangle, List<Document>> cells = mapCon.getGridCells(mapViewer.getZoom());
					if (cells == null) {
						return;
					}
					for (MapGridRectangle rec : cells.keySet()) {
						if (rec.contains(e.getX(), e.getY())) {
							System.out.println(cells.get(rec).size());
							// TODO
							
							break;
						}
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
		if (clearList)
			waypoints.clear();
		mapViewer.removeAll();
		mapViewer.repaint();
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
}
