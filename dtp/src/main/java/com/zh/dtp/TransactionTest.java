package com.zh.dtp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionTest {

	public static void main(String[] args) {
		commit();
//		rollback();
	}

	private static void commit() {
		Connection conn = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds0", "root", "root");
			conn.setAutoCommit(false); // JDBC中默认是true，自动提交事务
			ps1 = conn.prepareStatement("insert into t_order0 (user_id,order_id) values (?,?)");
			ps1.setObject(1, "111");
			ps1.setObject(2, "1008");
			ps1.execute();

			ps2 = conn.prepareStatement("insert into t_order0 (user_id,order_id) values (?,?)");
			ps2.setObject(1, "112");
			ps2.setObject(2, "1009");
			ps2.execute();

			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void rollback() {
		Connection conn = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds0", "root", "root");
			conn.setAutoCommit(false); // JDBC中默认是true，自动提交事务
			ps1 = conn.prepareStatement("insert into t_order0 (user_id,order_id) values (?,?)");
			ps1.setObject(1, "111");
			ps1.setObject(2, "1008");
			ps1.execute();

			ps2 = conn.prepareStatement("insert into t_order0 (user_id,order_id) values (?,?)");
			ps2.setObject(1, "112");
			ps2.setObject(2, "1009abcdefgh123456789");
			ps2.execute();

			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
}
