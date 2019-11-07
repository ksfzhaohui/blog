package com.mybatis.vo;

public enum AuthorEnum {

	zhaohui(1, "赵辉");

	private int id;
	private String name;

	private AuthorEnum(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public static AuthorEnum getAuthor(int id) {
		if (id == 1) {
			return zhaohui;
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
