package com.mybatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.RoleMapper;
import com.mybatis.vo.Role;

public class RoleMain {

	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		// 方式一
		SqlSession session1 = sqlSessionFactory.openSession();
		try {
			RoleMapper mapper = session1.getMapper(RoleMapper.class);
			Role role = mapper.getRole1(111);
			System.out.println("getRole1-->"+role.toString());

			// 没开启自动驼峰映射，映射失败
			Role role2 = mapper.getRole2(111);
			System.out.println("getRole2-->"+role2.toString());
		} finally {
			session1.close();
		}

		// 方式一
		SqlSession session2 = sqlSessionFactory.openSession();
		try {
			RoleMapper mapper = session2.getMapper(RoleMapper.class);
			// <setting name="mapUnderscoreToCamelCase" value="true" /> 开启驼峰映射
			Role role2 = mapper.getRole2(111);
			System.out.println(role2.toString());
		} finally {
			session2.close();
		}

		// 方式一
		SqlSession session3 = sqlSessionFactory.openSession();
		try {
			RoleMapper mapper = session3.getMapper(RoleMapper.class);
			Map map = new HashMap<>();
			map.put("roleName", "zhaohui");
			map.put("note", "hello");
			Role role = mapper.findRoleByMap(map);
			System.out.println("findRoleByMap-->" + role.toString());
		} finally {
			session3.close();
		}

		SqlSession session4 = sqlSessionFactory.openSession();
		try {
			RoleMapper mapper = session4.getMapper(RoleMapper.class);
			Role role = mapper.findRoleByAnnotation("zhaohui", "hello");
			System.out.println("findRoleByAnnotation-->" + role.toString());
		} finally {
			session4.close();
		}
		
		
		SqlSession session5 = sqlSessionFactory.openSession();
		try {
			Role role =new Role();
			role.setRoleName("kitty");
			role.setNote("world");
			RoleMapper mapper = session5.getMapper(RoleMapper.class);
			
			mapper.insertRole(role);
			session5.commit();
			//role主键自动回填了，可以输出id
			System.out.println("insertRole-->"+role.toString());
		} finally {
			session5.close();
		}

	}

}
