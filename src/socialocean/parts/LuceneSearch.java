 
package socialocean.parts;

import java.awt.Checkbox;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import socialocean.handlers.LuceneSearchHandler;
import utils.Lucene;

public class LuceneSearch {
	
	public static final String SEARCH_LUCENE_QUERY_COMMAND_ID = "socialocean.command.lucenesearch";
	
	private Text text;
	private String textText = "Enter query";
	private Text from;
	private String timeFormat = "yyyy-MM-dd hh:mm:ss";
	private Text to;
	
	private Pattern datePattern = Pattern.compile("[1-2][0-9]{3}-[0-1][0-9]-[0-3][0-9]");
	private Pattern timePattern = Pattern.compile("[0-2][0-9]:[0-6][0-9]:[0-9][0-9]");
//	private String luceneIndex = "/Users/michaelhundt/Documents/Meine/Studium/MASTER/MasterProject/data/lucene_index";
	
	@Inject ECommandService commandService;
	@Inject EHandlerService service;
	@Inject EPartService partService;
	@Inject MApplication app;
	
//	@Inject
//	public LuceneSearch(MApplication app) {
//		this.app = app;
//	}
	
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
		
		
//		## BUILD GUI  
		partService.showPart("socialocean.part.console", PartState.CREATE);
		partService.showPart("socialocean.part.timeline", PartState.CREATE);
		partService.showPart("socialocean.part.timeline", PartState.ACTIVATE  );
		partService.showPart("socialocean.part.timeline", PartState.VISIBLE );
		partService.showPart("socialocean.part.settings", PartState.CREATE);
		partService.showPart("socialocean.part.queryhistory", PartState.CREATE);
		partService.showPart("socialocean.part.graph", PartState.CREATE);
		partService.showPart("socialocean.part.jung", PartState.CREATE);
//		MPart part = partService.createPart("socialocean.partdescriptor.sample");
		
		
		Lucene l = Lucene.INSTANCE;
		parent.setLayout(new GridLayout(14, false));
		
		
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
		text.setText(textText);
		text.setToolTipText("The default field is content. Format of query 'Field:Text'. ");
		text.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if (text.getText().isEmpty())
					text.setText(textText);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				text.setText("");
			}
		});
		
		
		Label timeLabel = new Label(parent, SWT.NONE);
		timeLabel.setText("Timerange:");
		
		from = new Text(parent, SWT.BORDER);
		from.setText(timeFormat);
		from.setToolTipText("Format: " + timeFormat);
		from.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if (from.getText().isEmpty())
					from.setText(timeFormat);
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				from.setText("");
			}
		});
		
		
		Label toLabel = new Label(parent, SWT.NONE);
		toLabel.setText(" TO ");
		
		to = new Text(parent, SWT.BORDER);
		to.setText(timeFormat);
		to.setToolTipText("Format: " +timeFormat);
		to.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if (to.getText().isEmpty())
					to.setText(timeFormat);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				to.setText("");
			}
		});
		
		
		Button btnSearch = new Button(parent, SWT.NONE);
