package com.mybatis;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.BlogMapper;
import com.mybatis.vo.Blog;

public class BlogLimitPluginMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		SqlSession session2 = sqlSessionFactory.openSession();
		try {
			Blog blog = new Blog();
			blog.setTitle("new_java");
			blog.setId(101);
			BlogMapper mapper = session2.getMapper(BlogMapper.class);
			mapper.updateBlog(blog);

			session2.commit();
		} finally {
			session2.close();
		}

	}
}
