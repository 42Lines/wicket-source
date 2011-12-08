package net.ftlines.wicketsource;

import java.io.Serializable;

public class Book implements Serializable {
	
	private Integer id;
	private String title;
	private String url;
	private Integer numberPages;
	
	public Book() {
		numberPages = 0;
	}
	
	public Book(Integer id, String title, String url, Integer numberPages) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.numberPages = numberPages;
	}

	public Integer getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getNumberPages() {
		return numberPages;
	}

	public void setNumberPages(Integer numberPages) {
		this.numberPages = numberPages;
	}
	
}