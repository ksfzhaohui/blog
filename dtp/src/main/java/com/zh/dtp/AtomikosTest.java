package com.zh.dtp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;

public class AtomikosTest {

	public static void main(String[] args) throws IllegalStateException, SecurityException, SystemException {
		AtomikosDataSourceBean ds1 = createAtomikosDataSourceBean("t_order0");
		AtomikosDataSourceBean ds2 = createAtomikosDataSourceBean("t_order1");

		Connection conn1 = null;
		Connection conn2 = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;

		UserTransaction userTransaction = new UserTransactionImp();
		try {
			// 开启事务
			userTransaction.begin();

			// 执行分支1
			conn1 = ds1.getConnection();
			ps1 = conn1.prepareStatement("insert into t_order0 (user_id,order_id) values ('110','1212')");
			ps1.execute();

			// 执行分支1
			conn2 = ds2.getConnection();
			ps2 = conn2.prepareStatement("insert into t_order0 (user_id,order_id) values ('111','1213')");
			ps2.execute();

			// 两阶段提交
			userTransaction.commit();
		} catch (Exception e) {
			// 异常回滚
			userTransaction.rollback();
		} finally {
			// 关闭连接
		}
	}

	private static AtomikosDataSourceBean createAtomikosDataSourceBean(String dbName) {
		Properties p = new Properties();
		p.setProperty("url", "jdbc:mysql://localhost:3306/" + dbName);
		p.setProperty("user", "root");
		p.setProperty("password", "root");

		AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
		ds.setUniqueResourceName(dbName);

		// MySQL驱动XAResource实现类
		ds.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		ds.setXaProperties(p);
		return ds;
	}
}
