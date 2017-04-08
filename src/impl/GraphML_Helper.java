package impl;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import utils.DBManager;

public class GraphML_Helper {

	
	/**
	 * This method creates an external mention.graphml file, to open it with other programs:
	 * For every mention (@) that is found in the result set a directed edge is created.
	 * @param result
	 * @param filename 
	 * @param searcher LuceneSearcher
	 * @throws ParseException 
	 */
	public static void createGraphML_Mention(ScoreDoc[] result, IndexSearcher searcher, String filename)  {

		// result -> FUSE with Has@ -> Doc -> ID -> DB -> Content (get all @)
		
		// TODO if not clear list  -> try -- LOAD old graph file and 
		
		// HEADER
		
        org.dom4j.Document graphMLDoc = DocumentHelper.createDocument();
        Element root = graphMLDoc.addElement( "graphml", getXMLNamesspace() );
        
        addHeader(root);
        addKeys(root);
        
//        Element graph = root.addElement("graph");
        Element graph = root.addElement("graph", getXMLNamesspace()); 				
        graph.addAttribute(QName.get("id", "", getXMLNamesspace()), "G"); 	
        graph.addAttribute(QName.get("edgedefault", "", getXMLNamesspace()), "undirected"); 	
        graph.addAttribute(QName.get("parse.order", "", getXMLNamesspace()), "free"); 	
        // Optimize Parser
        
        HashMap<String, String> nodeNames = new HashMap<>();   // screenName -> id
        int nodesCounter = 0;
		
		try {
			Connection c = DBManager.getConnection();
			Statement stmt = c.createStatement();
			// ResultSet rs = stmt.executeQuery("Select creationdate from
			// tweetdata order by creationdate ASC Limit 1");
			// ResultSet rs = pre_statement_min.executeQuery();

			for (ScoreDoc doc : result) {
				int docID = doc.doc;
				Document document = searcher.doc(docID);
				String type = (document.getField("type")).stringValue();
				String id = (document.getField("id")).stringValue();

				// EDGE information
				String query = "";
				String screenName = "";
				String content = "";
				double sentiment = 0;
				String category = "other";
				boolean hasUrl = false;
				boolean isRetreet = false; // --> replyusername != null

				// NODE information --> user meta data ..
				int listedCount = 0;
				// Datetime
				int friends = 0;
				int followers = 0;
				int tweetCount = 0;
				// today - userCreationDate
				//

				switch (type) {
				case "twitter":
					query = "Select "
							+ "t.\"tweetScreenName\", t.\"tweetContent\", t.sentiment, t.category, t.\"containsUrl\", t.replytousername, "
							+ "t.userlistedcount, t.\"userCreationdate\", t.\"userFriendscount\" , "
							+ "t.\"userFollowers\", t.\"userStatusCount\"  from tweetdata as t where t.tweetid = "
							+ Long.parseLong(id);
					break;
				case "flickr":
					query = "Select t.sentiment from flickrdata as t where t.\"photoID\" = " + Long.parseLong(id);
					break;
				default:
					query = "Select t.sentiment from tweetdata as t where t.tweetid = " + Long.parseLong(id);
				}
				ResultSet rs = stmt.executeQuery(query);
				
				boolean isEmpty = true;
				
				while (rs.next()) {
					isEmpty = false;
					screenName = rs.getString(1);
					content = rs.getString(2);
					sentiment = rs.getInt(3);
					category = rs.getString(4);
					hasUrl = rs.getBoolean(5);
					isRetreet = (rs.getString(6).equals("null")) ? false : true;
					listedCount = rs.getInt(7);
					// DATE
					friends = rs.getInt(9);
					followers = rs.getInt(10);
					tweetCount = rs.getInt(11);
				}
				
				if (isEmpty) 
					continue;
				
				String mentionString = getMentionsFromTweets(content);
				if (mentionString.length() < 2) {
					continue;
				}
				String[] mentions = mentionString.split(" ");
				
				// ADD source node
				String nodeID = "";
				if (!nodeNames.containsKey(screenName)) {
					nodeID = "n" + nodesCounter++;
					nodeNames.put(screenName, nodeID);
					Element nodes = graph.addElement("node", getXMLNamesspace()); 				
					nodes.addAttribute(QName.get("id", "", getXMLNamesspace()), nodeID); 			
					Element data = nodes.addElement("data", getXMLNamesspace()); 				
					data.addAttribute(QName.get("key", "", getXMLNamesspace()), "fol").addText(""+followers); 	
				} else {
					nodeID = nodeNames.get(screenName);
				}
				String sourceID = nodeID;
				// ADD target nodes
				for (String target : mentions) {
					
					if (target.isEmpty())
						continue;
					
					if (!nodeNames.containsKey(target)) {
						nodeID = "n"+nodesCounter++;
						nodeNames.put(target, nodeID);
						Element nodes = graph.addElement("node", getXMLNamesspace()); 				
						nodes.addAttribute(QName.get("id", "", getXMLNamesspace()), nodeID); 			
						Element data = nodes.addElement("data", getXMLNamesspace()); 				
						data.addAttribute(QName.get("key", "", getXMLNamesspace()), "fol").addText(""+followers); 	
					} else {
						nodeID = nodeNames.get(target);
					}
					
					// ADD Edge:  source to Target
					Element edges = graph.addElement("edge", getXMLNamesspace()); 				
					edges.addAttribute(QName.get("source", "", getXMLNamesspace()), ""+sourceID); 
					edges.addAttribute(QName.get("target", "", getXMLNamesspace()), ""+nodeID);
					Element data = edges.addElement("data", getXMLNamesspace()); 				
					data.addAttribute(QName.get("key", "", getXMLNamesspace()), "senti").addText(""+sentiment); 	
					data.addAttribute(QName.get("key", "", getXMLNamesspace()), "cat").addText(""+category); 
					
				}

			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		
		try {
			writeGraphML(graphMLDoc, filename);
			System.out.println("writen GraphFile to '"+filename+"'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void addKeys(Element root) {
		
		
//		***************  NODES *******************
		Element key = root.addElement("key", getXMLNamesspace()); 				
		key.addAttribute(QName.get("id", "", getXMLNamesspace()), "fol"); 				// followers
		key.addAttribute(QName.get("for", "", getXMLNamesspace()), "node"); 
		key.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "followers"); 
		key.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "double"); 
		
		
//		***************  EDGES *******************
		
		Element senti = root.addElement("key", getXMLNamesspace()); 				
		senti.addAttribute(QName.get("id", "", getXMLNamesspace()), "senti"); 			// sentiment
		senti.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		senti.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "sentiment"); 
		senti.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "double"); 
		
		Element cat = root.addElement("key", getXMLNamesspace()); 				
		cat.addAttribute(QName.get("id", "", getXMLNamesspace()), "cat"); 				// category
		cat.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		cat.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "sentiment"); 
		cat.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "double"); 

	}
	

	private static void addHeader(Element root) {
		
//		<?xml version="1.0" encoding="UTF-8"?>
//		<graphml xmlns="http://graphml.graphdrawing.org/xmlns/graphml" 
//		xmlns:visone="http://visone.info/xmlns" 
//		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
//		xmlns:y="http://www.yworks.com/xml/graphml" 
//		xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd"
		
	    root.addAttribute(QName.get("schemaLocation", "xsi", "http://www.w3.org/2001/XMLSchema-instance"),
	            "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd");
		

	}
	
	private static String getXMLNamesspace() {
		return "http://graphml.graphdrawing.org/xmlns/graphml";
	}

	/**
	 * This method creates an external domain.graphml file, to open it with other programs:
	 * For every domain that is found in the result set a domain node is created and an edge from the user node
	 * @param result
	 * @param clearList boolean
	 */
	public static void createGraphML_Domain(ScoreDoc[] result, boolean clearList) {
		
	}
	
	
	private static void writeGraphML(org.dom4j.Document graphMLDoc, String name) throws IOException {

		OutputFormat format = OutputFormat.createPrettyPrint();
		// lets write to a file
		XMLWriter writer = new XMLWriter(new FileWriter(name), format);
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
	
	
	private static String getMentionsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("@")) {
				output += token.substring(1) + " ";
			}
		}
		return output.trim();
	}
	
}
