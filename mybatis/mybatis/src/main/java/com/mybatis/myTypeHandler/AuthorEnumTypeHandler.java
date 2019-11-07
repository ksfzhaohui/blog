package com.mybatis.myTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mybatis.vo.AuthorEnum;

public class AuthorEnumTypeHandler implements TypeHandler<AuthorEnum> {

	@Override
	public void setParameter(PreparedStatement ps, int i, AuthorEnum parameter, JdbcType jdbcType) throws SQLException {
		System.out.println("AuthorEnumTypeHandler->setParameter");
		ps.setInt(i, parameter.getId());
	}

	@Override
	public AuthorEnum getResult(ResultSet rs, String columnName) throws SQLException {
		int id = Integer.valueOf(rs.getString(columnName));
		System.out.println("AuthorEnumTypeHandler->getResult");
		return AuthorEnum.getAuthor(id);
	}

	@Override
	public AuthorEnum getResult(ResultSet rs, int columnIndex) throws SQLException {
		int id = rs.getInt(columnIndex);
		System.out.println("AuthorEnumTypeHandler->getResult");
		return AuthorEnum.getAuthor(id);
	}

	@Override
	public AuthorEnum getResult(CallableStatement cs, int columnIndex) throws SQLException {
		int id = cs.getInt(columnIndex);
		System.out.println("AuthorEnumTypeHandler->getResult");
		return AuthorEnum.getAuthor(id);
	}
}
