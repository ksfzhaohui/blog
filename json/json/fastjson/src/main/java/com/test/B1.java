package com.test;

import java.io.Serializable;

public class B1 implements Serializable{
	
	
	private String xx;
	
	public B1() {
		
	}
	
	public B1(String xx) {
		this.xx=xx;
	}

	public String getXx() {
		return xx;
	}

	public void setXx(String xx) {
		this.xx = xx;
	}
	
	

}