//		btnSearch.setFont(newFont);
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String query = text.getText();
				String timeFrom = from.getText();
				String timeTo = to.getText();
				
				boolean hasTimeRange = false;
				if (!timeFrom.isEmpty() || !timeFrom.equals(timeFormat)){
					if (!timeTo.isEmpty() || !timeTo.equals(timeFormat)) {
						String[] fromTime =  timeFrom.split(" ");
						String[] toTime = timeTo.split(" ");
						// TODO check Time - valid?
						if (fromTime.length == 2 && toTime.length == 2) {
							if (Pattern.matches(datePattern.pattern(), fromTime[0]) && Pattern.matches(datePattern.pattern(), toTime[0])) {
								if (Pattern.matches(timePattern.pattern(), fromTime[1]) && Pattern.matches(timePattern.pattern(), toTime[1])) {
									System.out.println("TRUE Timestamps");
									hasTimeRange = true;
								}
							}
							else
								System.out.println("FALSE Timestamps");
						}
						// TODO convert to UNIX long
						// TODO add to query
						
					}
				}
				
				if (hasTimeRange) {
					// TODO add Timerange - if needed
					
					String[] fromTime =  timeFrom.split(" ");
					String[] toTime = timeTo.split(" ");
					
					String[] fromDate = fromTime[0].split("-");
					String[] fromTimetime = fromTime[1].split(":");
					// DATE
					int year = Integer.parseInt(fromDate[0]);
					int month = Integer.parseInt(fromDate[1]);
					int day = Integer.parseInt(fromDate[2]);
					// TIME
					int hour = Integer.parseInt(fromTimetime[0]);
					int min = Integer.parseInt(fromTimetime[1]);
					int sec = Integer.parseInt(fromTimetime[2]);
					
					LocalDate date = LocalDate.of(year, month, day);
					LocalTime time = LocalTime.of(hour, min, sec);
					
					LocalDateTime dt = LocalDateTime.of(date, time);
					long utc_from = dt.toEpochSecond(ZoneOffset.UTC);
					
					String[] toDate = toTime[0].split("-");
					String[] toTimetime = toTime[1].split(":");
					// DATE
					year = Integer.parseInt(toDate[0]);
					month = Integer.parseInt(toDate[1]);
					day = Integer.parseInt(toDate[2]);
					// TIME
					hour = Integer.parseInt(toTimetime[0]);
					min = Integer.parseInt(toTimetime[1]);
					sec = Integer.parseInt(toTimetime[2]);
					
					date = LocalDate.of(year, month, day);
					time = LocalTime.of(hour, min, sec);
					
					dt = LocalDateTime.of(date, time);
					long utc_to = dt.toEpochSecond(ZoneOffset.UTC);
					
					if (utc_to > utc_from) {
						System.out.println("Timerange: "+timeFrom+" TO "+timeTo);
						// ADD to query String
						if (query.isEmpty() || query.equals(textText))
							query = "date:["+utc_from+" TO "+utc_to+"]";
						else {
							query = query + " AND date:["+utc_from+" TO "+utc_to+"]";
						}
					}
					else {
						Lucene l = Lucene.INSTANCE;
						l.printlnToConsole("TO-Timespamp "+timeTo+ " is not bigger than FFROM-Timestamp"+timeFrom);
						System.out.println("TO-Timespamp "+timeTo+ " is not bigger than FFROM-Timestamp"+timeFrom);
					}
				}
				
				// no timerange and no query
				else {
					if (query.isEmpty() || query.equals(textText)) {
						return;
					}
				}
				
				@SuppressWarnings("restriction")
				final Command command = commandService.getCommand( SEARCH_LUCENE_QUERY_COMMAND_ID );
                if( !command.isDefined() )
                    return;
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("QueryString", query);
                parameters.put("indexpath", l.getLucenIndexPath());
                
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
                
                text.setText(textText);
                from.setText(timeFormat);
                to.setText(timeFormat);
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
				
				if (l.getLastResult() == null)
					return;
				
				InputDialog input = new InputDialog(parent.getShell(), "", "Enter Folder Name", "", null);
				input.open();
				
				String name = input.getValue();
				input.close();
				
				if (name.equals("null") || name.isEmpty() || name == null)
					return;
				
				// TODO Graph
				// Clear all Results, Map, Graph
				System.out.println("Re-Index:");
				l.printlnToConsole("Re-Index:");
				
//				ProgressBar progress = new ProgressBar(parent.getShell(), SWT.SMOOTH);
//				progress.setVisible(true);
//				progress.setEnabled(true);
				
//				Progress progress = new Progress("Pro");
//				progress.fill(parent);
				
				l.reindexLastResult(name, true, true, null);
				l.clearMap();
				l.clearGraph();	
			}
		});
		
		Button export = new Button(parent, SWT.BUTTON1);
//		btnClear.setFont(newFont);
		export.setText("Export");
		export.setBackground(grey);
		
		export.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				
				if (l.getLastResult() == null)
					return;
				
				InputDialog input = new InputDialog(parent.getShell(), "", "Enter a name", "", null);
//				CustomInputDialog input = new CustomInputDialog(parent.getShell());
				input.open();
