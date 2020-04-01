package com.spring.bean;

/**
 * 循环依赖--Cyclic dependency
 * 
 * @author hui.zhao.cfs
 *
 */
public class CDBean2 {

	private CDBean1 cdBean1;

	public CDBean1 getCdBean1() {
		return cdBean1;
	}

	public void setCdBean1(CDBean1 cdBean1) {
		System.out.println("cdBean2 set cdBean1");
		this.cdBean1 = cdBean1;
	}

}
