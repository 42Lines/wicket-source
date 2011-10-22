package net.simsa.sourceopener.views;

/**
 * Indicates trouble opening the requested file in an editor and going to the specified line.
 * @author Jenny Brown
 *
 */
public class OpenFileException extends Exception {
	private Reason reason;
	
	public static enum Reason { 
		FILE_NOT_FOUND, FILE_OK_BUT_LINE_NOT_FOUND, TOO_MANY_MATCHES, EXCEPTION, MESSAGE_ALREADY_SET
	}

	public OpenFileException(Reason reason) {
		super();
		this.reason = reason;
	}

	public OpenFileException(Reason reason, Throwable cause) {
		super(cause);
	}

	public Reason getReason()
	{
		return reason;
	}

	public void setReason(Reason reason)
	{
		this.reason = reason;
	}

}
