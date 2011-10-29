package net.simsa.sourceopener;

import java.util.Properties;

import net.simsa.sourceopener.views.OpenFileException;

import org.eclipse.core.runtime.IPath;

/**
 * Represents a request to open a file and jump to a specific location in the
 * source.
 * 
 * @author Jenny Brown
 * 
 */
public class OpenEvent {

	private OpenFileException exception;
	private String resultOfOpen;
	private String projectName;
	private String packageName;
	private String fileName;
	private int lineNumber;
	private IPath file;

	public String toString()
	{
		return packageName + " " + fileName + ":" + lineNumber + " " + getResultOfOpen();
	}

	public OpenEvent(String packageName, String fileName, int lineNumber) {
		this.packageName = packageName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.resultOfOpen = "";
	}

	public OpenEvent(String src) {
		fromSrcLine(src);
	}

	private void fromSrcLine(String src)
	{
		String[] pieces = src.split(":");
		packageName = pieces[0];
		fileName = pieces[1];
		lineNumber = Integer.parseInt(pieces[2]);
		this.resultOfOpen = "";
	}

	/**
	 * Throws an exception if the url being requested isn't compatible with the
	 * kinds of requests we can serve.
	 * 
	 * @param uri
	 * @param params
	 * @throws IllegalArgumentException
	 */
	public OpenEvent(String uri, Properties params) throws IllegalArgumentException {
		if (!"/open".equals(uri)) {
			throw new IllegalArgumentException();
		}
		if ((params.getProperty("src") == null) || ("".equals(params.getProperty("src").trim()))) {
			throw new IllegalArgumentException();
		}
		String src = params.getProperty("src");
		if (src.indexOf(":") == -1) {
			throw new IllegalArgumentException();
		}

		fromSrcLine(src);
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public String getFileName()
	{
		return fileName;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public String getResultOfOpen()
	{
		if (resultOfOpen == null) { return ""; }
		return resultOfOpen;
	}

	public IPath getFile()
	{
		return file;
	}

	public void setFile(IPath file)
	{
		this.file = file;
	}

	public OpenFileException getException()
	{
		return exception;
	}

	public void setException(OpenFileException exception)
	{
		this.exception = exception;
		if (exception != null) exception.printStackTrace();
	}

	public void setResultOfOpen(String resultOfOpen)
	{
		this.resultOfOpen = resultOfOpen;
	}
	
	public void setResultOfOpenOk() 
	{
		this.resultOfOpen = "OK";
	}
	
	public void reset()
	{
		setResultOfOpenOk();
		exception = null;
	}
	
	public void setResultOfOpen(OpenFileException ofe)
	{
		setException(ofe);
		switch (ofe.getReason()) {
			case FILE_NOT_FOUND :
				this.resultOfOpen = "File could not be found in workspace.";
				return;
			case FILE_OK_BUT_LINE_NOT_FOUND :
				this.resultOfOpen = "File located but line number invalid.";
				return;
			case TOO_MANY_MATCHES :
				this.resultOfOpen = "Too many files matched; try limiting the Projects you're searching.";
				return;
			case EXCEPTION :
				this.resultOfOpen = "File system exception: " + ofe.getCause().toString();
				return;
			case MESSAGE_ALREADY_SET :
			default :
		}
	}

	/**
	 * Self-test method
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		String src1 = "net.simsa.foo:Bar.java:25";
		OpenEvent s1 = new OpenEvent(src1);
		assert (s1.packageName.equals("net.simsa.foo"));
		assert (s1.fileName.equals("Bar.java"));
		assert (s1.lineNumber == 25);

		String src2 = ":Bar.java:25";
		OpenEvent s2 = new OpenEvent(src2);
		assert (s2.packageName.equals(""));
		assert (s2.fileName.equals("Bar.java"));
		assert (s2.lineNumber == 25);

		System.out.println("OK");

	}

}
