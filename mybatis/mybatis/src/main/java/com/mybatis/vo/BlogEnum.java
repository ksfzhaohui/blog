package com.mybatis.vo;

public class BlogEnum {

	private long id;
	private String title;
	private AuthorEnum author;
	private String content;

	public BlogEnum() {

	}

	public BlogEnum(Long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AuthorEnum getAuthor() {
		return author;
	}

	public void setAuthor(AuthorEnum author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Blog [id=" + id + ", title=" + title + ", author=" + author.getName() + ", content=" + content + "]";
	}

}
