package net.simsa.sourceopener.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.simsa.sourceopener.IOpenEventListener;
import net.simsa.sourceopener.OpenEvent;

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
	private static int PORT = 9123;

	private List<IOpenEventListener> listeners;
	SourceOpenerHttpd currentHttpd;

	public HttpService() {
		listeners = new ArrayList<IOpenEventListener>();
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

	/**
	 * Threaded listener notification.
	 */
	@Override
	public void onOpenEvent(OpenEvent event)
	{
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
		currentHttpd = new SourceOpenerHttpd(PORT, this);
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
