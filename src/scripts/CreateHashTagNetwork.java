package scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import utils.DBManager;

public class CreateHashTagNetwork {
	
	private static String tweet_table = "so_tweets";
	private static String graphMLname = tweet_table;
	private static String outputFolderDir = "/Users/michaelhundt/Desktop/Hashtag_Network";
	
	private static int fetchsize = 1000;
	private static boolean LOCAL = false;
	private static boolean RCP = false;
	
	static ResultSet rs = null;
	
	static HashMap<String, HashNode> nodes = new HashMap<>();
	static HashMap<String, HashEdge> edges = new HashMap<>();
//	static ArrayList<HashEdge> edges = new ArrayList<>();

	public static void main(String[] args) {
		
		System.out.print("Create #-Network for '"+tweet_table+"'");
		Connection c = DBManager.getConnection(LOCAL, RCP);
		String query = "Select tweet_id, tweet_content from "+tweet_table +" where tweet_hashtags != '{}' Limit 50000" ;
		
		Statement st;
		try {
			c.setAutoCommit(false);
			st = c.createStatement();
			st.setFetchSize(fetchsize);
			rs = st.executeQuery(query);
			
			long tweet_counter = 0;
			int counter = 0;
			while (rs.next()) {
				tweet_counter++;
				long id = Long.parseLong(rs.getString(1));
				String text = rs.getString(2);
				// NO CONTENT
				if (text == null || text.isEmpty())
					continue;
				
				// NO HASHTAG
				if (!text.contains("#")) {
					continue;
				}
				counter++;
				
				// GET list if #
				ArrayList<String> hashtags = getTagsFromTweets(text);
				
				if (hashtags.size() <= 0	) 
					continue;

				// Add Nodes
				for (String hashtag : hashtags) {
					
					HashNode hashtag0 = new HashNode(hashtag);
					if (nodes.containsKey(hashtag)) {
						// freq ++
						nodes.get(hashtag).INC();
					} else {
						nodes.put(hashtag, hashtag0);
					}
				}
				
				int i = 0;
				for (; i < hashtags.size() - 1; i++) {
					for (int j = i + 1; j < hashtags.size(); j++) {
						HashNode one = nodes.get(hashtags.get(i));
						HashNode two = nodes.get(hashtags.get(j));
						// sort lexicographically: lower to the left side of "_".
						String edge_String = one.getName()+"_"+two.getName();
						boolean switchNodes = false;
						if (one.getName().compareTo(two.getName()) > 0) {
							edge_String = two.getName()+"_"+one.getName();
							switchNodes = true;
						}
						if (edges.containsKey(edge_String)) {
							// freq ++
							edges.get(edge_String).INC();
						} else {
							HashEdge edge = (switchNodes) ? new HashEdge(two, one) : new HashEdge(one, two);
							edges.put(edge_String, edge);
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("READING Nodes and Edges  --  DONE");
		
		File outFolder = new File(outputFolderDir);
		if (outFolder.isDirectory()) {
			createGraphML_Mention(false, graphMLname+"_hashtags.graphml", outFolder);
			System.out.println("WRITING graphML file  --  DONE");
		}
	}
	
	
	private static ArrayList<String> getTagsFromTweets(String text_content) {
		ArrayList<String> output = new ArrayList<>();
		for (String token : text_content.split(" ")) {
			if (token.startsWith("#") && token.length() > 1) {
				if (token.contains("\""))
					token = token.replace("\"", "\\\"");
				if (token.contains(","))
					token = token.replaceAll(",", "");
				if (token.contains("."))
					token = token.replaceAll(".", "");
				
				if (token.trim().isEmpty()) {
					continue;
				}
				if (token.length() > 1)
					output.add(token.substring(0).trim());
			}
		}
		return output;
	}
	
	
	/**
	 * This method creates an external mention.graphml file, to open it with other programs:
	 * For every mention (@) that is found in the result set a directed edge is created.
	 * @param result
	 * @param filename 
	 * @param searcher LuceneSearcher
	 * @param directed 
	 * @throws ParseException 
	 */
	public static void createGraphML_Mention(boolean directed, String filename, File exportDir)  {

		// result -> FUSE with Has@ -> Doc -> ID -> DB -> Content (get all @)
		// TODO if not clear list  -> try -- LOAD old graph file and 
		
		// HEADER
        org.dom4j.Document graphMLDoc = DocumentHelper.createDocument();
        Element root = graphMLDoc.addElement( "graphml", getXMLNamesspace() );
        
        addHeader(root);
        addKeysMentionGraph(root);
        
        Element graph = root.addElement("graph", getXMLNamesspace()); 				
        graph.addAttribute(QName.get("id", "", getXMLNamesspace()), "G"); 	
        graph.addAttribute(QName.get("edgedefault", "", getXMLNamesspace()), (directed) ? "directed" : "undirected"); 	
        graph.addAttribute(QName.get("parse.order", "", getXMLNamesspace()), "free"); 	
        // TODO Optimize Parser
        
        String nodeID = "";
        int nodesCounter = 0;
        for (HashNode node : nodes.values()) {
        		
        		if (node.getFreq() < 2) {
        			continue;
        		}
        	
        		nodeID = "n" + nodesCounter++;
        		node.add(nodeID);
//        		System.out.println(node.getName() + " - " + node.getFreq());
        		Element nodes = graph.addElement("node", getXMLNamesspace()); 				
			nodes.addAttribute(QName.get("id", "", getXMLNamesspace()), nodeID); 			
			Element data = nodes.addElement("data", getXMLNamesspace()); 		
			data.addAttribute(QName.get("key", "", getXMLNamesspace()), "label").addText(node.getName()); 
			Element data2 = nodes.addElement("data", getXMLNamesspace()); 
			data2.addAttribute(QName.get("key", "", getXMLNamesspace()), "freq").addText(""+node.getFreq()); 	
        }
        
        long e_counter = 0;
        String edgeId = "";
		for (HashEdge edge : edges.values()) {
			
			if (edge.getCounter() < 2)
				continue;
			
			edgeId = "e"+e_counter++;
			Element eedges = graph.addElement("edge", getXMLNamesspace()); 
			eedges.addAttribute(QName.get("id", "", getXMLNamesspace()), edgeId);
//			System.out.println(edge.getSource().getID() + "  --  "+edge.getSource().getName() + " --  "+ edge.getSource().getFreq() + "  EDGE: "+ edgeId);
			eedges.addAttribute(QName.get("source", "", getXMLNamesspace()), ""+edge.getSource().getID()); 
			eedges.addAttribute(QName.get("target", "", getXMLNamesspace()), ""+edge.getTarget().getID());
			Element data = eedges.addElement("data", getXMLNamesspace()); 				
			data.addAttribute(QName.get("key", "", getXMLNamesspace()), "frequent").addText(""+edge.getCounter()); 	
			
		}
		
		System.out.println("Nodes: "+ nodes.size());
		System.out.println("Edges: "+ edges.size());
			
		try {
			writeGraphML(graphMLDoc, filename, exportDir);
			System.out.println("writen GraphFile to '"+exportDir.getAbsolutePath()+"/"+filename+"'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private static void addHeader(Element root) {
	    root.addAttribute(QName.get("schemaLocation", "xsi", "http://www.w3.org/2001/XMLSchema-instance"),
	            "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd");
	}
	
	private static String getXMLNamesspace() {
		return "http://graphml.graphdrawing.org/xmlns/graphml";
	}
	
	
private static void addKeysMentionGraph(Element root) {
		
//		***************  NODES *******************
		
		Element label = root.addElement("key", getXMLNamesspace()); 				
		label.addAttribute(QName.get("id", "", getXMLNamesspace()), "label"); 				// label
		label.addAttribute(QName.get("for", "", getXMLNamesspace()), "node"); 
		label.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "label"); 
		label.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "string"); 
		
		Element credible = root.addElement("key", getXMLNamesspace()); 				
		credible.addAttribute(QName.get("id", "", getXMLNamesspace()), "freq"); 				// frequency
		credible.addAttribute(QName.get("for", "", getXMLNamesspace()), "node"); 
		credible.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "frequency"); 
		credible.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "int"); 
		
		
		
//		***************  EDGES *******************
		Element senti = root.addElement("key", getXMLNamesspace()); 				
		senti.addAttribute(QName.get("id", "", getXMLNamesspace()), "frequent"); 				// frequency
		senti.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		senti.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "frequency"); 
		senti.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "int"); 
		
	}
	

public void setOutputFolder(String path) {
	outputFolderDir = path;
}


public void setOutPutName(String filename) {
	graphMLname = filename;
}


/**
 * When the nodes and edges HashMap is changed just call <createGraphML_Mention()> to create a new graphMl output.
 * @param nodes
 * @param edges
 */
public void setNodesEdges(HashMap<String, HashNode> nodes, HashMap<String, HashEdge> edges ) {
	this.nodes = nodes;
	this.edges = edges;
}
	
	
	
private static void writeGraphML(org.dom4j.Document graphMLDoc, String name, File dir) throws IOException {
		
//		String path = FilesUtil.getPathOfRefFile();
//		path = path.replaceAll(FilesUtil.getReferenceFile(), "graphs/"+name);
		
		String path = dir.getAbsolutePath()+"/"+name;
		OutputFormat format = OutputFormat.createPrettyPrint();
		// lets write to a file
		XMLWriter writer = new XMLWriter(new FileWriter(path), format);
		writer.write(graphMLDoc);
		writer.close();

		// // Pretty print the document to System.out
//		 format = OutputFormat.createPrettyPrint();
//		 writer = new XMLWriter( System.out, format );
//		 writer.write( graphMLDoc );

		// Compact format to System.out
//		format = OutputFormat.createCompactFormat();
//		writer = new XMLWriter(System.out, format);
//		writer.write(graphMLDoc);
	}
	

}
