package net.simsa.sourceopener;

import net.simsa.sourceopener.preferences.PreferenceConstants;
import net.simsa.sourceopener.preferences.SecurePreferenceStore;
import net.simsa.sourceopener.socket.HttpService;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * Tutorial on plugins here: http://www.vogella.de/articles/EclipsePlugIn/article.html
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.simsa.sourceopener"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private HttpService httpService;
	private SecurePreferenceStore securePreferenceStore;

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
		plugin = null;
		super.stop(context);
		httpService.stop();
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
	 * For storing passwords and such. 
	 * @return
	 */
    public SecurePreferenceStore getSecurePreferenceStore() {
        if (securePreferenceStore == null) {
                ISecurePreferences root = SecurePreferencesFactory.getDefault();
                ISecurePreferences node = root.node(PLUGIN_ID);
                securePreferenceStore = new SecurePreferenceStore(node);
                securePreferenceStore.setDoEncryptPreference(PreferenceConstants.P_PASSWORD);
        }
        return securePreferenceStore; 
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
