package com.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

public class QTTest2 {

	public static void main(String[] args) throws IOException {
		QTTest2 qt =new QTTest2();
		Request requst =new Request();
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
		byte[] data = qt.test(requst);
		
		Request<B1> response =new Request<B1>();
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		HessianInput input = new HessianInput(is);
		response = (Request<B1>) input.readObject();

		System.out.println(response);
	}
	
	private byte[] test(Request request) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		HessianOutput output = new HessianOutput(os);
		output.writeObject(request);
		byte[] data = os.toByteArray();

		
		return data;
	}
}
