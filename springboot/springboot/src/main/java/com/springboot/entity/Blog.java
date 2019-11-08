package com.springboot.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Blog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;
	@Column(nullable = false)
	private String title;
	@Column(nullable = false)
	private String author;
	@Column(nullable = false)
	private String content;

	@Override
	public String toString() {
		return "Blog [id=" + id + ", title=" + title + ", author=" + author + ", content=" + content + "]";
	}

}
