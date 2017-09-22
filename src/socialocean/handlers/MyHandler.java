package socialocean.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class MyHandler {

	@Execute
	public int printHello(Shell shell) {
		MessageDialog.openInformation(shell, "", "Hello World");
		return 1;
	}
}
