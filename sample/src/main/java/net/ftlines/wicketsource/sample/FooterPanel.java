package net.ftlines.wicketsource.sample;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class FooterPanel extends Panel {

	public FooterPanel(String id) {
		super(id);
		
		add(new Label("footerText", "This is some footer text."));
		
	}
	

	
	
}
