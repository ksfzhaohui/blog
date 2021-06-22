package com.zh.dtp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.mysql.jdbc.jdbc2.optional.MysqlXAConnection;
import com.mysql.jdbc.jdbc2.optional.MysqlXid;

public class XAMysql {

	public static void main(String[] args) throws SQLException {
		// 打印XA语句用于调试
		boolean logXaCommands = true;
		Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds0", "root", "root");
		XAConnection xaConn1 = new MysqlXAConnection((com.mysql.jdbc.ConnectionImpl) conn1, logXaCommands);
		XAResource rm1 = xaConn1.getXAResource();

		Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds1", "root", "root");
		XAConnection xaConn2 = new MysqlXAConnection((com.mysql.jdbc.ConnectionImpl) conn2, logXaCommands);
		XAResource rm2 = xaConn2.getXAResource();

		// 全局事务id
		byte[] gid = "global".getBytes();
		int formatId = 1;
		try {
			// 事务分支1
			byte[] bqual1 = "b1".getBytes();
			Xid xid1 = new MysqlXid(gid, bqual1, formatId);
			rm1.start(xid1, XAResource.TMNOFLAGS);
			PreparedStatement ps1 = conn1
					.prepareStatement("insert into t_order0 (user_id,order_id) values ('110','1212')");
			ps1.execute();
			rm1.end(xid1, XAResource.TMSUCCESS);

			// 事务分支2
			byte[] bqual2 = "b2".getBytes();
			Xid xid2 = new MysqlXid(gid, bqual2, formatId);
			rm2.start(xid2, XAResource.TMNOFLAGS);
			PreparedStatement ps2 = conn2
					.prepareStatement("insert into t_order0 (user_id,order_id) values ('111','1213')");
			ps2.execute();
			rm2.end(xid2, XAResource.TMSUCCESS);

			// 两阶段提交
			int rm1_prepare = rm1.prepare(xid1);
			int rm2_prepare = rm2.prepare(xid2);
			if (rm1_prepare == XAResource.XA_OK && rm2_prepare == XAResource.XA_OK) {
				rm1.commit(xid1, false);
				rm2.commit(xid2, false);
			} else {
				rm1.rollback(xid1);
				rm2.rollback(xid2);
			}
		} catch (XAException e) {
			e.printStackTrace();
		}
	}
}
