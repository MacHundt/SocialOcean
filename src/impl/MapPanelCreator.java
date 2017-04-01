package impl;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.apache.lucene.search.ScoreDoc;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import com.itextpdf.text.log.SysoLogger;

import utils.Lucene;
import utils.Swing_SWT;

public class MapPanelCreator {

	private static JPanel mapPanel = null;
	private static JXMapViewer mapViewer = null;
	private static int max = 19;
	
	private static Set<SwingWaypoint> waypoints = new HashSet<>();
	private static WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
	
	private static ImageIcon tweetIcon_p;
	private static ImageIcon tweetIcon_n;
	private static ImageIcon tweetIcon_;
	
	private static boolean loadedIcons = false;
	
	private static Point startPoint;
	private static Rectangle geoSelection = new Rectangle();
	
	
	public static void loadTweetIcons() {
		// ## LOAD Icons
		ImageDescriptor st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/tweet.png");
		org.eclipse.swt.graphics.Image img = st.createImage();
		BufferedImage image = Swing_SWT.convertToAWT(img.getImageData());
		tweetIcon_ = new ImageIcon(image);

		st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/tweetn.png");
		img = st.createImage();
		image = Swing_SWT.convertToAWT(img.getImageData());
		tweetIcon_n = new ImageIcon(image);

		st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/tweetp.png");
		img = st.createImage();
		image = Swing_SWT.convertToAWT(img.getImageData());
		tweetIcon_p = new ImageIcon(image);

	}
	
	public static JPanel getMapPanel() {
		if (mapPanel != null) {
			return mapPanel;
		} else {
			
			mapPanel = new JPanel(new BorderLayout());

			final List<TileFactory> factories = new ArrayList<TileFactory>();

			TileFactoryInfo osmInfo = new OSMTileFactoryInfo();
			TileFactoryInfo veInfo = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
			TileFactoryInfo googlemaps = new TileFactoryInfo("" 
					+ "GoogleMaps", 2, // min
					15, // max allowed zoom level
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

			factories.add(new DefaultTileFactory(osmInfo));
			factories.add(new DefaultTileFactory(veInfo));
			factories.add(new DefaultTileFactory(googlemaps));
			
			
			// Setup JXMapViewer
			mapViewer = new JXMapViewer();
			mapViewer.setTileFactory(factories.get(0));

//			GeoPosition frankfurt = new GeoPosition(50.11, 8.68);
			GeoPosition boston = new GeoPosition(42.367391, -71.065063);

			// Set the focus
			mapViewer.setZoom(7);
			mapViewer.setAddressLocation(boston);
			
			// Add interactions
			MouseInputListener mia = new PanMouseInputListener(mapViewer);
			mapViewer.addMouseListener(mia);
			mapViewer.addMouseMotionListener(mia);

			mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
			
			mapViewer.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
//					System.out.println("Button "+e.getButton()+" Released at: "+e.getPoint() );
					
					// Right Click  --> Geo Search!
					if (e.getButton() == 3) {
						System.out.println("Button "+e.getButton()+" Pressed at: "+e.getPoint());
						
						int topX = (int) Math.min(startPoint.getX(), e.getPoint().getX());
						int topY = (int) Math.min(startPoint.getY(), e.getPoint().getY());
						int width = (int) Math.abs(startPoint.getX() - e.getPoint().getX());
						int height = (int) Math.abs(startPoint.getY() - e.getPoint().getY());
						
//						geoSelection.setBounds((int)topX, (int)topY, (int)width, (int) height);
						
						// Geo Selection on
						System.out.println("Geo Selection Rectangle ("+topX+" "+ topY +" TO "+topX+width +" "+ topY+height+")");
						// convert to Lat Long
//						mapViewer.convertGeoPositionToPoint(pos)
						GeoPosition p1 = mapViewer.convertPointToGeoPosition(new Point(topX, topY));
						GeoPosition p2 = mapViewer.convertPointToGeoPosition(new Point(topX+width, topY+height));
						System.out.println("Geo Selection BBOX in Space ("+p2.getLatitude()+" "+ p1.getLatitude() +" , "+p2.getLongitude() +" "+ p1.getLongitude()+")");
						
						Lucene l = Lucene.INSTANCE;
						ScoreDoc[] result = null;
						while (!l.isInitialized) {
							continue;
						}
						// GEO Test
						// result = l.ADDGeoQuery(42.2279, 42.3969, -71.1908, -70.9235);
						result = l.ADDGeoQuery(p2.getLatitude(), p1.getLatitude(), p2.getLongitude(), p1.getLongitude());
						l.showInMap(result, true);
					}
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
//					System.out.println("Button "+e.getButton()+" Pressed at: "+e.getPoint());
					
					// Right Click
					if (e.getButton() == 3) {
						System.out.println("Button "+e.getButton()+" Pressed at: "+e.getPoint());
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
				}
			});
			
			mapViewer.addMouseMotionListener(new MouseMotionListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					
					// Paint Rectangle   --> Geo Selection
					if (e.getButton() == 3) {
//						System.out.println("From X   TO   "+e.getPoint());
						double topX = Math.min(startPoint.getX(), e.getPoint().getX());
						double topY = Math.min(startPoint.getY(), e.getPoint().getY());
						double width = Math.abs(startPoint.getX() - e.getPoint().getX());
						double height = Math.abs(startPoint.getY() - e.getPoint().getY());
						
						geoSelection.setBounds((int)topX, (int)topY, (int)width, (int) height);
						
						// Paint Rectangle on Map  --> updateView
						// repaint(geoSelection)
					}
					
				}
			});
			
			
