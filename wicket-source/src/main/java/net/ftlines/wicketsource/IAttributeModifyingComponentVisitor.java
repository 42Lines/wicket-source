package net.ftlines.wicketsource;

import org.apache.wicket.Page;

/**
 * Visits all children of a Page, to attach an html attribute to each
 * @author jenny
 *
 */
public interface IAttributeModifyingComponentVisitor {

	public abstract void addClassNameVisitor(Page page);

}
