package com.mybatis.myObjectFactory;

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

public class MyObjectFactory extends DefaultObjectFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setProperties(Properties properties) {
		System.out.println("setProperties:" + properties);
		super.setProperties(properties);
	}

	@Override
	public <T> T create(Class<T> type) {
		System.out.println("create:" + type);
		return super.create(type);
	}

	@Override
	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		System.out.println("create:" + type + "," + constructorArgTypes + "," + constructorArgs);
		return super.create(type, constructorArgTypes, constructorArgs);
	}

}
