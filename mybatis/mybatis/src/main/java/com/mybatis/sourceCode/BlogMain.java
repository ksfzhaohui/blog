package com.mybatis.sourceCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.BlogMapper;
import com.mybatis.vo.Blog;
import com.mybatis.vo.BlogNew;

public class BlogMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config-sourceCode.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		SqlSession session = sqlSessionFactory.openSession();
		try {
			// select(session);
//			selectHandler(session, sqlSessionFactory.getConfiguration());
			selectMyHandler(session);
			// insert(session);
			// update(session);
		} finally {
			session.close();
		}
	}

	public static void select(SqlSession session) {
		BlogMapper<Blog> mapper = session.getMapper(BlogMapper.class);
		// 常规方法
		// System.out.println(mapper.selectBlog3(158,"zhaohui"));
		// // Object的方法
		// System.out.println(mapper.hashCode());
		// // public default方法
		// System.out.println(mapper.defaultValue());
		// // 父接口中的方法
		// System.out.println(mapper.selectParent(158));
		// System.out.println(mapper.selectBlog(158));
		System.out.println(mapper.selectBlogMap2(158, "zhaohui"));
		// System.out.println(mapper.selectBlogsArray("zhaohui"));
	}

	public static void selectHandler(SqlSession session, Configuration configuration) {
		BlogMapper mapper = session.getMapper(BlogMapper.class);
		// DefaultResultHandler内置结果处理器
		DefaultResultHandler defaultHandler = new DefaultResultHandler();
		// System.out.println(mapper.selectBlogsByHandler("zhaohui",
		// defaultHandler));
		System.out.println(defaultHandler.getResultList());

		// DefaultMapResultHandler内置结果处理器
		DefaultMapResultHandler<Long, Blog> defaultMapResultHandler = new DefaultMapResultHandler<Long, Blog>("id",
				configuration.getObjectFactory(), configuration.getObjectWrapperFactory(),
				configuration.getReflectorFactory());
		mapper.selectBlogsByHandler("zhaohui", defaultMapResultHandler);
		System.out.println(defaultMapResultHandler.getMappedResults());
	}

	public static void selectMyHandler(SqlSession session) {
		BlogMapper mapper = session.getMapper(BlogMapper.class);
		MyResultHandler handler = new MyResultHandler();
		mapper.selectBlogsByHandler("zhaohui", handler);
		System.out.println(handler.getResult());
	}

	public static void insert(SqlSession session) throws IOException {

		BlogMapper mapper = session.getMapper(BlogMapper.class);
		Blog blog = new Blog();
		blog.setTitle("hello java");
		// blog.setAuthor("zhaohui");
		blog.setContent("hello java666");
		mapper.insertBlog(blog);
		session.commit();
		System.out.println(blog.toString());
	}

	public static void update(SqlSession session) {
		BlogMapper mapper = session.getMapper(BlogMapper.class);
		Blog blog = new Blog();
		blog.setId(158);
		blog.setTitle("hello java new");

		mapper.updateBlog(blog);
		session.commit();
	}
}