//				CustomInputDialog.open();
				String name = input.getValue();
				input.close();
				
				if (name.equals("null") || name.isEmpty() || name == null)
					return;
				
				System.out.println("Export to JSON");
//				l.printlnToConsole("Export to JSON");
				
				String luceneIndex = l.getLucenIndexPath();
				// in temp folder? --> temp to export
				String tempPath = luceneIndex.substring(0, luceneIndex.lastIndexOf("/")+1);
				if (tempPath.endsWith("temp/")) {
					tempPath = tempPath.replace("temp/", "export/");
					File theDir = new File(tempPath);

					// if the directory does not exist, create it
					if (!theDir.exists()) {
					    System.out.println("\tcreating directory: " + theDir.getName());
					    boolean result = false;

					    try{
					        theDir.mkdir();
					        result = true;
					    } 
					    catch(SecurityException se){
					        //handle it
					    }        
					    if(result) {    
					        System.out.println("\tDIR created");  
					    }
					}
				}
				
				// nor in temp folder, not in export folder --> create export
				if (!tempPath.endsWith("export/")) {
					tempPath +="export/";
					File theDir = new File(tempPath);

					// if the directory does not exist, create it
					if (!theDir.exists()) {
					    System.out.println("\tcreating directory: " + theDir.getName());
					    boolean result = false;

					    try{
					        theDir.mkdir();
					        result = true;
					    } 
					    catch(SecurityException se){
					        //handle it
					    }        
					    if(result) {    
					        System.out.println("\tDIR created");  
					    }
					}
				}
				
				
				File newIndex = new File(tempPath+name);
				if (!newIndex.exists()) {
				    System.out.println("\tcreating directory: " + newIndex.getName());
				    boolean result = false;

				    try{
				    	newIndex.mkdir();
				        result = true;
				    } 
				    catch(SecurityException se){
				        //handle it
				    }        
				    if(result) {    
				        System.out.println("\tDIR created");  
				    }
				}
				
				System.out.println("\tcreated directory '" + newIndex + "' ... DONE");
				l.printlnToConsole("\tcreated directory '"+newIndex+"' ... DONE");
				
				MWindow window = app.getChildren().get(0);
				Rectangle appBounds = new Rectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight());
				l.takeScreenshot(name, appBounds, newIndex.getAbsolutePath());
				
				l.reindexLastResult(name, false, false, newIndex);
				l.exporttoJSON(newIndex, name);
				
//				l.createGraphML_Mention(l.getLastResult().getData(), true, name, newIndex);
				
			}
		});
		
		export.setToolTipText("Export to JSON");
		
		
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
				l.printlnToConsole("CLEAR");
				l.clearQueryHistroy();
				l.clearMap();
				l.showCatHisto();
				l.resetTimeLine();
				l.clearGraph();							//TODO
			}
		});
		
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
		Lucene l = Lucene.INSTANCE;
		String luceneIndex = l.getLucenIndexPath();
		
		if (luceneIndex == null)
			return;
		
		String tempPath = luceneIndex.substring(0, luceneIndex.lastIndexOf("/")+1);
		
		System.out.println("Clean up the temp folder ...");
		// is already in temp folder .. if not check if we are now in the root folder
		if (!tempPath.endsWith("temp/")) {
			tempPath += "temp/";
		}
		File newIndex = new File(tempPath);
		
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
//		text.setFocus();
	}
	
	
	@Persist
	public void save() {
		
	}
	
//	private class CustomInputDialog extends Dialog {
//
//		private Shell dialog;
//		
//		public CustomInputDialog(Shell parent) {
//			super(parent);
//		}
//		
//		public void close() {
//			dialog.close();
//		}
//
//		public String open() {
//			Shell parent = getParent();
//			dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
//			dialog.setSize(100, 100);
//			dialog.setText("Java Source and Support");
//			dialog.open();
//			Display display = parent.getDisplay();
//			while (!dialog.isDisposed()) {
//				if (!display.readAndDispatch())
//					display.sleep();
//			}
//			return "After Dialog";
//		}
//		
//	}
	
}