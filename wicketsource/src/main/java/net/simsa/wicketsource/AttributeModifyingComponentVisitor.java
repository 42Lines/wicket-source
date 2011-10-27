package net.simsa.wicketsource;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Component.IVisitor;
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
 * Usage, in the base Page class for the application: 
 * <pre>
 * private AttributeModifyingComponentVisitor locationTagger = new AttributeModifyingComponentVisitor(); // member variable
 * 
 * 	@Override
 *	protected void onBeforeRender()
 *	{
 *		super.onBeforeRender(); // goes first so repeating views/grids/datatables have rendered children.
 *		if (getApplication().getDebugSettings().isOutputMarkupContainerClassName()) {
 *			locationTagger.addClassNameVisitor(this);
 *		}
 *	}
 * </pre>
 * 
 * @author Jenny Brown
 *
 */
public class AttributeModifyingComponentVisitor implements Serializable, IAttributeModifyingComponentVisitor {
	private final AttributeModifier wicketSourceAttribute;

	public AttributeModifyingComponentVisitor() {
		wicketSourceAttribute = new AttributeModifier("wicketSource", true, new SourceModel());
	}

	/* (non-Javadoc)
	 * @see net.simsa.wicketsource.IAttributeModifyingComponentVisitor#addClassNameVisitor(org.apache.wicket.Page)
	 */
	public void addClassNameVisitor(Page page)
	{
		page.visitChildren(new IVisitor<Component>()
		{
			public Object component(final Component component)
			{
				component.add(wicketSourceAttribute);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}	
}
