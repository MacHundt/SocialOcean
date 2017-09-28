package socialocean.parts;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class Progress extends ContributionItem {

	public static org.eclipse.swt.widgets.ProgressBar progressBar;
	
	public Progress(final String id) {
        super(id);
    }

    @Override
    public void fill(final Composite parent) {
        if (progressBar == null) {
            progressBar = new ProgressBar(parent, SWT.SMOOTH);
            progressBar.setBounds (300, 170, 200, 32);
        }
        progressBar.setVisible(false);
    }

    public void makeProgress(final int loaded_data, final int total_data) {
//        logger.log("progressitem do progress", new String [] {""});
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                Progress.progressBar.setVisible(true);
                Progress.progressBar.setMaximum(total_data);
                Progress.progressBar.setSelection(loaded_data);
            }});
        if (loaded_data == total_data) {
            //When all of the data are loaded, make progress bar disappear
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Progress.progressBar.setVisible(false);
                }});
        }
    }
}
