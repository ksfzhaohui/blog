package com.mybatis.sourceCode;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.BlogMapper;
import com.mybatis.vo.Blog;

public class CacheBlogMain3 {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config-sourceCode.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		SqlSession session = sqlSessionFactory.openSession();

		new Thread(new Runnable() {

			@Override
			public void run() {
				BlogMapper<Blog> mapper = session.getMapper(BlogMapper.class);
				System.out.println(mapper.selectBlog(160));
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				BlogMapper<Blog> mapper = session.getMapper(BlogMapper.class);
				System.out.println(mapper.selectBlog(160));
			}
		}).start();

	}

}
