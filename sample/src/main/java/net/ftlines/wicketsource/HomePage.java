package net.ftlines.wicketsource;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		add(new Label("version", getApplication().getFrameworkSettings()
				.getVersion()));
		add(new Label("bigTitle", "Application Works"));
		add(new MyPanel("mypanel"));
	}

	static class MyPanel extends Panel {
		public MyPanel(String id) {
			super(id);
			add(new Label("title", "Inspect Me"));
		}
	}

}
