 
package bostoncase.parts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.validation.Validator;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import bostoncase.handlers.LuceneSearchHandler;
import impl.GetMinMaxDateThread;
import impl.LuceneIndexLoaderThread;
import impl.LuceneQuerySearcher;
import utils.Lucene;

public class LuceneSearch {
	
	public static final String SEARCH_LUCENE_QUERY_COMMAND_ID = "bostoncase.command.lucenesearch";
	
	private Text text;
//	private String luceneIndex = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index";
	private String luceneIndex = "";
	
//	private Composite parent = null;

	
	@Inject ECommandService commandService;
	@Inject EHandlerService service;
	
	@Inject
	public LuceneSearch() {
		
	}
	
	public String readTextFile(URL url) throws IOException {
	    StringBuilder output = new StringBuilder();
	    String lineSeperator = System.lineSeparator();

	    try ( InputStream in = url.openConnection().getInputStream();
	          BufferedReader br = new BufferedReader(new InputStreamReader(in)) ) {

	        String inputLine;

	        while ((inputLine = br.readLine()) != null ) {
	            output.append(inputLine).append(lineSeperator);
	        }

	      if (output.length()>0) {
	        // remove last line separator
	        output.setLength(output.length() - lineSeperator.length());
	      }
	     }

	    return output.toString();
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
//		this.parent = parent;
		Display display = Display.getCurrent();
		Color red = display.getSystemColor(SWT.COLOR_RED);
		Color grey = display.getSystemColor(SWT.COLOR_GRAY);
		Color green = display.getSystemColor(SWT.COLOR_DARK_GREEN);
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
//		Font standard = display.getSystemFont();
//		FontData[] fd = standard.getFontData();
//		Font newFont = new Font(display, fd[0].getName(), 13, fd[0].getStyle());
		
		InputStream input = null;
		try {
//			## LOAD Icons
//			ImageDescriptor st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/open.png");
//			Image img = st.createImage();
			
//			## LOAD Settings File
			Properties prop = new Properties();
			URL url = null;
			try {
			  url = new URL("platform:/plugin/"
			    + "BostonCase/"
			    + "settings/config.properties");

			    } catch (MalformedURLException e1) {
			      e1.printStackTrace();
			}
			url = FileLocator.toFileURL(url);
			input = new FileInputStream(new File(url.getPath()));
			prop.load(input);
//			System.out.println(prop.getProperty("lucene_index"));
			luceneIndex = prop.getProperty("lucene_index");
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
//		## Initialize LUCENE model class
		LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
		Lucene l = Lucene.INSTANCE;
		if (!l.isInitialized && !luceneIndex.isEmpty()) {
			LuceneIndexLoaderThread lilt = new LuceneIndexLoaderThread(l) {
				@Override
				public void execute() {
					System.out.println("Loading Lucene Index ...");
					l.initLucene(luceneIndex, lqs);
				}
			};
			lilt.start();
			
			GetMinMaxDateThread gmdt = new GetMinMaxDateThread(l) {
				
				@Override
				public void execute() {
					System.out.println("Get MinMax Date ...");
					l.initMinDate();
					l.initMaxDate();
				}
			};
			gmdt.start();
			
			
		} else {
			System.out.println(" Could not load the index at path: '"+luceneIndex+"'");
		}
		
		
//		## BUILD GUI
		
		parent.setLayout(new GridLayout(9, false));
		
		
		Button btnAdd = new Button(parent, SWT.CHECK );
		btnAdd.setSelection(false);
		btnAdd.setText("ADD");
		btnAdd.setForeground(blue);
//		btnAdd.setFont(newFont);
//		btnAdd.setForeground(new Color (device, l.getColor().getRed(), l.getColor().getGreen(), l.getColor().getBlue()));
		
		Button btnFuse = new Button(parent, SWT.CHECK );
		btnFuse.setText("FUSE");
		btnFuse.setForeground(green);
//		btnFuse.setFont(newFont);
//		btnFuse.setForeground(new Color (device, l.getColor().getRed(), l.getColor().getGreen(), l.getColor().getBlue()));
		
		btnAdd.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// if Fuse is selected -> de select
				if (btnFuse.getSelection() && btnAdd.getSelection()) {
					btnFuse.setSelection(false);
				}
				if (btnAdd.getSelection())
					l.setQeryType(btnAdd.getText());
				else 
					l.setQeryType("");
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		btnFuse.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// if ADD is selected -> de select
				if (btnAdd.getSelection() && btnFuse.getSelection()) {
					btnAdd.setSelection(false);
				}
				
				if (btnFuse.getSelection())
					l.setQeryType(btnFuse.getText());
				else 
					l.setQeryType("");
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
//		text.setFont(newFont);
		text.setText("Enter query");
		
		Button btnSearch = new Button(parent, SWT.NONE);
//		btnSearch.setFont(newFont);
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String query = text.getText();
				if (query.isEmpty()) {
					return;
				}
				
				@SuppressWarnings("restriction")
				final Command command = commandService.getCommand( SEARCH_LUCENE_QUERY_COMMAND_ID );
                if( !command.isDefined() )
                    return;
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("QueryString", query);
                parameters.put("indexpath", luceneIndex);
                
                String type = "";
                if (btnAdd.getSelection())
                	type = "ADD";
                if (btnFuse.getSelection())
                	type = "FUSE";
                parameters.put("type", type);
                
                final ParameterizedCommand pcmd = ParameterizedCommand.generateCommand(command, parameters);
                
//                ParameterizedCommand myCommand = commandService.createCommand(command, parameters);
                service.activateHandler(SEARCH_LUCENE_QUERY_COMMAND_ID, new LuceneSearchHandler());
                if( !service.canExecute( pcmd ))
                    return;
                service.executeHandler( pcmd );
                
                text.setText("");
			}
		});
		btnSearch.setText("Search");
		btnSearch.setBackground(red);
		btnSearch.setForeground(red);
		
		Button btnBack = new Button(parent, SWT.BUTTON1);
