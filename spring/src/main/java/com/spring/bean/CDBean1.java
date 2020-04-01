package com.spring.bean;

/**
 * 循环依赖--Cyclic dependency
 * 
 * @author hui.zhao.cfs
 *
 */
public class CDBean1 {

	private CDBean2 cdBean2;

	public CDBean2 getCdBean2() {
		return cdBean2;
	}

	public void setCdBean2(CDBean2 cdBean2) {
		System.out.println("cdBean1 set cdBean2");
		this.cdBean2 = cdBean2;
	}

}
