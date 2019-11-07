package com.mybatis.myTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 类型处理器（typeHandlers） 可以重写类型处理器或创建你自己的类型处理器来处理不支持的或非标准的类型
 * 
 * @author hui.zhao.cfs
 *
 */
/*@MappedTypes({ String.class })
@MappedJdbcTypes(JdbcType.VARCHAR)*/
public class MyStringTypeHandler extends BaseTypeHandler<String> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
			throws SQLException {
		System.out.println("MyStringTypeHandler->setNonNullParameter");
		ps.setString(i, parameter);
	}

	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		System.out.println("MyStringTypeHandler->getNullableResult");
		return rs.getString(columnName);
	}

	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		System.out.println("MyStringTypeHandler->getNullableResult");
		return rs.getString(columnIndex);
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		System.out.println("MyStringTypeHandler->getNullableResult");
		return cs.getString(columnIndex);
	}
}