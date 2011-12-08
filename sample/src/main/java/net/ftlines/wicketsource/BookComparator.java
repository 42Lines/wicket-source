package net.ftlines.wicketsource;

import java.util.Comparator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

public class BookComparator implements Comparator<Book> {
	
	SortParam sort;
	
	public BookComparator(SortParam sort)
	{
		this.sort = sort;
	}

	public int compare(Book book0, Book book1) {
		if ((book0 == null) && (book1 == null)) { return 0; }
		if ((book0 == null) && (book1 != null)) { return  1; }
		if ((book1 == null) && (book0 != null)) { return -1; }

		if (sort == null) { 
			return (book0.getId().compareTo(book1.getId()));
		}
		
		int multiplier = 1;
		if (!sort.isAscending()) {
			multiplier = -1;
		}
		
		if ("id".equals(sort.getProperty())) {
			return multiplier*(book0.getId().compareTo(book1.getId()));
		}
		if ("title".equals(sort.getProperty())) {
			return multiplier*(book0.getTitle().compareTo(book1.getTitle()));
		}
		if ("url".equals(sort.getProperty())) {
			return multiplier*(book0.getUrl().compareTo(book1.getUrl()));
		}
		if ("numberPages".equals(sort.getProperty())) {
			return multiplier*(book0.getNumberPages().compareTo(book1.getNumberPages()));
		}
		
		return multiplier*(book0.getId().compareTo(book1.getId()));
	}

}
