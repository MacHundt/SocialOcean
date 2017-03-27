package impl;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.jxmapviewer.viewer.GeoPosition;


public class TweetWayPoint  extends SwingWaypoint {
    private final JButton button;
    private final String text;

    public TweetWayPoint(String text, ImageIcon icon, GeoPosition coord) {
        super(text, coord);
        this.text = text;
        
        button = new JButton(text.substring(0, 1));
	    button.setIcon(icon);
        
        button.setSize(26, 26);
        button.setPreferredSize(new Dimension(26, 26));
        button.addMouseListener(new SwingWaypointMouseListener());
        button.setVisible(true);
    }

    JButton getButton() {
        return button;
    }

    private class SwingWaypointMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            JOptionPane.showMessageDialog(button, "You clicked on " + text);
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

