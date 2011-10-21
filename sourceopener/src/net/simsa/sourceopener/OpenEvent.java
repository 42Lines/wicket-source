package net.simsa.sourceopener;

import java.util.Properties;

/**
 * Represents a location in the source.
 * @author Jenny Brown
 *
 */
public class OpenEvent {
	
	String projectName;
	String packageName;
	String fileName;
	int lineNumber;
	
	public String toString()
	{
		return "OpenEvent for " + packageName + ":" + fileName +  ":" + lineNumber;
	}
	
	public OpenEvent(String packageName, String fileName, int lineNumber) {
		super();
		this.packageName = packageName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
	public OpenEvent(String src)
	{
		fromSrcLine(src);
	}
	private void fromSrcLine(String src)
	{
		String[] pieces = src.split(":");
		packageName = pieces[0];
		fileName = pieces[1];
		lineNumber = Integer.parseInt(pieces[2]);
	}
	
	/**
	 * Throws an exception if the url being requested isn't compatible with the kinds of
	 * requests we can serve.
	 * @param uri
	 * @param params
	 * @throws IllegalArgumentException
	 */
	public OpenEvent(String uri, Properties params)
	throws IllegalArgumentException
	{
		if (! "/open".equals(uri)) {
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

	/** 
	 * Self-test method
	 * @param args
	 */
	public static void main(String[] args)
	{
		String src1 = "net.simsa.foo:Bar.java:25";
		OpenEvent s1 = new OpenEvent(src1);
		assert(s1.packageName.equals("net.simsa.foo"));
		assert(s1.fileName.equals("Bar.java"));
		assert(s1.lineNumber == 25);
		
		String src2 = ":Bar.java:25";
		OpenEvent s2 = new OpenEvent(src2);
		assert(s2.packageName.equals(""));
		assert(s2.fileName.equals("Bar.java"));
		assert(s2.lineNumber == 25);
		
		System.out.println("OK");
		
	}

}
