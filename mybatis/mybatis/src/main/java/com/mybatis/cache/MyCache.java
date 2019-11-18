package com.mybatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;

/**
 * 自动缓存，比如使用redis来缓存数据
 * 
 * 如果缓存里获取不到，会重新执行sql
 * 
 * @author hui.zhao.cfs
 *
 */
public class MyCache implements Cache{

	private String id;

	public MyCache(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		System.out.println("getId :" + id);
		return id;
	}

	@Override
	public void putObject(Object key, Object value) {
		System.out.println("putObject null");
	}

	@Override
	public Object getObject(Object key) {
		System.out.println("getObject null");
		return null;
	}

	@Override
	public Object removeObject(Object key) {
		System.out.println("removeObject null");
		return null;
	}

	@Override
	public void clear() {
		System.out.println("clear");
	}

	@Override
	public int getSize() {
		System.out.println("getSize 0");
		return 0;
	}

	@Override
	public ReadWriteLock getReadWriteLock() {
		System.out.println("getReadWriteLock null");
		return null;
	}

}
