package impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.ItemSelectable;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;


/** 
 * 
 * DefaultModalGraphMouse is a PluggableGraphMouse class that
 * pre-installs a large collection of plugins for picking and
 * transforming the graph. Additionally, it carries the notion
 * of a Mode: Picking or Translating. Switching between modes
 * allows for a more natural choice of mouse modifiers to
 * be used for the various plugins. The default modifiers are
 * intended to mimick those of mainstream software applications
 * in order to be intuitive to users.
 * 
 * To change between modes, two different controls are offered,
 * a combo box and a menu system. These controls are lazily created
 * in their respective 'getter' methods so they don't impact
 * code that does not intend to use them.
 * The menu control can be placed in an unused corner of the
 * GraphZoomScrollPane, which is a common location for mouse
 * mode selection menus in mainstream applications.
 * 
 * @author Tom Nelson
 */
public class MyModalGraphMouse<V,E> extends AbstractModalGraphMouse 
    implements ModalGraphMouse, ItemSelectable {
    
    /**
     * create an instance with default values
     *
     */
    public MyModalGraphMouse() {
        this(1.1f, 1/1.1f);
    }
    
    /**
     * create an instance with passed values
     * @param in override value for scale in
     * @param out override value for scale out
     */
    public MyModalGraphMouse(float in, float out) {
        super(in,out);
        loadPlugins();
		setModeKeyListener(new ModeKeyAdapter(this));
    }
    
    
    public void changeToPicking() {
		setMode(Mode.PICKING);
    }
    
    
    /**tttttttttttttttttttttt
     * create the plugins, and load the plugins for TRANSFORMING mode
     *
     */
    @Override
    protected void loadPlugins() {
    		MyPickingGraphMousePlugin<V, E> picker = new MyPickingGraphMousePlugin<V,E>();
    		picker.setLensColor(Color.black);
        pickingPlugin = picker;
        animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V,E>();
        translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK );
        scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, out, in);
//        rotatingPlugin = new RotatingGraphMousePlugin();
//        shearingPlugin = new ShearingGraphMousePlugin();

        add(scalingPlugin);
        setMode(Mode.TRANSFORMING);
    }
    
	public static class ModeKeyAdapter extends KeyAdapter implements MouseListener {
		private char t = 't';
		private char p = 'p';
		public ModalGraphMouse graphMouse;

		public ModeKeyAdapter(ModalGraphMouse graphMouse) {
			this.graphMouse = graphMouse;
		}

		public ModeKeyAdapter(char t, char p, ModalGraphMouse graphMouse) {
			this.t = t;
			this.p = p;
			this.graphMouse = graphMouse;
		}

		@Override
		public void keyTyped(KeyEvent event) {
			char keyChar = event.getKeyChar();
			if (keyChar == t) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				graphMouse.setMode(Mode.TRANSFORMING);
			} else if (keyChar == p) {
				((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				graphMouse.setMode(Mode.PICKING);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}

	@Override
	public Object[] getSelectedObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addItemListener(ItemListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeItemListener(ItemListener l) {
		// TODO Auto-generated method stub

	}
}
