package net.ftlines.wicketsource;

import java.util.ArrayList;
import java.util.List;

import net.ftlines.wicketsource.BookDataTable.BookDataProvider;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		add(new Label("version", getApplication().getFrameworkSettings()
				.getVersion()));
		add(new Label("bigTitle", "Application Works"));
		add(new MyPanel("mypanel"));
		
		List<IColumn<Book>> columns = new ArrayList<IColumn<Book>>();
		columns.add(new PropertyColumn<Book>(Model.of("ID"), "id", "id"));
		columns.add(new PropertyColumn<Book>(Model.of("Book"), "title", "title") {
			@Override
			public void populateItem(Item<ICellPopulator<Book>> item,
					String componentId, IModel<Book> rowModel) {
				LinkedTitlePanel bookTitleContainer = new LinkedTitlePanel(componentId, rowModel);
				item.add(bookTitleContainer);
			}
		});
		columns.add(new PropertyColumn<Book>(Model.of("# Downloads"), "downloads", "downloads"));
		
		BookDataTable bookTable = new BookDataTable("bookTable", columns, new BookDataProvider(), 5);
		add(bookTable);
		
		add(new FooterPanel("footerPanel"));
	}

	static class MyPanel extends Panel {
		public MyPanel(String id) {
			super(id);
			add(new Label("title", "Inspect Me"));
		}
	}
	
	static class LinkedTitlePanel extends Panel {

		public LinkedTitlePanel(String id, IModel<?> rowModel) {
			super(id, rowModel);
			
			ExternalLink link = new ExternalLink("link", new PropertyModel<String>(rowModel, "url"), new PropertyModel<Book>(rowModel, "title"));
			add(link);
		}
		
	}

}
