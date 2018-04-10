package impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.data.category.DefaultCategoryDataset;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

public class GroupDetailsWindow {

	private JFrame frame = new JFrame("Group Details");
	
	/* To split window 2 part as a CloudPanel and Histogram Panel ??*/
	JSplitPane splitPane = new JSplitPane();
	
	private ArrayList<MyEdge> detailsEdges;
	private ArrayList<MyUser> detailUsers;

	private JFreeChart hashtagHistogram;
	private JPanel contentPane;
	private DefaultCategoryDataset datasetHistogramm;
	private ChartPanel chartPanel;

	private JPanel cloudPanel;

	private boolean topX = true;
	private int topX_val = 15;

	public GroupDetailsWindow(ArrayList<MyEdge> detailsEdges, ArrayList<MyUser> detailUsers) {

		this.detailsEdges = detailsEdges;
		this.detailUsers = detailUsers;
		frame.setLocation(400, 200);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		datasetHistogramm = new DefaultCategoryDataset();
		hashtagHistogram = ChartFactory.createBarChart("Histogram of Hashtags", // chart title
				"", "Count", // range axis label
				datasetHistogramm, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				true, // tooltips
				false // URLs
		);

		chartPanel = new ChartPanel(hashtagHistogram, false);
		chartPanel.setMinimumSize(new Dimension(400, 300));
		hashtagHistogram.setBackgroundPaint(Color.white);
	
		cloudPanel = new JPanel(new FlowLayout(10));	
		contentPane.add(cloudPanel, (BorderLayout.NORTH));
		
		frame.setMinimumSize(new Dimension(400, 300));
		frame.add(chartPanel);
		frame.setVisible(false);
     
		
	}
	public void setVisible() {
		frame.setVisible(true);
	}

	public void changeHashtagDataset() {

		// Connection c = DBManager.getConnection();
		// Statement stmt;
		// try {
		// stmt = c.createStatement();
		// for (MyUser m : detailUsers) {
		// System.out.println(m.getName());
		// String contentResult = "Select so_tweets.tweet_hashtags " + "from so_tweets "
		// + "where so_tweets.user_screenname = '" + m.getName() + "'";
		//
		// ResultSet rs = stmt.executeQuery(contentResult);
		//
		// while (rs.next()) {
		//
		// String hashtag = rs.getString("tweet_hashtags");
		// System.out.println("hashtags:" + hashtag);
		// }
		//
		// }
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }

		HashMap<String, Integer> hashtagCounts = new HashMap<>();

		for (MyEdge e : detailsEdges) {
			String tags = getTagsFromTweets(e.getContent());
			for (String tag : tags.split(" ")) {
				if (hashtagCounts.containsKey(tag)) {
					// freq ++
					hashtagCounts.put(tag, 1 + hashtagCounts.get(tag).intValue());
				} else {
					hashtagCounts.put(tag, new Integer(1));
				}
			}
		}

		int[] dataSeries = null;
		String[] categories = null;
		if (topX) {
			// Top 10 hashtag within SELECZTION
			hashtagCounts = sortHashMapByValues(hashtagCounts);

			dataSeries = new int[hashtagCounts.values().size()];
			categories = new String[hashtagCounts.keySet().size()];
			int j = 0;
			for (Entry<String, Integer> entry : hashtagCounts.entrySet()) {
				if (entry.getKey() == null) {
					continue;
				}
				categories[j] = entry.getKey();
				dataSeries[j] = entry.getValue();
				j++;
				if (j >= topX_val) {
					break;
				}
			}
		} else {
			// all used hashtags within SELECZTION
			dataSeries = new int[hashtagCounts.values().size()];
			categories = new String[hashtagCounts.keySet().size()];
			int j = 0;
			for (Entry<String, Integer> entry : hashtagCounts.entrySet()) {
				categories[j] = entry.getKey();
				dataSeries[j] = entry.getValue();
				j++;
			}
		}
		
		createTagCloud();

		if (dataSeries != null && categories != null) {
			for (int i = 0; i < dataSeries.length; i++) {
				String s = categories[i];
				int val = dataSeries[i];
				
				if (s == null) {
					continue;
				}
				
				datasetHistogramm.setValue(dataSeries[i], "Hashtags", categories[i]);
			}
		}
		
		

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
//				frame.pack();
				frame.validate();
				frame.repaint();
				frame.setVisible(true);
			}
		});
	}

	
	
	
	protected void createTagCloud() {
		
		Cloud cloud = new Cloud();
		for (MyEdge edge : detailsEdges) {
			for (String word : edge.getContent().split(" ")) {
				
				// remove punctuation and stopwords
				word = word.replaceAll("[!?,.;:]", "");
				if (word.length() <= 2) {
					continue;
				} else if (word.startsWith("#")) {
					continue;
				} else if (word.startsWith("@")) {
					continue;
				}
				
				cloud.addTag(word);
			}
		}
		
		for (Tag tag : cloud.tags()) {
			if (tag.getWeight() < 1) {
				continue;
			}
			final JLabel label = new JLabel(tag.getName());
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 10));
			cloudPanel.add(label);
		}
		
		
//		Random random = new Random();
//		for (String s : WORDS) {
//			for (int i = random.nextInt(50); i > 0; i--) {
//				cloud.addTag(s);
//			}
//		}
//		
//		for (Tag tag : cloud.tags()) {
//			final JLabel label = new JLabel(tag.getName());
//			label.setOpaque(false);
//			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 10));
//			cloudPanel.add(label);
//		}
	}

	
	
	private static String getTagsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			if (token.startsWith("#")) {
				token = token.replaceAll("[,:!?;]", "");
				output += token.substring(1) + " ";
			}
		}
		Cloud c = new Cloud();
		return output.trim();
	}

	private static String getMentionsFromTweets(String text_content) {
		String output = "";
		for (String token : text_content.split(" ")) {
			// Make a retweet to a simple mention:
			// RT @peter: -> @peter
			if (token.startsWith("@")) {
				token = token.replaceAll("[,:]", "");
				output += token.substring(1) + " ";
			}
		}
		// output = output.replace(":", "");
		return output.trim();
	}

	public LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			int val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Integer comp1 = passedMap.get(key);
				Integer comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

}