//		btnBack.setFont(newFont);
		btnBack.setText("Back");
		btnBack.setBackground(grey);
		
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				
				// TODO Graph
				// Clear all Results, Map, Graph
				l.showLastResult();
			}
		});
		
		
		Button reindex = new Button(parent, SWT.BUTTON1);
//		btnClear.setFont(newFont);
		reindex.setText("Re-Index");
		reindex.setBackground(grey);
		
		reindex.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				
				InputDialog input = new InputDialog(parent.getShell(), "", "Enter Folder Name", "", null);
				input.open();
				
				String name = input.getValue();
				
				// TODO Graph
				// Clear all Results, Map, Graph
				System.out.println("Re-Index");
				l.printToConsole("Re-Index");
				l.reindexLastResult(name);
			}
		});
		
		
		Button btnClear = new Button(parent, SWT.BUTTON1);
//		btnClear.setFont(newFont);
		btnClear.setText("Clear");
		btnClear.setBackground(grey);
		
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				
				// TODO Graph
				// Clear all Results, Map, Graph
				System.out.println("CLEAR");
				l.printToConsole("CLEAR");
				l.clearQueryHistroy();
				l.clearMap();
				l.showCatHisto();
				l.resetTimeLine();
//				l.clearGraph();							//TODO
			}
		});
		
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
		String tempPath = luceneIndex.substring(0, luceneIndex.lastIndexOf("/")+1);
		
		System.out.println("Clean up the temp folder ...");
		File newIndex = new File(tempPath+"/temp");
		
		if (newIndex.exists() && newIndex.isDirectory()) {
			// remove all files in dir
			String[]entries = newIndex.list();
			for(String s: entries){
			    File currentIndexFolder = new File(newIndex.getPath(), s);
			    
			    if (currentIndexFolder.isDirectory()) {
			    	String[] indexFiles = currentIndexFolder.list();
					for(String index: indexFiles){
					    File files = new File(currentIndexFolder.getPath(), index);
					    files.delete();
					}
			    }
			    
			    currentIndexFolder.delete();
			}
			newIndex.delete();
			System.out.println("DIR deleted");  
		}
		
	}
	
	
	@Focus
	public void onFocus() {
		text.setFocus();
		text.setText("");
	}
	
	
	@Persist
	public void save() {
		
	}
	
}