package net.ftlines.wicketsource.demo;

import java.util.Comparator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

/**
 * Sorts books by a given field
 * 
 * @author Jenny Brown
 * 
 */
public class BookComparator implements Comparator<Book> {

	SortParam<BookSort> sort;

	public static enum BookSort {
		ID, TITLE, URL, DOWNLOADS
	}

	public BookComparator(SortParam<BookSort> sort) {
		this.sort = sort;
	}

	public int compare(Book book0, Book book1) {
		if ((book0 == null) && (book1 == null)) {
			return 0;
		}
		if ((book0 == null) && (book1 != null)) {
			return 1;
		}
		if ((book1 == null) && (book0 != null)) {
			return -1;
		}

		if (sort == null) {
			return (book0.getId().compareTo(book1.getId()));
		}

		int multiplier = 1;
		if (!sort.isAscending()) {
			multiplier = -1;
		}

		if (BookSort.ID.equals(sort.getProperty())) {
			return multiplier * (book0.getId().compareTo(book1.getId()));
		}
		if (BookSort.TITLE.equals(sort.getProperty())) {
			return multiplier * (book0.getTitle().compareTo(book1.getTitle()));
		}
		if (BookSort.URL.equals(sort.getProperty())) {
			return multiplier * (book0.getUrl().compareTo(book1.getUrl()));
		}
		if (BookSort.DOWNLOADS.equals(sort.getProperty())) {
			return multiplier
					* (book0.getDownloads().compareTo(book1.getDownloads()));
		}

		return multiplier * (book0.getId().compareTo(book1.getId()));
	}

}
