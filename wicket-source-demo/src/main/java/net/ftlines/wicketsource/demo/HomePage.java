package net.ftlines.wicketsource.demo;

import java.util.ArrayList;
import java.util.List;

import net.ftlines.wicketsource.demo.BookDataTable.BookDataProvider;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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

/**
 * The main home page of this application
 * @author Jenny Brown
 *
 */
public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;
	ModalWindow modal;

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
				LinkedTitlePanel bookTitleContainer = new LinkedTitlePanel(
						componentId, rowModel);
				item.add(bookTitleContainer);
			}
		});
		columns.add(new PropertyColumn<Book>(Model.of("# Downloads"),
				"downloads", "downloads"));

		BookDataTable bookTable = new BookDataTable("bookTable", columns,
				new BookDataProvider(), 5);
		add(bookTable);
		add(modal = new ModalWindow("modalWindow"));
		add(new ModalLink("showModal", modal) {
			public Component createContent()  {
				return new MyPanel(modal.getContentId());
			}
		});
		add(new FooterPanel("footerPanel"));
	}
	
	static abstract class ModalLink extends AjaxLink {
		private ModalWindow modalWindow;
		
		public ModalLink(String id, ModalWindow modalWindow) {
			super(id);
			this.modalWindow = modalWindow;
		}
		
		public abstract Component createContent();

		@Override
		public void onClick(AjaxRequestTarget target) {
			modalWindow.setCssClassName("modalCss");
			modalWindow.setContent(createContent());
			modalWindow.setTitle("A Title Goes Here");
			modalWindow.setInitialHeight(300);
			modalWindow.setInitialWidth(400);
			modalWindow.show(target);
		}
		
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

			ExternalLink link = new ExternalLink("link",
					new PropertyModel<String>(rowModel, "url"),
					new PropertyModel<Book>(rowModel, "title"));
			add(link);
		}

	}

}
