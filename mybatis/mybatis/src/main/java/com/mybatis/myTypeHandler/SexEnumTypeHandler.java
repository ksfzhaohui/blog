package com.mybatis.myTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.mybatis.vo.Sex;

public class SexEnumTypeHandler implements TypeHandler<Sex> {

	@Override
	public void setParameter(PreparedStatement ps, int i, Sex parameter, JdbcType jdbcType) throws SQLException {
		System.out.println("SexEnumTypeHandler->setParameter");
		ps.setInt(i, parameter.getId());
	}

	@Override
	public Sex getResult(ResultSet rs, String columnName) throws SQLException {
		int id = Integer.valueOf(rs.getString(columnName));
		System.out.println("SexEnumTypeHandler->getResult");
		return Sex.getSex(id);
	}

	@Override
	public Sex getResult(ResultSet rs, int columnIndex) throws SQLException {
		int id = rs.getInt(columnIndex);
		System.out.println("SexEnumTypeHandler->getResult");
		return Sex.getSex(id);
	}

	@Override
	public Sex getResult(CallableStatement cs, int columnIndex) throws SQLException {
		int id = cs.getInt(columnIndex);
		System.out.println("SexEnumTypeHandler->getResult");
		return Sex.getSex(id);
	}

}
