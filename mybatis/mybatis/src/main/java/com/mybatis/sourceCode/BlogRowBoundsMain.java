package com.mybatis.sourceCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.BlogMapper;
import com.mybatis.vo.Blog;
import com.mybatis.vo.BlogNew;

public class BlogRowBoundsMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config-sourceCode.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		SqlSession session = sqlSessionFactory.openSession();
		try {
			BlogMapper<Blog> mapper = session.getMapper(BlogMapper.class);
			int pageSize = 50;
			int totalCount = 55;
			int loopCount = (totalCount % pageSize == 0) ? totalCount / pageSize : totalCount / pageSize + 1;
			for (int i = 1; i <= loopCount; i++) {
				List<Blog> blogs = mapper.selectBlogs("ksfzhaohui", new RowBounds(i, pageSize));
				for (Blog b : blogs) {
					System.out.println(b);
				}
				System.out.println("============================");
			}
		} finally {
			session.close();
		}
	}

}
