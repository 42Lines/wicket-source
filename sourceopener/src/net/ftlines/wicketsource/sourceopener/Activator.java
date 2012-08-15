package net.ftlines.wicketsource.sourceopener;

import java.io.IOException;

import net.ftlines.wicketsource.sourceopener.preferences.PreferenceValueService;
import net.ftlines.wicketsource.sourceopener.socket.HttpService;
import net.ftlines.wicketsource.sourceopener.views.OpenFileException;
import net.ftlines.wicketsource.sourceopener.views.UIEditorFileOpener;

import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * Tutorial on plugins here: http://www.vogella.de/articles/EclipsePlugIn/article.html
 */
public class Activator extends AbstractUIPlugin implements IStartup, IOpenEventListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.ftlines.wicketsource.sourceopener"; //$NON-NLS-1$
	// The shared instance
	private static Activator plugin;
	// Data model entry point for this plugin.
	private HttpService httpService;

	/**
	 * The constructor
	 */
	public Activator() {
		httpService = new HttpService();
		httpService.registerListener(this);
	}
	
	@Override
	public void earlyStartup()
	{
		if (PreferenceValueService.isStartListenerOnStartup()) {
			// don't have to do anything, just be registered.
		}
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		startServer();
	}

	public void startServer() throws IOException
	{
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(httpService);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(httpService.getEventCache());
		if (PreferenceValueService.isStartListenerOnStartup()) {
			httpService.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception
	{
		httpService.stop();
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(httpService);
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(httpService.getEventCache());
		plugin = null;
		super.stop(context);
	}

	/**
	 * Reference to the (only) http service for this plugin; it manages
	 * starting and stopping the web server.
	 * 
	 * @return
	 */
	public HttpService getHttpService()
	{
		return httpService;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static synchronized Activator getDefault()
	{
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	

	/**
	 * Open the specified file in an editor and go to the line number. Then
	 * notify the eventTableViewer that the data in the model has changed and it
	 * should update. This must occur in the UI thread hence the use of
	 * syncExec.
	 * 
	 * If more than one file matches during search, a UI dialog will appear
	 * asking the user to choose one of the matches. If they cancel, the file
	 * will not open and the event will received an error message saying that
	 * the user cancelled open. If they choose a file and press Ok, the file
	 * should open in an editor.
	 * 
	 */
	@Override
	public void onOpenEvent(final OpenEvent event)
	{
		event.reset();

		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				Activator.identifyFileAndDisplay(event);
			}
		});		
	}
	
	

	private static SearchMatch[] searchForFile(OpenEvent event) throws OpenFileException
	{
		PackageFileSearchRequester searchFacade = new PackageFileSearchRequester(event.getPackageName(), event.getFileName().replace(".java", ""));
		searchFacade.searchAndWait();
		return searchFacade.allMatches();
	}

	/**
	 * This expects to be run from the Display thread.
	 * 
	 * @param event
	 */
	public static void identifyFileAndDisplay(final OpenEvent event)
	{
		try {
			// Look for the file requested by open event.
			SearchMatch[] matches = Activator.searchForFile(event);
			if (matches.length == 0) {
				event.setResultOfOpen(new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND));
				return;
			}
			if (matches.length == 1) {
				event.setFile(matches[0]);
				new UIEditorFileOpener(event).run();
				return;
			}
	
			// Multiple choices, so display a dialog and have the user pick one.
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					new LabelProvider());
			dialog.setTitle("File Selection - SourceOpener");
			dialog.setMessage("Multiple matches. Choose One (* = any string, ? = any char):");
			dialog.setElements(matches);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] choices = dialog.getResult();
	
			if ((dialog.getReturnCode() == Window.CANCEL) || (choices.length < 1)) {
				event.setResultOfOpen("Multiple matches - user cancelled selection");
				event.setFile(null);
				return;
			}
			event.setFile((SearchMatch) choices[0]);
			event.setResultOfOpenOk();
			new UIEditorFileOpener(event).run();
			
		} catch (OpenFileException ofe) {
			event.setResultOfOpen(ofe);
		}
	}	
}
