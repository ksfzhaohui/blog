package com.springboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.springboot.entity.Role;

@Mapper
public interface RoleDao {

	@Select("SELECT id,role_name as roleName,note FROM role WHERE id = #{id}")
	Role findRoleById(@Param("id") long id);

}
