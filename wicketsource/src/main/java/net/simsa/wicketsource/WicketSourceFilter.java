package net.simsa.wicketsource;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.Strings;

/**
 * Helper utilities for digging through stack traces and component paths.
 * Work is based on org.apache.wicket.util.string.Strings and slightly modified.
 * 
 * @author Jenny Brown
 *
 */
public class WicketSourceFilter {

	/** The notification method where instantiation listeners are called. */
	private static final String NOTIFY_COMPONENT_INSTANTIATION_LISTENERS = "org.apache.wicket.Application.notifyComponentInstantiationListeners";
	/** When we hit this as the component origin, we know we won't get anything useful out of it.  */
	private static final String WICKET_PAGE_ON_RENDER = "org.apache.wicket.Page.onRender";

	/**
	 * Human readable class name for the type of component this is (Label, BookmarkablePageLink, whatever).
	 * @param component
	 * @return
	 */
	public static String getClassName(Component component)
	{
		// anonymous class? Get the parent's class name
		String name = component.getClass().getName();
		if (name.indexOf("$") > 0) {
			name = component.getClass().getSuperclass().getName();
		}

		// remove the path component
		name = Strings.lastPathComponent(name, Component.PATH_SEPARATOR);
		return name;
	}
	/**
	 * Produces which line of the stack trace is the most useful for inspecting the
	 * origins of this component in the Java source.  Non-useful for types: 
	 * org.apache.wicket.markup.html.internal.Enclosure and org.apache.wicket.markup.html.internal.HtmlHeaderContainer
	 * which are filtered out ahead of time by the AttributeModifyingInstantionListener.
	 * @param location The throwable from creation time
	 * @param component The component we're examining
	 * @return
	 */
	public static StackTraceElement findCreationSource(Throwable location, Component component)
	{
		return(findCreationSource(filterForInteresting(component, location), component));
	}

	/**
	 * Takes a list of "interesting" stack trace elements and figures out which
	 * one is the most likely source for creation of the component.
	 * 
	 * @param elements
	 * @param component
	 * @return
	 */
	private static StackTraceElement findCreationSource(List<StackTraceElement> elements, Component component)
	{
//		System.out.println(".");
//		System.out.println("Component " + component.getId() + " -- " + component.getClassRelativePath());
//		for (StackTraceElement element : elements) {
//			System.out.println(element);
//		}

		StackTraceElement wicketInternalOrigin = null;
		StackTraceElement userDeclaredOrigin = null;
		
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).toString().indexOf(WicketSourceFilter.NOTIFY_COMPONENT_INSTANTIATION_LISTENERS) != -1) {
				// For internal wicket classes, we'll typically see the line immediately after the notify call.
				if (i + 1 < elements.size()) {
					wicketInternalOrigin = elements.get(i + 1);
					if (wicketInternalOrigin.toString().indexOf(WicketSourceFilter.WICKET_PAGE_ON_RENDER) != -1) {
						wicketInternalOrigin = null; // useless result.
					}
				}
			}
			
			String elementClass = elements.get(i).getClassName();
			String componentClass = component.getClassRelativePath().substring(0, component.getClassRelativePath().indexOf(':'));
			if (elementClass.equals(componentClass)) {
				// For user-created panels and wrapped classes, we'll see the user's class in the trace, 
				// immediately followed by the place where it's declared.
				if (i + 1 < elements.size()) {
					userDeclaredOrigin = elements.get(i+1);
				} else {
					userDeclaredOrigin = elements.get(i);
				}
			}
		}
		if (userDeclaredOrigin != null) { 
//			System.out.println("Using user-declared: " + userDeclaredOrigin);
			return userDeclaredOrigin;
		} else {
//			System.out.println("Using wicket internal from: " + wicketInternalOrigin);
			return wicketInternalOrigin;
		}
	}	

	
	/**
	 * This is a modified version of org.apache.wicket.util.string.Strings
	 * toString(final Component component, final Throwable location)
	 * method but set up just to filter a stack trace, not print it.
	 * 
	 * Original javadoc follows:
	 * 
	 * Creates a location stacktrace string representation for the component for
	 * reference when the render check fails. This method filters out most of
	 * the unnecessary parts of the stack trace. The message of the
	 * <code>location</code> is used as a verb in the rendered string. Use
	 * "added", "constructed" or similar verbs as values.
	 * 
	 * @param component
	 *            the component that was constructed or added and failed to
	 *            render
	 * @param location
	 *            the location where the component was created or added in the
	 *            java code.
	 * @return a string giving the line precise location where the component was
	 *         added or created.
	 */
	private static List<StackTraceElement> filterForInteresting(final Component component, final Throwable location)
	{
		Class<?> componentClass = component.getClass();

		// try to find the component type, if it is an inner element, then get
		// the parent component.
		String componentType = componentClass.getName();
		if (componentType.indexOf('$') >= 0)
		{
			componentType = componentClass.getSuperclass().getName();
		}

		componentType = componentType.substring(componentType.lastIndexOf('.') + 1);

		// a list of stacktrace elements that need to be skipped in the location
		// stack trace
		String[] skippedElements = new String[] { "org.apache.wicket.MarkupContainer",
				"org.apache.wicket.Component", "org.apache.wicket.markup" };

		// a list of stack trace elements that stop the traversal of the stack
		// trace
		String[] breakingElements = new String[] { "org.apache.wicket.protocol.http.WicketServlet",
				"org.apache.wicket.protocol.http.WicketFilter", "java.lang.reflect" };

		StackTraceElement[] trace = location.getStackTrace();
		List<StackTraceElement> keep = new ArrayList<StackTraceElement>();
		for (int i = 0; i < trace.length; i++)
		{
			String traceString = trace[i].toString();
			if (shouldSkip(traceString, skippedElements))
			{
				// don't print this line, is wicket internal
				continue;
			}

			if (!(traceString.startsWith("sun.reflect.") && i > 1))
			{
				// filter out reflection API calls from the stack trace
				if (traceString.indexOf("java.lang.reflect") < 0)
				{
					keep.add(trace[i]);
				}
				if (shouldSkip(traceString, breakingElements))
				{
					break;
				}
			}
		}
		return keep;
	}

	/**
	 * This is a modified version of org.apache.wicket.util.string.Strings
	 * shouldSkip(String, String[]) method but set up just to filter a stack
	 * trace, not print it.
	 */
	private static boolean shouldSkip(String text, String[] filters)
	{
		for (int i = 0; i < filters.length; i++)
		{
			if (text.indexOf(filters[i]) >= 0)
			{
				return true;
			}
		}
		return false;
	}	

}
