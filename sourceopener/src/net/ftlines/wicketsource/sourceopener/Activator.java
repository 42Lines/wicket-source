package net.ftlines.wicketsource.sourceopener;

import net.ftlines.wicketsource.sourceopener.preferences.PreferenceValueService;
import net.ftlines.wicketsource.sourceopener.socket.HttpService;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * Tutorial on plugins here: http://www.vogella.de/articles/EclipsePlugIn/article.html
 */
public class Activator extends AbstractUIPlugin {

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
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
	public static Activator getDefault()
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
}