//			PropertyChangeListener changed = new PropertyChangeListener() {
//				
//				@Override
//				public void propertyChange(PropertyChangeEvent evt) {
//					// TODO Auto-generated method stub
//					
//				}
//			};
//			mapViewer.addPropertyChangeListener(listener);
			
			JPanel panel = new JPanel();
			JLabel label = new JLabel("Select a TileFactory ");
			
			String[] tfLabels = new String[factories.size()];
			for (int i = 0; i < factories.size(); i++)
			{
				tfLabels[i] = factories.get(i).getInfo().getName();
			}
			
			final JComboBox combo = new JComboBox(tfLabels);
			combo.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					TileFactory factory = factories.get(combo.getSelectedIndex());
					mapViewer.setTileFactory(factory);
//					System.out.println(factory.getInfo().getName());
				}
			});
			
			panel.setLayout(new GridLayout());
			panel.add(label);
			panel.add(combo);
			
			JButton btn = new JButton("TEST X");
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("WOW - this works :))");
					mapViewer.removeAll();
					GeoPosition frankfurt = new GeoPosition(50.11, 8.68);
					GeoPosition wiesbaden = new GeoPosition(50,  5, 0, 8, 14, 0);
			        GeoPosition mainz     = new GeoPosition(50,  0, 0, 8, 16, 0);
			        GeoPosition darmstadt = new GeoPosition(49, 52, 0, 8, 39, 0);
			        GeoPosition offenbach = new GeoPosition(50,  6, 0, 8, 46, 0);
					// Set the focus
			        // Create waypoints from the geo-positions
			        waypoints = new HashSet<SwingWaypoint>(Arrays.asList(
			        		new TweetWayPoint("Frankfurt", tweetIcon_, frankfurt),
			        		new TweetWayPoint("Wiesbaden", tweetIcon_, wiesbaden),
			                new TweetWayPoint("Mainz", tweetIcon_p, mainz),
			                new TweetWayPoint("Darmstadt", tweetIcon_n, darmstadt),
			                new TweetWayPoint("Offenbach", tweetIcon_n, offenbach)
			                ));

			        // Set the overlay painter
