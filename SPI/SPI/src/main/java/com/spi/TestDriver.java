package com.spi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class TestDriver implements java.sql.Driver {

	static {
		try {
			register();
		} catch (SQLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		System.out.println("TestDriver-->connect:" + url);
		return null;
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		System.out.println("TestDriver-->acceptsURL:" + url);
		return false;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		System.out.println("TestDriver-->getPropertyInfo:" + url);
		return null;
	}

	@Override
	public int getMajorVersion() {
		System.out.println("TestDriver-->getMajorVersion");
		return 0;
	}

	@Override
	public int getMinorVersion() {
		System.out.println("TestDriver-->getMinorVersion");
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {
		System.out.println("TestDriver-->jdbcCompliant");
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		System.out.println("TestDriver-->getParentLogger");
		return null;
	}

	public static void register() throws SQLException {
		TestDriver registeredDriver = new TestDriver();
		DriverManager.registerDriver(registeredDriver);
	}

}
