package com.serialize;

public class Person {

	private String id;
	private String name;

	public Person() {

	}

	public Person(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
