package net.ftlines.wicketsource;


import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.application.IComponentInstantiationListener;

/**
 * Notes the creation location of components and saves that metadata in the component metadata area.
 * This is the precursor step to using a {@link AttributeModifyingComponentVisitor} for writing out an
 * HTML <code>wicketsource="net.simsa.photo.web.MainMenuPanel:35"</code> style of attribute.
 * 
 * This uses a similar approach to line-precise-reporting-on-new-component and is likely to be 
 * equally slow, so turn it on only in debug/development mode, not production.
 * 
 * Usage, in your WicketApplication:
 * <pre>
 * if (getDebugSettings().isLinePreciseReportingOnNewComponentEnabled()) {
 *      // wicket 1.4
 *      addComponentInstantiationListener(new AttributeModifyingInstantiationListener()); 
 * 		// or wicket 1.5
 * 		getComponentInstantiationListeners().add(new AttributeModifyingInstantiationListener()); 
 * }
 * </pre>
 *  
 * See {@link AttributeModifyingComponentVisitor} for usage syntax in onBeforeRender().
 * 
 * @author Jenny Brown
 *
 */
public class AttributeModifyingInstantiationListener implements IComponentInstantiationListener {
	/**
	 * This is an alternative to Component.CONSTRUCTED_AT_KEY which is package-private and thus internal to wicket.
	 * If wicket eventually exposes the markup exception from creation time, we can use that directly instead.
	 */
	static MetaDataKey<InstantiationLocation> CONSTRUCTED_AT_KEY = new MetaDataKey<InstantiationLocation>() {
		private static final long serialVersionUID = 1L;
	};
	
	/**
	 * Indicates that the component's source location could not be determined,
	 * probably because this is an internal wicket enclosure or similar
	 * automatically generated component.
	 * 
	 * @author Jenny Brown
	 * 
	 */
	static class UnsupportedComponentException extends Exception {
		public UnsupportedComponentException(String message) {
			super(message);
		}
	}	
	
	/**
	 * When a component is instantiated, record the source location as part of its metadata.
	 */
	public void onInstantiation(Component component)
	{
		if (component instanceof org.apache.wicket.markup.html.internal.Enclosure) {
			return; // nothing to see here; bail out early to save time.
		}
		
		try {
			component.setMetaData(CONSTRUCTED_AT_KEY, new InstantiationLocation(component));
		} catch (UnsupportedComponentException ie) {
			// nothing to see here; skip this component.
		}
	}
}