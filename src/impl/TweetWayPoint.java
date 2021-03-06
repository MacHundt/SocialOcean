package impl;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.jxmapviewer.viewer.GeoPosition;

import utils.DBManager;


public class TweetWayPoint  extends SwingWaypoint {
    private final JButton button;
    private final String text;
    
    private int breakLineAT = 80;

    public TweetWayPoint(String text, ImageIcon icon, GeoPosition coord) {
        super(text, coord);
        this.text = text;
        
//        button = new JButton(text.substring(0, 1));
        button = new JButton("Tweet");
	    button.setIcon(icon);
	    // to remote the spacing between the image and button's borders
	    button.setMargin(new Insets(0, 0, 0, 0));
	    // to add a different background
	    button.setBackground(null);
	    // to remove the border
	    button.setBorder(null);
        button.setSize(24,24);
//        button.setBackground(new Color(255, 255, 255, 255));
	    button.setPreferredSize(new Dimension(25, 25));
        button.addMouseListener(new SwingWaypointMouseListener());
        button.setVisible(true);
    }

    JButton getButton() {
        return button;
    }

    private class SwingWaypointMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        	String details = "";
        	Connection c = DBManager.getConnection();
        	try {
				Statement stmt = c.createStatement();
				String table = DBManager.getTweetdataTable();
				
//				String query = "select t.\"tweetScreenName\", t.\"tweetContent\", t.creationdate, t.sentiment, t.category, t.\"containsUrl\"  from "+table+" as t where t.tweetid = "+text;
				String query = "select t.user_screenname, t.relationship, t.tweet_content, t.tweet_creationdate, t.sentiment, t.category, t.hasurl, t.urls  from "+table+" as t where t.tweet_id = "+text;

				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					String scName = rs.getString("user_screenname");
					String relationship = rs.getString("relationship");
					String content = rs.getString("tweet_content");
					String date = rs.getString("tweet_creationdate");
					String sentiment = rs.getString("sentiment");
					String category = rs.getString("category");
					boolean hasUrl = rs.getBoolean("hasurl");
					String urls = rs.getString("urls");
					
					if (relationship.equals("Followed")) {
						details += "\n"+scName+" follows ";
						break;
					}
					
					
					details += "\n"+scName+" wrote on "+date+":";
					if (content.length() > breakLineAT) {
						// next line 
						details += "\n"+content.substring(0, breakLineAT);
						details += "\n"+content.substring(breakLineAT, content.length());
					} else {
						details += "\n"+content;
					}
					details += "\nSentiment: \t"+sentiment;
					details += "\nCategory: \t"+category;
					details += "\nhasURL: \t"+((hasUrl)? "true":"false");
					details += "\nURLs: \t"+ urls;
										
				}
				
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            JOptionPane.showMessageDialog(button, details, "Details", JOptionPane.PLAIN_MESSAGE ,button.getIcon());
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}

