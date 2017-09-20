package socialocean.parts;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import socialocean.controller.MapController;

public class MapMenuPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7834412728232288279L;
	private JSlider cellSizeSlider;

	/**
	 * Create the panel.
	 */
	public MapMenuPanel(MapController mapCon) {
//		this.cellSizeSlider = new SteppingSlider(mapCon.getCellSize());
		
		this.cellSizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, mapCon.getCellSize()*4);
		this.cellSizeSlider.setPaintTicks(true);
		this.cellSizeSlider.setPaintLabels(true);
//		this.cellSizeSlider.setSnapToTicks(true);
		this.cellSizeSlider.setMinorTickSpacing(10);
		this.cellSizeSlider.setMajorTickSpacing(100);
		this.cellSizeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int val = (int) source.getValue();
				
					mapCon.setCellSize(val / 4);
				}

			}
		});

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		this.add(new JLabel("Cell Size: "));
		this.add(Box.createRigidArea(new Dimension(5, 20)));

		this.add(cellSizeSlider);
	}

//	public static class SteppingSlider extends JSlider {
//		private static final long serialVersionUID = -1195270044097152629L;
//		private static final Integer[] VALUES = { 16, 32, 64, 128, 256, 512, 1024 };
//		private static final Hashtable<Integer, JLabel> LABELS = new Hashtable<>();
//		static {
//			for (int i = 0; i < VALUES.length; ++i) {
//				LABELS.put(i, new JLabel(VALUES[i].toString()));
//			}
//		}
//
//		public SteppingSlider(int initVal) {		
//			super(0, VALUES.length - 1, 3);
//			setLabelTable(LABELS);
//			setPaintTicks(true);
//			setPaintLabels(true);
//			setSnapToTicks(true);
//			setMajorTickSpacing(1);
//		}
//
//		public int getDomainValue() {
//			return VALUES[getValue()];
//		}
//	}

}
