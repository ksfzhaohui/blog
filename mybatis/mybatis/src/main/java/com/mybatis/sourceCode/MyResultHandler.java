package com.mybatis.sourceCode;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import com.mybatis.vo.Blog;

public class MyResultHandler implements ResultHandler<Blog> {

	Map<Long, Blog> result = new HashMap<Long, Blog>();

	@Override
	public void handleResult(ResultContext<? extends Blog> resultContext) {
		Blog blog = resultContext.getResultObject();
		System.out.println(blog.toString());
		result.put(blog.getId(), blog);
	}

	public Map<Long, Blog> getResult() {
		return result;
	}

}
