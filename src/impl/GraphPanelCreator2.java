package impl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import utils.DBManager;

public class GraphPanelCreator2 {
	
	private static JPanel graphPanel = null;

	public static JPanel getGraphPanel() {
		
		if (graphPanel != null) {
			return graphPanel;
		} else {
			
			graphPanel = new JPanel(new BorderLayout());
			
			mxGraph graph = new mxGraph();
			Object parent = graph.getDefaultParent();

			graph.getModel().beginUpdate();
			try
			{
				Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
						30);
				Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
						80, 30);
				graph.insertEdge(parent, null, "Edge", v1, v2);
			}
			finally
			{
				graph.getModel().endUpdate();
			}

			mxGraphComponent graphComponent = new mxGraphComponent(graph);
			graphComponent.setAutoExtend(true);
			graphComponent.setPanning(true);
			graphComponent.setWheelScrollingEnabled(true);
			
			JButton updateGraph_btn = new JButton("Update");
			
			updateGraph_btn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					// load Graph from DB and show
					try {
						updateGraph(graphComponent, graph);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			
			graphPanel.add(updateGraph_btn, BorderLayout.SOUTH);
			graphPanel.add(graphComponent, BorderLayout.CENTER);
			
			return graphPanel;
		}
	}

	protected static void updateGraph(mxGraphComponent graphComponent, mxGraph graph) throws SQLException {
		
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
		mxGraph graph_new = new mxGraph();
		Object parent = graph_new.getDefaultParent();
		
		graphComponent.setGraph(graph_new);
		
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
			
			// NO GraphLayout algorithms!! 
			
			Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
					30);
			Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
					80, 30);
			graph.insertEdge(parent, null, "Edge", v1, v2);
			
		}
		
		System.out.println(lines + " ResultLines");
//		ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate ASC Limit 1");
		
	}

}
