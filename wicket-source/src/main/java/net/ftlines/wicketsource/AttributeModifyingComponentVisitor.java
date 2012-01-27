package net.ftlines.wicketsource;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;	
import org.apache.wicket.Page;

/**
 * Sticks the wicket component name into an html attribute on the tag, which 
 * makes it easier to find using the Firebug or Chrome Dev Tools
 * "Inspect Element" menu item.
 *
 * This outputs more than just container classes (such as panels); it also 
 * tags images, labels, and links. Some of that is noise but some may be useful
 * in locating a component in the source.
 * 
 * Usage, in the application class: 
 * <pre>
 * if (yourDebugCondition) {
 * 	 getComponentPostOnBeforeRenderListeners().add(new AttributeModifyingComponentVisitor());
 * }
 * </pre>
 * 
 * @author Jenny Brown
 *
 */
public class AttributeModifyingComponentVisitor implements Serializable, IAttributeModifyingComponentVisitor, IComponentOnBeforeRenderListener {
	private final AttributeModifier wicketSourceAttribute;

	/**
	 * Creates a visitor
	 */
	public AttributeModifyingComponentVisitor() {
		wicketSourceAttribute = new AttributeModifier("wicketSource", new SourceModel());
	}

	/**
	 * Causes this visitor to visit all the children of the Page, attaching an html attribute to each one
	 */
	public void addClassNameVisitor(Page page)
	{
		page.visitChildren(new IVisitor<Component, Void>()
		{
			public void component(Component component, IVisit<Void> visit)
			{
				component.add(wicketSourceAttribute);
			}
		});
	}

	/**
	 * For Page components (only) causes a call to addClassNameVisitor(page) visit all of its children
	 */
	public void onBeforeRender(Component component)
	{
		if (!(component instanceof Page)) {
			return;
		}

		addClassNameVisitor((Page)component);
	}
	
	
	
}
