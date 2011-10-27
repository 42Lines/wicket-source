package net.simsa.wicketsource;

import org.apache.wicket.Page;

public interface IAttributeModifyingComponentVisitor {

	public abstract void addClassNameVisitor(Page page);

}