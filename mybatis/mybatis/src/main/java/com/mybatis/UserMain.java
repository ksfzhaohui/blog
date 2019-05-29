package com.mybatis;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.UserMapper;
import com.mybatis.vo.Sex;
import com.mybatis.vo.User;

public class UserMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		// 方式一
		SqlSession session1 = sqlSessionFactory.openSession();
		User user = new User();
		user.setId(102);
		user.setSex(Sex.FEMALE);
		user.setName("zhaohui");
		try {
			session1.insert("insertUser", user);
			session1.commit();
		} finally {
			session1.close();
		}

		SqlSession session2 = sqlSessionFactory.openSession();
		try {
			UserMapper userMapper = session2.getMapper(UserMapper.class);
			User user2 = userMapper.getUser(102);
			System.out.println(user2.toString());
		} finally {
			session2.close();
		}

	}
}
