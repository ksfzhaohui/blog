package com.mybatis.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mybatis.vo.Role;

public interface RoleMapper {

	public Role getRole1(long id);

	public Role getRole2(long id);

	public Role findRoleByMap(Map map);

	// 使用注解方式传递代码，如果参数太多，可以使用javaBean作为参数
	public Role findRoleByAnnotation(@Param("roleName") String rolename, @Param("note") String note);

	public long insertRole(Role role);
}
