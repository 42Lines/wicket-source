package net.simsa.sourceopener.socket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.simsa.sourceopener.IOpenEventListener;
import net.simsa.sourceopener.OpenEvent;

/**
 * Listens for requests to open a file in an eclipse editor, fires the open
 * event (in a separate thread), and responds to the http request with a 200 OK
 * and a very basic response message. Ignores all requests other than file-opens
 * of the appropriate type.
 * 
 * @author Jenny Brown
 * 
 */
public class SourceOpenerHttpd extends NanoHTTPD {

	private List<IOpenEventListener> listeners = new ArrayList<IOpenEventListener>();

	private SourceOpenerHttpd(int port, File wwwroot) throws IOException {
		super(port, wwwroot);
	}

	private SourceOpenerHttpd(int port) throws IOException {
		super(port);
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

	@Override
	public Response serve(String uri, String method, Properties header, Properties params, Properties files)
	{
		try {
			OpenEvent event = new OpenEvent(uri, params);
			new Thread(new OpenEventNotifier(event)).start();
			System.out.println("Responding with 200 OK for file " + event.getFileName());
			return new Response(HTTP_OK, MIME_HTML, "<html><body>OK</body></html>");
		} catch (IllegalArgumentException ie) {
			System.out.println("Responding with Bad Request.");
			return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Bad Request: Invalid uri.");
		}
	}

	@Override
	public void stop()
	{
		super.stop();
		listeners.clear();
	}

	@Override
	public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing)
	{
		return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: No file serving.");
	}

	/**
	 * Overrides the parent class since I want more specific handling for
	 * testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		int port = 9123;
		File wwwroot = new File("/jail");
		try {
			new SourceOpenerHttpd(port, wwwroot);
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
			System.exit(-1);
		}

		System.out.println("Now serving files in port " + port + " from \"" + wwwroot + "\"");
		System.out.println("Hit Enter to stop, or terminate the jvm manually.\n");

		try {
			System.in.read();
		} catch (Throwable t) {
		}

	}

}
