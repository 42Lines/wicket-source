package net.ftlines.wicketsource;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;

/**
 * Sticks the wicket component name into an html attribute on the tag, which 
 * makes it easier to find using the Firebug or Chrome Dev Tools
 * "Inspect Element" menu item.
 *
 * This outputs more than just container classes (such as panels); it also 
 * tags images, labels, and links. Some of that is noise but some may be useful
 * in locating a component in the source.
 * 
 * @author Jenny Brown
 *
 */
public class AttributeModifyingComponentVisitor implements Serializable, IComponentOnBeforeRenderListener {
	private final AttributeModifier wicketSourceAttribute;

	/**
	 * Creates a visitor
	 */
	public AttributeModifyingComponentVisitor() {
		wicketSourceAttribute = new AttributeModifier("wicketSource", true, new SourceModel());
	}

	/**
	 * Attaches the attribute modifier to each component being rendered.
	 */
	public void onBeforeRender(Component component)
	{
		component.add(wicketSourceAttribute);
	}
	
	
	
}
