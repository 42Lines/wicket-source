package net.ftlines.wicketsource.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A list of books on the web page
 * 
 * @author Jenny Brown
 * 
 */
public class BookDataTable extends DefaultDataTable<Book> {

	public BookDataTable(String id, List<IColumn<Book>> columns,
			ISortableDataProvider<Book> dataProvider, int rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
	}

	static class BookDataProvider extends SortableDataProvider<Book> {

		public BookDataProvider() {
		}

		public Iterator<? extends Book> iterator(int first, int count) {
			return getBooks(getSort()).subList(first, first + count).iterator();
		}

		public int size() {
			return getBooks(getSort()).size();
		}

		public IModel<Book> model(Book book) {
			return Model.of(book);
		}

		/**
		 * Mock database lookup for data. Normal this data would come from a
		 * call out to an injected Hibernate data lookup class.
		 * 
		 * @return List of books
		 */
		private List<Book> getBooks(SortParam sort) {
			List<Book> books = new ArrayList<Book>();
			books.add(new Book(1, "Alice's Adventures in Wonderland",
					"http://www.gutenberg.org/files/11/11-h/11-h.htm", 506));
			books.add(new Book(2, "The Practice and Science of Drawing",
					"http://www.gutenberg.org/files/14264/14264-h/14264-h.htm",
					157));
			books.add(new Book(3, "The Tale of Peter Rabbit",
					"http://www.gutenberg.org/files/14838/14838-h/14838-h.htm",
					156));
			books.add(new Book(4, "A Christmas Carol",
					"http://www.gutenberg.org/files/46/46-h/46-h.htm", 1805));

			Collections.sort(books, new BookComparator(sort));

			return books;
		}

	}

}