//			        WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
			        swingWaypointPainter.setWaypoints(waypoints);
			        mapViewer.setOverlayPainter(swingWaypointPainter);

			        // Add the JButtons to the map viewer
			        for (SwingWaypoint w : waypoints) {
			            mapViewer.add(w.getButton());
			        }
			        
			        mapViewer.setDrawTileBorders(false);
			        mapViewer.setZoom(7);
					mapViewer.setAddressLocation(frankfurt);
				}
			});
			
			
			JButton rem = new JButton("remove");
			rem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("remove");
					GeoPosition boston = new GeoPosition(42.367391, -71.065063);
					SwingWaypoint b = new SwingWaypoint("boston", boston);
					waypoints.clear();
					waypoints.add(b);
					
//					Set<SwingWaypoint> wp = swingWaypointPainter.getWaypoints();
//					for (SwingWaypoint p : wp) {
//						mapViewer.remove(p.getButton());
//					}
					
					mapViewer.removeAll();
					swingWaypointPainter.setWaypoints(waypoints);
					
					mapViewer.add(b.getButton());
					
					mapViewer.setDrawTileBorders(true);
					mapViewer.setOverlayPainter(swingWaypointPainter);
					
					// Set the focus
					mapViewer.setZoom(8);
					mapViewer.setAddressLocation(boston);
				}
			});
			
			JPanel south = new JPanel(new BorderLayout());
			south.add(btn, BorderLayout.WEST);
			south.add(rem, BorderLayout.EAST);
			
//			mapPanel.add(south, BorderLayout.SOUTH);
			mapPanel.add(mapViewer, BorderLayout.CENTER);
			mapPanel.add(panel, BorderLayout.NORTH);
			
			return mapPanel;
		}
	}
	
	public static TweetWayPoint createTweetWayPoint(String label, String sentiment, double lat, double lon ) {
		
		if (!loadedIcons) {
			loadTweetIcons();
			loadedIcons = true;
		}
		
		GeoPosition geo = new GeoPosition(lat, lon);
		
		ImageIcon icon = null;
		// Check the sentiment for the right icon
		if (sentiment.equals("positive"))
			icon = tweetIcon_p;
		else if (sentiment.equals("negative")) 
			icon = tweetIcon_n;
		else 
			icon = tweetIcon_;
			
		return new TweetWayPoint(label, icon, geo);
	}
	
//	public static TweetWayPoint createTweetWayPoint(String label, double sentiment, double lat, double lon ) {
//		
//		if (!loadedIcons) {
//			loadTweetIcons();
//			loadedIcons = true;
//		}
//		
//		GeoPosition geo = new GeoPosition(lat, lon);
//		
//		ImageIcon icon = null;
//		// Check the sentiment for the right icon
//		if (sentiment > 0.5)
//			icon = tweetIcon_p;
//		else if (sentiment < -0.5) 
//			icon = tweetIcon_n;
//		else 
//			icon = tweetIcon_;
//			
//		return new TweetWayPoint(label, icon, geo);
//	}
	
	
	public static void addWayPoint(TweetWayPoint marker) {
		waypoints.add(marker);
		mapViewer.add(marker.getButton());
		
		// FSAVE points
	}
	
	
	public static void showWayPointsOnMap() {
		
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
            	swingWaypointPainter.setWaypoints(waypoints);
        		mapViewer.setAddressLocation(mapViewer.getCenterPosition());
        		mapViewer.setOverlayPainter(swingWaypointPainter);
        		
        		System.out.println(swingWaypointPainter.getWaypoints().size());
        		
        		// Set the focus
        		mapViewer.setZoom(16);
//        		mapViewer.setAddressLocation(mapViewer.getCenterPosition());
            }
        });
		
	}
	
	
	public static void addWayPointsToMap(ArrayList<SwingWaypoint> markers) {
		
		for (SwingWaypoint point : markers) {
			waypoints.add(point);
		}
		swingWaypointPainter.setWaypoints(waypoints);
		mapViewer.repaint();
		mapViewer.setAddressLocation(mapViewer.getCenterPosition());
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
	
}
