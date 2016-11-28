package bostoncase.parts;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import swingintegration.example.EmbeddedSwingComposite;
import utils.Swing_SWT;

public class MapPart {
	
//	private MapWidget mapvis;
	
	private Composite mapComposite;
	private EmbeddedSwingComposite esc;		//Embedded Swing Component
	private JApplet rootContainer;
	
	@Inject
	public MapPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		// SWING/SWT integration 
		// http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
		
		//-- same LOOK and FEEL
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// for Windows, reducing flicker when resize
//			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// SWT - MapWidget
//		// Boston Coordinates
//		mapvis = new MapWidget(parent, SWT.NONE | SWT.CENTER,
//				new Point(MapWidget.lon2position(-71.18596, 11), MapWidget.lat2position(42.4355, 11)), 
//				11);
//		mapvis.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// No "Cheese" Pixels
		Swing_SWT util = new Swing_SWT();
		parent.addControlListener(util.CleanResize);
		
		mapComposite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND );
		Frame frame = SWT_AWT.new_Frame(mapComposite);
 		
		rootContainer = new JApplet();
		
		JButton btn = new JButton("TEST");
// 		rootContainer.add(btn);
//		JMapViewer map = new JMapViewer();
//		rootContainer.add(map);
		
		
//		JTable table = new JTable(5, 5);
//		rootContainer.add(table);
		
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		panel.add(btn, BorderLayout.CENTER);
		rootContainer.add(panel);
		rootContainer.validate();
 		
		frame.add(rootContainer);
		
//		Display.getCurrent().asyncExec(new Runnable() {
//             public void run() {
//         		Frame frame = SWT_AWT.new_Frame(mapComposite);
//         		
//         		frame.setSize(200, 200);
//         		frame.setVisible(true);
//         		
//         		frame.setName("FRAME-Name");
//         		
//         		Button awtB = new Button();
//         		frame.add(awtB);
//         		frame.pack();
//         		frame.setVisible(true);
         		
//         		rootContainer = new JApplet();
//         		
//         		
////         		JMapViewer map = new JMapViewer();
//         		JButton btn = new JButton("TEST");
//         		rootContainer.add(btn);
//         		
//         		frame.add(rootContainer);
//         		frame.pack();
//         		frame.setVisible(true);
//         		mapComposite.setVisible(true);
//        		mapComposite.setEnabled(true);
//        		mapComposite.pack();
//        		mapComposite.setToolTipText("Test Tip");
//        		mapComposite.setSize(200, 300);
//             }
//         });            
		
		
		
		
		
		
//		esc = new EmbeddedSwingComposite(parent, SWT.NONE) {
//			JMapViewer map;
//			
//			JScrollPane scrollPane;
//			JTable table;
//			JButton btn;
//			
//			@Override
//			public JComponent createSwingComponent() {
//				map = new JMapViewer();
//				scrollPane = new JScrollPane();
//				// table = new JTable();
//				btn = new JButton("TEST");
//				scrollPane.setViewportView(table);
//				return scrollPane;
//
//				// return map;
//			}
//			
//			public JComponent getMap() {
//				return map;
//			}
//				
//			
//		};
//		
//		esc.populate();

	}
	
	
	@Focus
	public void onFocus() {
		mapComposite.setFocus();
	}
	
	
}