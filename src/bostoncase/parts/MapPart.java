package bostoncase.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import bostoncase.widget.MapWidget;

public class MapPart {
	
	private MapWidget mapvis;
	private Tree layerTree;
	
	@Inject
	public MapPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
//		parent.setLayout(new GridLayout(1, false));
//
//		mapvis = new MapWidget(parent, SWT.NONE);
//		mapvis.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		SashForm middle = new SashForm(parent, SWT.HORIZONTAL);
		
		Group grpLayers = new Group(middle, SWT.NONE);
		grpLayers.setText("Layers");
		grpLayers.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		layerTree = new Tree(grpLayers, SWT.BORDER);
		
		TreeItem trtmBaseMap = new TreeItem(layerTree, SWT.NONE);
		trtmBaseMap.setText("BaseMap");
		
		TreeItem trtmMapnic = new TreeItem(trtmBaseMap, SWT.NONE);
		trtmMapnic.setText("OSM-Mapnic");
		trtmBaseMap.setExpanded(true);
		
		TreeItem trtmTweets = new TreeItem(layerTree, SWT.NONE);
		trtmTweets.setText("Tweets");
		
		
		mapvis = new MapWidget(middle, SWT.NONE);
		
		Group grpSettings = new Group(middle, SWT.NONE);
		grpSettings.setText("Settings");
		middle.setWeights(new int[] {109, 258, 77});
	}
	
	
	
	@Focus
	public void onFocus() {
		mapvis.setFocus();
	}
	
	
}