package net.simsa.sourceopener.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.simsa.sourceopener.IOpenEventListener;
import net.simsa.sourceopener.OpenEvent;
import net.simsa.sourceopener.RecentEventsCache;
import net.simsa.sourceopener.views.ConfigurationService;

/**
 * Coordinates web server start/stop and event notifications from it serving
 * requests. This is the bridge between low-level server behaviors and the rest
 * of the GUI, and ensures we don't lose the list of event listeners between
 * server restarts.
 * 
 * @author Jenny Brown
 * 
 */
public class HttpService implements IOpenEventListener {
	Logger log = Logger.getLogger("HttpService");
	
	private List<IOpenEventListener> listeners;
	SourceOpenerHttpd currentHttpd;
	RecentEventsCache eventCache;

	public HttpService() {
		listeners = new ArrayList<IOpenEventListener>();
		eventCache = new RecentEventsCache();
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

	/**
	 * Starts the web server
	 * 
	 * @throws IOException
	 *             If the port is already in use.
	 */
	public void start() throws IOException
	{
		int port = ConfigurationService.getPort();
		if (port == 0) { 
			throw new IOException("No port configured for service!");
		}
		log.info("Starting listener on port " + port);
		currentHttpd = new SourceOpenerHttpd(port, this);
	}

	/**
	 * Stops the web server
	 */
	public void stop()
	{
		if (currentHttpd != null) {
			currentHttpd.stop();
		}
		currentHttpd = null;
	}

}
