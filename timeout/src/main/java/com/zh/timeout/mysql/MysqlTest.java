package com.zh.timeout.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MysqlTest {

	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement ps1 = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
					"jdbc:mysql://20.11.23.44:3307/ds0?connectTimeout=2000&socketTimeout=20", "root", "root");
//					"jdbc:mysql://localhost:3306/ds0?connectTimeout=2000&socketTimeout=200", "root", "root");
			ps1 = conn.prepareStatement("select * from t_order0");
			ps1.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
