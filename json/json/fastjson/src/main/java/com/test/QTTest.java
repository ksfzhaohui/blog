package com.test;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class QTTest {

	public static void main(String[] args) {
		QTTest qt =new QTTest();
		Request<B1> requst =new Request<B1>();
		List<List<List<B1>>> list =new ArrayList<List<List<B1>>>();
		List<List<B1>> list1 =new ArrayList<List<B1>>();
		List<B1> list11=new ArrayList<B1>();
		list11.add(new B1("h11"));
		List<B1> list12=new ArrayList<B1>();
		list12.add(new B1("h12"));
		list1.add(list11);
		list1.add(list12);
		
		List<List<B1>> list2 =new ArrayList<List<B1>>();
		List<B1> list21=new ArrayList<B1>();
		list21.add(new B1("h21"));
		list2.add(list21);
		
		list.add(list1);
		list.add(list2);
		
		requst.setList(list);
		String str = qt.test(requst);
		
		Request<B1> response =new Request<B1>();
		response =JSON.parseObject(str,new TypeReference<Request<B1>>(){});
		System.out.println(response);
	}
	
	private<T> String test(Request request) {
		String xx = JSON.toJSONString(request);
		System.out.println(xx);
		
		return xx;
	}
}
