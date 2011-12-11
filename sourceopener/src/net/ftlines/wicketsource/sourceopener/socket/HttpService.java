package net.ftlines.wicketsource.sourceopener.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.ftlines.wicketsource.sourceopener.IOpenEventListener;
import net.ftlines.wicketsource.sourceopener.OpenEvent;
import net.ftlines.wicketsource.sourceopener.RecentEventsCache;
import net.ftlines.wicketsource.sourceopener.preferences.PreferenceConstants;
import net.ftlines.wicketsource.sourceopener.preferences.PreferenceValueService;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Coordinates web server start/stop and event notifications from it serving
 * requests. This is the bridge between low-level server behaviors and the rest
 * of the GUI, and ensures we don't lose the list of event listeners between
 * server restarts.
 * 
 * @author Jenny Brown
 * 
 */
public class HttpService implements IOpenEventListener, IPropertyChangeListener {
	private Logger log = null;
	private List<IOpenEventListener> listeners;
	private SourceOpenerHttpd currentHttpd;
	private RecentEventsCache eventCache;

	public HttpService() {
		listeners = new ArrayList<IOpenEventListener>();
		eventCache = new RecentEventsCache();
		log = Logger.getLogger("HttpService");
	}

	@Override
	public void propertyChange(PropertyChangeEvent pcEvent)
	{
		if (PreferenceConstants.P_PASSWORD.equals(pcEvent.getProperty()) ||
			PreferenceConstants.P_USEPASSWORD.equals(pcEvent.getProperty()) ||
			PreferenceConstants.P_PORT.equals(pcEvent.getProperty())		) {
			reloadConfiguration();
		}
	}
	
	public void registerListener(IOpenEventListener listener)
	{
		this.listeners.add(listener);
	}

	public void removeListener(IOpenEventListener listener)
	{
		this.listeners.remove(listener);
	}

	class OpenEventNotifier implements Runnable {
		private OpenEvent event;

		public OpenEventNotifier(OpenEvent event) {
			this.event = event;
		}

		@Override
		public void run()
		{
			for (IOpenEventListener listener : listeners) {
				listener.onOpenEvent(event);
			}
		}
	}

	public RecentEventsCache getEventCache()
	{
		return eventCache;
	}

	/**
	 * Threaded listener notification.
	 */
	@Override
	public void onOpenEvent(OpenEvent event)
	{
		eventCache.add(event);
		new Thread(new OpenEventNotifier(event)).start();
	}

	public boolean isRunning()
	{
		return currentHttpd != null;
	}
	
	/**
	 * Starts the web server
	 * 
	 * @throws IOException
	 *             If the port is already in use.
	 */
	public void start() throws IOException
	{
		int port = PreferenceValueService.getPort();
		if (port == 0) {
			log.info("No port configured!!");
			throw new IOException("No port configured for service!");
		}
		log.info("Starting listener on port " + port + " with requirePassword = " + PreferenceValueService.isUsePassword());
		currentHttpd = new SourceOpenerHttpd(port, PreferenceValueService.isUsePassword(), PreferenceValueService.getPassword(), this);
	}

	/**
	 * Stops the web server
	 */
	public void stop()
	{
		log.info("Stopping manually.");		
		if (currentHttpd != null) {
			currentHttpd.stop();
		}
		currentHttpd = null;
	}

	public void reloadConfiguration()
	{
		// only restart it if it was already running.
		if (currentHttpd != null) { 
			log.info("Reloading configuration... ");		
			stop();
			try {
				start();
			} catch (IOException io) {
				log.warning("Couldn't restart listener: " + io.toString());
			}
		}
	}
	
}
