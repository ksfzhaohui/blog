package com.serialization.bean;

import java.math.BigDecimal;

public class Bean1 {

	private String p1;
	private BigDecimal p2;

	public Bean1() {

	}

	public Bean1(String p1, BigDecimal p2) {
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

	public BigDecimal getP2() {
		return p2;
	}

	public void setP2(BigDecimal p2) {
		this.p2 = p2;
	}

	@Override
	public String toString() {
		return "Bean1 [p1=" + p1 + ", p2=" + p2 + "]";
	}

}
