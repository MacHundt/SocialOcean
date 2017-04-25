package impl;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import utils.FilesUtil;

public class GraphML_Helper {

	
	private static int topK = 5;
	static boolean ASC = true;
	static boolean DESC = false;
	
	
	/**
	 * This method creates an external mention.graphml file, to open it with other programs:
	 * For every mention (@) that is found in the result set a directed edge is created.
	 * @param result
	 * @param filename 
	 * @param searcher LuceneSearcher
	 * @param directed 
	 * @throws ParseException 
	 */
	public static void createGraphML_Mention(ScoreDoc[] result, IndexSearcher searcher, boolean directed, String filename)  {

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
        
        HashMap<String, String> nodeNames = new HashMap<>();   // screenName -> id
        HashMap<String, Integer> edgesMap = new HashMap<>();
        int nodesCounter = 0;
		
		try {
			Connection c = DBManager.getConnection();
			Statement stmt = c.createStatement();
			
			long now = Date.UTC(2017, 03, 3, 0, 0, 0);
			double maxFollow = Math.log10(8448672);				// loged
			double maxFriends = Math.log10(290739);
			double maxMessage = Math.log10(625638);
			long maxDate = Date.UTC(2013, 04, 24, 19, 0, 0);
			long minDate = Date.UTC(2006, 03, 21, 0, 0, 0);
			
			for (ScoreDoc doc : result) {
				int docID = doc.doc;
				Document document = searcher.doc(docID);
				String type = (document.getField("type")).stringValue();
				String id = (document.getField("id")).stringValue();
				long tweetdate = Long.parseLong((document.getField("date")).stringValue());

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
				long datetime = 0;
				int friends = 0;
				int followers = 0;
				int tweetCount = 0;
				// today - userCreationDate
				long time = 0;
				
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
					String cd = rs.getString(8).split(" ")[0];
					Date dt = new Date(Integer.parseInt(cd.split("-")[0]), (Integer.parseInt(cd.split("-")[1])) -1 , Integer.parseInt(cd.split("-")[2]));
					time = dt.getTime(); 	// the bigger the 'older' the user
					friends = rs.getInt(9);
					followers = rs.getInt(10);
					tweetCount = rs.getInt(11);
				}
				double sc_follow =  Math.log10(followers) / maxFollow;
				double sc_friend =  Math.log10(friends) / maxFriends;
				double sc_tweets =  Math.log10(tweetCount) / maxMessage;
				double sc_time = ((Math.log(time) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000))) / ((Math.log(maxDate) / Math.log(1000)) - (Math.log(minDate) / Math.log(1000)));
				
				double credible  = 1 - ((sc_follow + sc_friend +sc_tweets + sc_time) / 4.0) ;
				
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
					data.addAttribute(QName.get("key", "", getXMLNamesspace()), "label").addText(screenName); 
					Element data2 = nodes.addElement("data", getXMLNamesspace()); 
					data2.addAttribute(QName.get("key", "", getXMLNamesspace()), "fol").addText(""+followers); 	
					Element data3 = nodes.addElement("data", getXMLNamesspace()); 
					data3.addAttribute(QName.get("key", "", getXMLNamesspace()), "credi").addText(""+credible); 
				} else {
					nodeID = nodeNames.get(screenName);
//					Element nodes = graph.element(nodeID);
//					Element node = nodes.element(""+nodeID);
//					Element data2 = node.addElement("data", getXMLNamesspace()); 
//					data2.addAttribute(QName.get("key", "", getXMLNamesspace()), "fol").addText(""+followers); 
					
				}
				String sourceID = nodeID;
				
				
				
