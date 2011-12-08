package net.ftlines.wicketsource;

import java.io.Serializable;

public class Book implements Serializable {
	
	private Integer id;
	private String title;
	private String url;
	private Integer downloads;
	
	public Book() {
		downloads = 0;
	}
	
	public Book(Integer id, String title, String url, Integer downloads) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.downloads = downloads;
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

	public Integer getDownloads() {
		return downloads;
	}

	public void setDownloads(Integer downloads) {
		this.downloads = downloads;
	}
	
}