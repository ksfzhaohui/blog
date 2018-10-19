package com.serialization.bean;

public class Bean2 {

	private String p1;
	private Object p2;

	public Bean2() {

	}

	public Bean2(String p1, Object p2) {
		super();
		this.p1 = p1;
		this.p2 = p2;
	}

	public String getP1() {
		return p1;
	}

	public void setP1(String p1) {
		this.p1 = p1;
	}

	public Object getP2() {
		return p2;
	}

	public void setP2(Object p2) {
		this.p2 = p2;
	}

	@Override
	public String toString() {
		return "Bean2 [p1=" + p1 + ", p2=" + p2 + "]";
	}

}