				// ADD target nodes
				for (String target : mentions) {
					
					if (target.isEmpty())
						continue;
					
					double targetCred = 0.5;
					
					if (!nodeNames.containsKey(target)) {
						nodeID = "n"+nodesCounter++;
						nodeNames.put(target, nodeID);
						Element nodes = graph.addElement("node", getXMLNamesspace()); 				
						nodes.addAttribute(QName.get("id", "", getXMLNamesspace()), nodeID); 			
						Element data = nodes.addElement("data", getXMLNamesspace()); 	
						data.addAttribute(QName.get("key", "", getXMLNamesspace()), "label").addText(target); 
//						Element data2 = nodes.addElement("data", getXMLNamesspace()); 
//						data2.addAttribute(QName.get("key", "", getXMLNamesspace()), "fol").addText(""+followers); 	
						Element data3 = nodes.addElement("data", getXMLNamesspace()); 
						data3.addAttribute(QName.get("key", "", getXMLNamesspace()), "credi").addText(""+targetCred); 
					} else {
						nodeID = nodeNames.get(target);
					}
					
			// ADD Edge:  source to Target
					
					// edgesNames   sourceID_targetID --> count
					String edgesNames = ""+sourceID+"_"+nodeID;
					if (edgesMap.containsKey(edgesNames)) {
						edgesMap.put(edgesNames, edgesMap.get(edgesNames) + 1);
					} 
					else {
						edgesMap.put(edgesNames, new Integer(1));
					}
					
					double edgeCredebility = createZipfScore(content) + ((hasUrl)? 0.4 : 0);
					
					Element edges = graph.addElement("edge", getXMLNamesspace()); 				
					edges.addAttribute(QName.get("source", "", getXMLNamesspace()), ""+sourceID); 
					edges.addAttribute(QName.get("target", "", getXMLNamesspace()), ""+nodeID);
					Element data = edges.addElement("data", getXMLNamesspace()); 				
					data.addAttribute(QName.get("key", "", getXMLNamesspace()), "senti").addText(""+sentiment); 	
					Element data2 = edges.addElement("data", getXMLNamesspace()); 
					data2.addAttribute(QName.get("key", "", getXMLNamesspace()), "cat").addText(""+category); 
					Element data3 = edges.addElement("data", getXMLNamesspace()); 
					data3.addAttribute(QName.get("key", "", getXMLNamesspace()), "content").addText(""+content); 
					Element data4 = edges.addElement("data", getXMLNamesspace()); 
					data4.addAttribute(QName.get("key", "", getXMLNamesspace()), "e_credi").addText(""+edgeCredebility); 
					
				}

			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		
		// ADD EDGES count   --- ( go through MAP ) - connection count
		
//		for (String edgeName : edgesMap.keySet()) {
//			String[] nodes = edgeName.split("_");
//			
//		}
		
		
		try {
			writeGraphML(graphMLDoc, filename);
			System.out.println("writen GraphFile to '"+filename+"'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void addKeysMentionGraph(Element root) {
		
//		***************  NODES *******************
		Element key = root.addElement("key", getXMLNamesspace()); 				
		key.addAttribute(QName.get("id", "", getXMLNamesspace()), "fol"); 				// followers
		key.addAttribute(QName.get("for", "", getXMLNamesspace()), "node"); 
		key.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "followers"); 
		key.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "int"); 
		
		Element label = root.addElement("key", getXMLNamesspace()); 				
		label.addAttribute(QName.get("id", "", getXMLNamesspace()), "label"); 				// label
		label.addAttribute(QName.get("for", "", getXMLNamesspace()), "node"); 
		label.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "label"); 
		label.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "string"); 
		
		Element credible = root.addElement("key", getXMLNamesspace()); 				
		credible.addAttribute(QName.get("id", "", getXMLNamesspace()), "credi"); 				// credible
		credible.addAttribute(QName.get("for", "", getXMLNamesspace()), "node"); 
		credible.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "credible"); 
		credible.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "double"); 
		
		
		
//		***************  EDGES *******************
		Element senti = root.addElement("key", getXMLNamesspace()); 				
		senti.addAttribute(QName.get("id", "", getXMLNamesspace()), "senti"); 			// sentiment
		senti.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		senti.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "sentiment"); 
		senti.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "double"); 
		
		Element cat = root.addElement("key", getXMLNamesspace()); 				
		cat.addAttribute(QName.get("id", "", getXMLNamesspace()), "cat"); 				// category
		cat.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		cat.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "category"); 
		cat.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "string");
		
		Element content = root.addElement("key", getXMLNamesspace()); 				
		content.addAttribute(QName.get("id", "", getXMLNamesspace()), "content"); 				// content
		content.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		content.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "content"); 
		content.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "string");
		
		Element credi = root.addElement("key", getXMLNamesspace()); 				
		credi.addAttribute(QName.get("id", "", getXMLNamesspace()), "e_credi"); 				// credi
		credi.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
		credi.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "credibility"); 
		credi.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "double");
		
//		Element count = root.addElement("key", getXMLNamesspace()); 				
//		count.addAttribute(QName.get("id", "", getXMLNamesspace()), "count"); 				// count
//		count.addAttribute(QName.get("for", "", getXMLNamesspace()), "edge"); 
//		count.addAttribute(QName.get("attr.name", "", getXMLNamesspace()), "count"); 
//		count.addAttribute(QName.get("attr.type", "", getXMLNamesspace()), "int"); 
	}
	

	private static void addHeader(Element root) {
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
		
		String path = FilesUtil.getPathOfRefFile();
		path = path.replaceAll(FilesUtil.getReferenceFile(), "graphs/"+name);

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
	
}
