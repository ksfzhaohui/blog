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

public class XATest2 {

	public static void main(String[] args) throws SQLException {
		// true表示打印XA语句,，用于调试
		boolean logXaCommands = true;
		// 获得资源管理器操作接口实例 RM1
		Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds0", "root", "root");
		XAConnection xaConn1 = new MysqlXAConnection((com.mysql.jdbc.ConnectionImpl) conn1, logXaCommands);
		XAResource rm1 = xaConn1.getXAResource();
		// 获得资源管理器操作接口实例 RM2
		Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds1", "root", "root");
		XAConnection xaConn2 = new MysqlXAConnection((com.mysql.jdbc.ConnectionImpl) conn2, logXaCommands);
		XAResource rm2 = xaConn2.getXAResource();
		// AP请求TM执行一个分布式事务，TM生成全局事务id
		byte[] gtrid = "g12345".getBytes();
		int formatId = 1;
		try {
			// ==============分别执行RM1和RM2上的事务分支====================
			// TM生成rm1上的事务分支id
			byte[] bqual1 = "b00001".getBytes();
			Xid xid1 = new MysqlXid(gtrid, bqual1, formatId);
			// 执行rm1上的事务分支
			rm1.start(xid1, XAResource.TMNOFLAGS);// One of TMNOFLAGS, TMJOIN, or TMRESUME.
			PreparedStatement ps1 = conn1
					.prepareStatement("insert into t_order0 (user_id,order_id) values ('110','1212')");
			ps1.execute();
			rm1.end(xid1, XAResource.TMSUCCESS);
			// TM生成rm2上的事务分支id
			byte[] bqual2 = "b00002".getBytes();
			Xid xid2 = new MysqlXid(gtrid, bqual2, formatId);
			// 执行rm2上的事务分支
			rm2.start(xid2, XAResource.TMNOFLAGS);
			PreparedStatement ps2 = conn2
					.prepareStatement("insert into t_order0 (user_id,order_id) values ('111','1213')");
			ps2.execute();
			rm2.end(xid2, XAResource.TMSUCCESS);
			// ===================两阶段提交================================
			// phase1：询问所有的RM 准备提交事务分支
			int rm1_prepare = rm1.prepare(xid1);
			int rm2_prepare = rm2.prepare(xid2);
			// phase2：提交所有事务分支
			boolean onePhase = false; // TM判断有2个事务分支，所以不能优化为一阶段提交
			if (rm1_prepare == XAResource.XA_OK && rm2_prepare == XAResource.XA_OK) {// 所有事务分支都prepare成功，提交所有事务分支
				rm1.commit(xid1, onePhase);
				rm2.commit(xid2, onePhase);
			} else {// 如果有事务分支没有成功，则回滚
				rm1.rollback(xid1);
				rm2.rollback(xid2);
			}
		} catch (XAException e) {
			// 如果出现异常，也要进行回滚
			e.printStackTrace();
		}
	}

	class B1 {

		byte[] gtrid;
		int formatId;
		
		Connection conn1;
		XAConnection xaConn1;
		XAResource rm1;

		public B1() {
			try {
				// true表示打印XA语句,，用于调试
				boolean logXaCommands = true;
				// 获得资源管理器操作接口实例 RM1
				conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds0", "root", "root");
				xaConn1 = new MysqlXAConnection((com.mysql.jdbc.ConnectionImpl) conn1, logXaCommands);
				rm1 = xaConn1.getXAResource();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void start() throws XAException, SQLException {
			// TM生成rm1上的事务分支id
			byte[] bqual1 = "b00001".getBytes();
			Xid xid1 = new MysqlXid(gtrid, bqual1, formatId);
			// 执行rm1上的事务分支
			rm1.start(xid1, XAResource.TMNOFLAGS);// One of TMNOFLAGS, TMJOIN, or TMRESUME.
			PreparedStatement ps1 = conn1
					.prepareStatement("insert into t_order0 (user_id,order_id) values ('110','1212')");
			ps1.execute();
			rm1.end(xid1, XAResource.TMSUCCESS);
		}
	}

	class B2 {

		Connection conn2;
		XAConnection xaConn2;
		XAResource rm2;

		public B2() {
			try {
				// true表示打印XA语句,，用于调试
				boolean logXaCommands = true;
				conn2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ds1", "root", "root");
				xaConn2 = new MysqlXAConnection((com.mysql.jdbc.ConnectionImpl) conn2, logXaCommands);
				rm2 = xaConn2.getXAResource();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
