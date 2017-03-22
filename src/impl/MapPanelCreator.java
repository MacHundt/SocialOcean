package impl;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

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

public class MapPanelCreator {

	private static JPanel mapPanel = null;
	private static JXMapViewer mapViewer = null;
	private static int max = 19;
	
	public static JPanel getMapPanel() {
		if (mapPanel != null) {
			return mapPanel;
		} else {
			
			mapPanel = new JPanel(new BorderLayout());
			
			
			final List<TileFactory> factories = new ArrayList<TileFactory>();

			TileFactoryInfo osmInfo = new OSMTileFactoryInfo();
			TileFactoryInfo veInfo = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
			
			TileFactoryInfo googlemaps = new TileFactoryInfo("GoogleMaps", 2, // min
																				// zoom
																				// level
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
					GeoPosition frankfurt = new GeoPosition(50.11, 8.68);
					GeoPosition wiesbaden = new GeoPosition(50,  5, 0, 8, 14, 0);
			        GeoPosition mainz     = new GeoPosition(50,  0, 0, 8, 16, 0);
			        GeoPosition darmstadt = new GeoPosition(49, 52, 0, 8, 39, 0);
			        GeoPosition offenbach = new GeoPosition(50,  6, 0, 8, 46, 0);
					// Set the focus
			        // Create waypoints from the geo-positions
			        Set<SwingWaypoint> waypoints = new HashSet<SwingWaypoint>(Arrays.asList(
			        		new SwingWaypoint("Frankfurt", frankfurt),
			        		new SwingWaypoint("Wiesbaden", wiesbaden),
			                new SwingWaypoint("Mainz", mainz),
			                new SwingWaypoint("Darmstadt", darmstadt),
			                new SwingWaypoint("Offenbach", offenbach)
			                ));

			        // Set the overlay painter
			        WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
			        swingWaypointPainter.setWaypoints(waypoints);
			        mapViewer.setOverlayPainter(swingWaypointPainter);

			        // Add the JButtons to the map viewer
			        for (SwingWaypoint w : waypoints) {
			            mapViewer.add(w.getButton());
			        }
			        
			        mapViewer.setZoom(7);
					mapViewer.setAddressLocation(frankfurt);
					
				}
			});
			
			mapPanel.add(btn, BorderLayout.SOUTH);
			mapPanel.add(mapViewer, BorderLayout.CENTER);
			mapPanel.add(panel, BorderLayout.NORTH);
			
			return mapPanel;
		}
	}
}
