package com.mybatis;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.BlogMapper;
import com.mybatis.vo.Blog;

public class Main {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		//方式一
		SqlSession session1 = sqlSessionFactory.openSession();
		try {
			Blog blog = (Blog) session1.selectOne("com.mybatis.mapper.BlogMapper.selectBlog", 101l);
			System.out.println(blog.toString());
		} finally {
			session1.close();
		}

		//方式二
		SqlSession session2 = sqlSessionFactory.openSession();
		try {
			BlogMapper mapper = session2.getMapper(BlogMapper.class);
			Blog blog = mapper.selectBlog(101);
			System.out.println(blog.toString());
		} finally {
			session2.close();
		}
	}
}
