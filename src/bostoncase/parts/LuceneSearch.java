 
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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import bostoncase.handlers.LuceneSearchHandler;
import impl.LuceneQuerySearcher;
import utils.IndexInfo;
import utils.Lucene;


public class LuceneSearch {
	
	public static final String SEARCH_LUCENE_QUERY_COMMAND_ID = "bostoncase.command.lucenesearch";
	
	private Text text;
	private String luceneIndex = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index";
	
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
		
		Properties prop = new Properties();
		InputStream input = null;

		try {
//			input = LuceneSearchHandler.class.getResourceAsStream("config.properties");
			// load a properties file
//			Path currentRelativePath = Paths.get("");
//			String s = currentRelativePath.toAbsolutePath().toString();
//			System.out.println("Current relative path is: " + s);
			
//			## LOAD Icons
			ImageDescriptor st = AbstractUIPlugin.imageDescriptorFromPlugin("BostonCase", "icons/open.png");
			Image img = st.createImage();
			
//			## LOAD Settings File
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
//			// get the property value and print it out
//			System.out.println(prop.getProperty("lucene_index"));
			luceneIndex = prop.getProperty("lucene_index");
			
			LuceneQuerySearcher lqs = LuceneQuerySearcher.INSTANCE;
			Lucene l = Lucene.INSTANCE;
			if (!l.isInitialized) {
				System.out.println("Loading Lucene Index ...");
				l.initLucene(luceneIndex, lqs);
			}

	
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
		
//		Composite composite = new Composite(tabFolder, SWT.NONE);
//		tbtmLuceneSearch.setControl(composite);
		parent.setLayout(new GridLayout(4, false));
		
		Button btnAdd = new Button(parent, SWT.RADIO);
		btnAdd.setSelection(true);
		btnAdd.setText("ADD");
		
		Button btnFuse = new Button(parent, SWT.RADIO);
		btnFuse.setText("FUSE");
		
		text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setMessage("Enter query");
		
		Button btnSearch = new Button(parent, SWT.NONE);
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
                
                String type = "ADD";
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
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
		text.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
}