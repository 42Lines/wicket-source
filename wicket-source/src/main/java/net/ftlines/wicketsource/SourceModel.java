package net.ftlines.wicketsource;

import org.apache.wicket.Component;
import org.apache.wicket.model.ComponentModel;

public class SourceModel extends ComponentModel<String> 
{
	@Override
	protected String getObject(Component component)
	{
		InstantiationLocation loc = component.getMetaData(AttributeModifyingInstantiationListener.CONSTRUCTED_AT_KEY);
		return loc == null ? null : loc.generateSourceLocationAttribute(component);
	}
}
