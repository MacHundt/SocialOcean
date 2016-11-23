 
package bostoncase.parts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import utils.Lucene;
import utils.Swing_SWT;
import utils.TermStats;

public class TopSelectionPart {
	
	private static TopSelectionPart INSTANCE;
	public static boolean isInitialized = false;
	
	private Composite composite;
	private JTable results;
	private DefaultTableModel resultDataModel;
	public int detailsColumns = 3;
	private JTable detail;
	private DefaultTableModel detailsDataModel;
	private int resultColumns = 4;
	
	@Inject
	public TopSelectionPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		Swing_SWT util = new Swing_SWT();
		parent.addControlListener(util.CleanResize);
		
		composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND );
		Frame frame = SWT_AWT.new_Frame(composite);
 		
		JApplet rootContainer = new JApplet();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel northSelection = new JPanel();
		panel.add(northSelection, BorderLayout.NORTH);
		northSelection.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblNewLabel = new JLabel("get Top X");
		northSelection.add(lblNewLabel);
		
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
		
		northSelection.add(spinner);
		
		JButton resultBtn = new JButton(" >>>");
		northSelection.add(resultBtn);
		
		
		JSplitPane split = new JSplitPane();
		panel.add(split, BorderLayout.CENTER);
		
		String[] header = {"Name", "Term count", "%"};
		Object[][] data = new Object[7][detailsColumns];
		detailsDataModel = new DefaultTableModel(data, header);
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(detailsDataModel);
		detail = new JTable(detailsDataModel);
		detail.setRowSorter(sorter);
		
		JScrollPane detailsPane = new JScrollPane(detail);
		split.setLeftComponent(detailsPane);
		
		String[] result_header = {"Rank", "Freq", "Field", "Text"};
		Object[][] result_data = new Object[8][resultColumns];
		resultDataModel = new DefaultTableModel(result_data, result_header);
		results = new JTable(resultDataModel);
		JScrollPane resultPane = new JScrollPane(results);
		results.setFillsViewportHeight(true);
		resultPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		split.setRightComponent(resultPane);
		
		split.validate();
		split.setDividerLocation(0.5);
		
		resultBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Lucene l = Lucene.INSTANCE;
				while (!l.isInitialized) {
					continue;
				}
				int topX = -1;
				try {
					topX = (int) spinner.getValue();
				} catch (Exception ex) {
					return;
				}
				int selected = detail.getSelectedRow();
				int colIndexName = 0;
				for (int i=0; i< detailsDataModel.getColumnCount(); i++) {
					if (detailsDataModel.getColumnName(i).equals("Name"))
							colIndexName = i;
				}
				if (selected == -1)
					return;
				String field = (String) detailsDataModel.getValueAt(detail.convertRowIndexToModel(selected), colIndexName);
				if (field != null) {
					System.out.println("Get top "+ topX +" from field "+field);
					l.printToConsole("Get top "+ topX +" from field "+field);
					TermStats[] result = l.searchTopXOfField(field, topX);
					
					Object[][] resulTable = new Object[result.length][4];
					for (int i= 0; i< result.length; i++) {
						TermStats ts = result[i];
						resulTable[i][0] = i;						// Rank
						resulTable[i][1] = ts.docFreq;				
						resulTable[i][2] = ts.field;
						resulTable[i][3] = ts.termtext.utf8ToString();
					}
					
					setResultTable(resulTable);
					
				}
			}
		});
		
		
		rootContainer.add(panel);
		rootContainer.validate();
 		
		frame.add(rootContainer);
		
		INSTANCE = this;
		isInitialized = true;
	}
	
	public static TopSelectionPart getInstance() {
        return INSTANCE;
	}
	
	public void setDetailTable(Object[][] tableData) {
		for (int i= 0; i< tableData.length; i++) {
			if (i >= detailsDataModel.getRowCount()) {
				detailsDataModel.addRow(new Object[detailsColumns]);
			}
			for (int j=0; j < tableData[0].length; j++) {
				detailsDataModel.setValueAt(tableData[i][j], i, j);
			}
		}
		detailsDataModel.fireTableStructureChanged();
		detail.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	
	public void setResultTable(Object[][] resultTableData) {
		for (int i= 0; i< resultTableData.length; i++) {
			if (i >= resultDataModel.getRowCount()) {
				resultDataModel.addRow(new Object[resultColumns]);
			}
			for (int j=0; j < resultTableData[0].length; j++) {
				resultDataModel.setValueAt(resultTableData[i][j], i, j);
			}
		}
		resultDataModel.fireTableStructureChanged();
		results.setEnabled(false);
		results.doLayout();
	}
	
	
	
	@Focus
	public void onFocus() {
		composite.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
}