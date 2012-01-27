package net.ftlines.wicketsource;

import java.io.Serializable;

import net.ftlines.wicketsource.AttributeModifyingInstantiationListener.UnsupportedComponentException;

import org.apache.wicket.Component;
import org.apache.wicket.markup.MarkupException;

/**
 * Holds information about where a component was created. Pairs with a
 * {@link AttributeModifyingComponentVisitor} to record the source information in an
 * html attribute for inspection and (eventually) click-through.
 * 
 * The new MarkupException in the constructor here is an alternative to
 * <code>component.getMetaData(Component.CONSTRUCTED_AT_KEY)</code> which is package-private
 * and thus internal to wicket. If wicket eventually exposes the markup
 * exception from creation time, we can use that directly instead of keeping our
 * own separate Throwable.
 * 
 * @author Jenny Brown
 * 
 */
public class InstantiationLocation implements Serializable {
	private static final long serialVersionUID = 1L;

	private StackTraceElement traceElement;

	/**
	 * When a component is instantiated, record its source location as part of its metadata.
	 * @param component being instantiated
	 * @throws UnsupportedComponentException
	 */
	public InstantiationLocation(Component component) 
	throws UnsupportedComponentException
	{
		traceElement = WicketSourceFilter.findCreationSource(new MarkupException("constructed"), component);
		if (traceElement == null) { 
			throw new UnsupportedComponentException(component.getId());
		}
	}

	/**
	 * What kind of wicket component is this (Label, BookmarkableLink, etc) - convenience lookup.
	 * @param component to identify
	 * @return string with the class name of the component
	 */
	public String getComponentType(Component component)
	{
		return WicketSourceFilter.getClassName(component);
	}

	/**
	 * @return Gets a string from a stack trace element indicating the package that the class came from or empty for default package
	 */
	private String getPackageLocation()
	{
		if (traceElement.getClassName().indexOf('.') == -1) { // default package
			return "";
		} else {
			return traceElement.getClassName().substring(0, traceElement.getClassName().lastIndexOf('.'));
		}
	}

	/**
	 * @return Gets a string from a stack trace element indicating the class that the component came from
	 */
	private String getClassLocation()
	{
		if (traceElement.getClassName().indexOf('.') == -1) { // default package
			return traceElement.getClassName();
		} else {
			return traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf('.') + 1);
		}
	}

	/**
	 * @return Gets the filename from a stack trace element
	 */
	private String getFilename()
	{
		return traceElement.getFileName();
	}

	/**
	 * @return Gets the line number of the source location from a stack trace element
	 */
	private Integer getLineNumber()
	{
		return traceElement.getLineNumber();
	}

	/**
	 * Produces the value for the html attribute describing the location in Java
	 * source of this component. Result is of the form
	 * net.simsa.packagename:ClassName.java:35 (package:file:line).
	 * 
	 * @param component to search for
	 * @return String indicating package : filename.java : lineNumber
	 */
	public String generateSourceLocationAttribute()
	{
		return getPackageLocation() + ":" + getFilename() + ":"	+ getLineNumber();
	}

	@Override
	public String toString()
	{
		return "InstantiationLocation: packageLocation=" + getPackageLocation() + ", classLocation="
				+ getClassLocation() + ", lineNumber=" + getLineNumber();
	}

}
