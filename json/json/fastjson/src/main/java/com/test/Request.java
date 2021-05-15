package com.test;

import java.io.Serializable;
import java.util.List;

public class Request<T> implements Serializable{

	private List<List<List<T>>> list;

	public List<List<List<T>>> getList() {
		return list;
	}

	public void setList(List<List<List<T>>> list) {
		this.list = list;
	}

}
