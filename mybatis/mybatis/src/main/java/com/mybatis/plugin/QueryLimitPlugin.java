package com.mybatis.plugin;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

@Intercepts({ @Signature(type = StatementHandler.class, // 确定要拦截的对象
		method = "prepare", // 要拦截的方法
		args = { Connection.class, Integer.class })// 拦截方法的参数
})
public class QueryLimitPlugin implements Interceptor {

	private int limit;
	private String dbType;

	private static final String LMT_TABLE_NAME = "limit_table_name_blog";

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler stmtHandler = (StatementHandler) invocation.getTarget();
		MetaObject metaStmtHandler = SystemMetaObject.forObject(stmtHandler);

		while (metaStmtHandler.hasGetter("h")) {
			Object object = metaStmtHandler.getValue("h");
			metaStmtHandler = SystemMetaObject.forObject(object);
		}
		while (metaStmtHandler.hasGetter("target")) {
			Object object = metaStmtHandler.getValue("target");
			metaStmtHandler = SystemMetaObject.forObject(object);
		}

		String sql = (String) metaStmtHandler.getValue("delegate.boundSql.sql");
		String limitSql;

		if ("mysql".equals(this.dbType) && sql.indexOf(LMT_TABLE_NAME) == -1) {
			sql = sql.trim();
			limitSql = "select * from (" + sql + ") " + LMT_TABLE_NAME + " limit " + limit;
			metaStmtHandler.setValue("delegate.boundSql.sql", limitSql);

		}

		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		String limit = properties.getProperty("limit");
		String dbType = properties.getProperty("dbType");
		this.limit = Integer.valueOf(limit);
		this.dbType = dbType;
	}

}
