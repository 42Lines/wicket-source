package net.ftlines.wicketsource;

import org.apache.wicket.Component;
import org.apache.wicket.model.ComponentModel;

/**
 * Holds metadata for the instantiation location of a component
 * @author Jenny Brown
 *
 */
public class SourceModel extends ComponentModel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @return package:file.java:lineNumber from InstantiationLocation.generateSourceLocationAttribute as a String
	 */
	@Override
	protected String getObject(Component component)
	{
		InstantiationLocation loc = (InstantiationLocation) component.getMetaData(AttributeModifyingInstantiationListener.CONSTRUCTED_AT_KEY);
		return loc == null ? null : loc.generateSourceLocationAttribute();
	}
}
