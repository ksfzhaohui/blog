package com.mybatis.plugin;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

@Intercepts({ @Signature(type = Executor.class, // 确定要拦截的对象
		method = "update", // 要拦截的方法
		args = { MappedStatement.class, Object.class })// 拦截方法的参数
})
public class MyPlugin implements Interceptor {

	Properties prop;

	/**
	 * 代替拦截对象方法内容
	 */
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		System.err.println("======before===============");
		Object obj = invocation.proceed();
		System.err.println("======after===============");
		return obj;
	}

	/**
	 * 生成对象的代理
	 */
	@Override
	public Object plugin(Object target) {
		System.err.println("调用生成代理对象....");
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		System.err.println(properties.get("dbType"));
		this.prop = properties;
	}

}
