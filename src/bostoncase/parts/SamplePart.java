package bostoncase.parts;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import bostoncase.widget.MapWidget;

public class SamplePart {

	private Text txtInput;
	private TableViewer tableViewer;
	private MapWidget mapvis;
	private Tree layerTree;

	@Inject
	private MDirtyable dirty;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		txtInput = new Text(parent, SWT.BORDER);
		txtInput.setMessage("Enter text to mark part as dirty");
		txtInput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dirty.setDirty(true);
			}
		});
		txtInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
//		sashForm.setLayoutData(BorderLayout.CENTER);
//		
//		SashForm middle = new SashForm(sashForm, SWT.HORIZONTAL);
//		
//		Group grpLayers = new Group(middle, SWT.NONE);
//		grpLayers.setText("Layers");
//		grpLayers.setLayout(new FillLayout(SWT.HORIZONTAL));
//		
//		layerTree = new Tree(grpLayers, SWT.BORDER);
//		
//		TreeItem trtmBaseMap = new TreeItem(layerTree, SWT.NONE);
//		trtmBaseMap.setText("BaseMap");
//		
//		TreeItem trtmMapnic = new TreeItem(trtmBaseMap, SWT.NONE);
//		trtmMapnic.setText("OSM-Mapnic");
//		trtmBaseMap.setExpanded(true);
//		
//		TreeItem trtmTweets = new TreeItem(layerTree, SWT.NONE);
//		trtmTweets.setText("Tweets");
//		
//		
//		mapvis = new MapWidget(middle, SWT.NONE);
//		
//		Group grpSettings = new Group(middle, SWT.NONE);
//		grpSettings.setText("Settings");
//		middle.setWeights(new int[] {109, 258, 77});
		
		
		tableViewer = new TableViewer(parent);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());;
		tableViewer.setInput(createInitialDataModel());
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		mapvis = new MapWidget(parent, SWT.CENTER);
		mapvis.setLayoutData(new GridData(GridData.FILL_BOTH));
		
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}
	
	private List<String> createInitialDataModel() {
		return Arrays.asList("Sample item 1", "Sample item 2", "Sample item 3", "Sample item 4", "Sample item 5", "Sample item 6");
	}
}