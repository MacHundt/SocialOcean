package impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import utils.DBManager;

public class GraphPanelCreator1 {
	
	private static JPanel graphPanel = null;

	public static JPanel getGraphPanel() {
		
		if (graphPanel != null) {
			return graphPanel;
		} else {
			
			graphPanel = new JPanel(new BorderLayout());
			
			SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	
	            	Graph graph = new SingleGraph("Tutorial 1");
	                Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
	                
	                viewer.enableAutoLayout();
	                
	                graph.addNode("A");
	                graph.addNode("B");
	                graph.addNode("C");
	                graph.addEdge("AB", "A", "B");
	                graph.addEdge("BC", "B", "C");
	                graph.addEdge("CA", "C", "A");

	                graph.addAttribute("ui.quality");
	                graph.addAttribute("ui.antialias");
	                
	                graphPanel.add((Component) viewer.getDefaultView(), BorderLayout.CENTER);
	            }
	        });
			
			System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
			
//			Graph graph = new MultiGraph("embedded");
//			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//			View view = viewer.addDefaultView(false);   // false indicates "no JFrame".
			// ...
			
			
			
			
			JButton updateGraph_btn = new JButton("Update");
			
			updateGraph_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					// load Graph from DB and show
//					try {
//						updateGraph(viewer, graph);
//					} catch (SQLException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
				}
			});
			
			graphPanel.add(updateGraph_btn, BorderLayout.SOUTH);
			
			return graphPanel;
		}
	}

	protected static void updateGraph(Viewer viewer, Graph graph) throws SQLException {
		
//		graphComponent.clear
//		graph.getModel().beginUpdate();
//		try
//		{
//			graph.selectAll();
//			for (Object b: graph.getSelectionCells()) {
//				graph.getModel().remove(b);
//			}
//		}
//		finally
//		{
//			graph.getModel().endUpdate();
//		}
		
		Connection c = DBManager.getConnection();
		Statement stmt;
		stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery("Select * from graph_edges");
		
		int lines = 0;
		HashMap<Integer, String> nodes = new HashMap<>();
		
		while (rs != null && rs.next()) {
			
			int source = rs.getInt(1);
			int target = rs.getInt(2);
			String source_name = rs.getString(3);
			String target_name = rs.getString(4);
			int count = rs.getInt(7);
			
			if (!nodes.containsKey(source)) {
				nodes.put(source, source_name);
			}
			if (!nodes.containsKey(target)) {
				nodes.put(target, target_name);
			}
			
			
		}
		
		System.out.println(lines + " ResultLines");
//		ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate ASC Limit 1");
		
	}

}
