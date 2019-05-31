package com.mybatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.BlogMapper;
import com.mybatis.vo.Blog;

public class BlogPluginMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		SqlSession session2 = sqlSessionFactory.openSession();
		try {
			BlogMapper mapper = session2.getMapper(BlogMapper.class);
			List<Blog> blogs = mapper.selectBlogs("ksfzhaohui");
			//最大只会查出50条  select * from (select * from blog where author = ?) limit_table_name_blog limit 50 
			System.out.println(blogs.size());
		} finally {
			session2.close();
		}

	}
}
