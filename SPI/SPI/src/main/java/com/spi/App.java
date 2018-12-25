package com.spi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class App {

	public static void main(String[] args) throws ClassNotFoundException {

		String url = "jdbc:mysql://localhost:3306/db3";
		String username = "root";
		String password = "root";
		String sql = "update travelrecord set name=\'bbb\' where id=1";
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, username, password);
			PreparedStatement pstmt = con.prepareStatement(sql);
			con.setAutoCommit(false);
			pstmt.execute();
			con.commit();
			System.out.println("update success");
		} catch (Exception se) {
			if (con != null) {
				try {
					System.out.println("rollback");
					con.rollback();
				} catch (SQLException e) {
					System.err.println("rollback error");
				}
			}
			se.printStackTrace();
		}
	}
}
