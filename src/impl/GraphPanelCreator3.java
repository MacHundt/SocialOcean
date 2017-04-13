//package impl;
//
//import java.awt.BorderLayout;
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javax.swing.SwingUtilities;
//
//import org.gephi.graph.api.DirectedGraph;
//import org.gephi.graph.api.GraphController;
//import org.gephi.graph.api.GraphModel;
//import org.gephi.io.generator.plugin.RandomGraph;
//import org.gephi.io.importer.api.Container;
//import org.gephi.io.importer.api.ImportController;
//import org.gephi.io.processor.plugin.DefaultProcessor;
//import org.gephi.project.api.ProjectController;
//import org.gephi.project.api.Workspace;
//import org.openide.util.Lookup;
//
//public class GraphPanelCreator3 {
//	
//	private static JPanel graphPanel = null;
//
//	public static JPanel getGraphPanel() {
//		
//		if (graphPanel != null) {
//			return graphPanel;
//		} else {
//			
//			graphPanel = new JPanel(new BorderLayout());
//			
//			SwingUtilities.invokeLater(new Runnable() {
//	            public void run() {
//	            	
//	            	//Init a project - and therefore a workspace
//	            	ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
//	            	pc.newProject();
//	            	Workspace workspace = pc.getCurrentWorkspace();
//	            	 
//	            	//Generate a new random graph into a container
//	            	Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();
//	            	RandomGraph randomGraph = new RandomGraph();
//	            	randomGraph.setNumberOfNodes(500);
//	            	randomGraph.setWiringProbability(0.005);
//	            	randomGraph.generate(container.getLoader());
//	            	
//	            	//Append container to graph structure
//	            	ImportController importController = Lookup.getDefault().lookup(ImportController.class);
//	            	importController.process(container, new DefaultProcessor(), workspace);
//	            	
//	            	//See if graph is well imported
//	            	GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
//	            	DirectedGraph graph = graphModel.getDirectedGraph();
//	            	System.out.println("Nodes: " + graph.getNodeCount());
//	            	System.out.println("Edges: " + graph.getEdgeCount());
//	                
////	                graphPanel.add((Component)container, BorderLayout.CENTER);
////	            	graphPanel.add((Component)pc, BorderLayout.CENTER);
////	            	graphPanel.add((Component)workspace, BorderLayout.CENTER);
//	            }
//	        });
//			
//			System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
//			
////			Graph graph = new MultiGraph("embedded");
////			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
////			View view = viewer.addDefaultView(false);   // false indicates "no JFrame".
//			// ...
//			
//			
//			
//			
//			JButton updateGraph_btn = new JButton("Update");
//			
//			updateGraph_btn.addActionListener(new ActionListener() {
//				
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					
//					// load Graph from DB and show
////					try {
////						updateGraph(viewer, graph);
////					} catch (SQLException e1) {
////						// TODO Auto-generated catch block
////						e1.printStackTrace();
////					}
//				}
//			});
//			
//			graphPanel.add(updateGraph_btn, BorderLayout.SOUTH);
//			
//			return graphPanel;
//		}
//	}
//
////	protected static void updateGraph(Viewer viewer, Graph graph) throws SQLException {
////		
//////		graphComponent.clear
//////		graph.getModel().beginUpdate();
//////		try
//////		{
//////			graph.selectAll();
//////			for (Object b: graph.getSelectionCells()) {
//////				graph.getModel().remove(b);
//////			}
//////		}
//////		finally
//////		{
//////			graph.getModel().endUpdate();
//////		}
////		
////		Connection c = DBManager.getConnection();
////		Statement stmt;
////		stmt = c.createStatement();
////		ResultSet rs = stmt.executeQuery("Select * from graph_edges");
////		
////		int lines = 0;
////		HashMap<Integer, String> nodes = new HashMap<>();
////		
////		while (rs != null && rs.next()) {
////			
////			int source = rs.getInt(1);
////			int target = rs.getInt(2);
////			String source_name = rs.getString(3);
////			String target_name = rs.getString(4);
////			int count = rs.getInt(7);
////			
////			if (!nodes.containsKey(source)) {
////				nodes.put(source, source_name);
////			}
////			if (!nodes.containsKey(target)) {
////				nodes.put(target, target_name);
////			}
////			
////			
////		}
////		
////		System.out.println(lines + " ResultLines");
//////		ResultSet rs = stmt.executeQuery("Select creationdate from tweetdata order by creationdate ASC Limit 1");
////		
////	}
//
//}
