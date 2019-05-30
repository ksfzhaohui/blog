package com.mybatis;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mybatis.mapper.StudentMapper;
import com.mybatis.vo.Student;

public class StudentMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		SqlSession session1 = sqlSessionFactory.openSession();
		try {
			StudentMapper mapper = session1.getMapper(StudentMapper.class);
			Student student = mapper.selectStudent(111);
			System.out.println(student.toString());
			//默认开启一级缓存，在参数和sql相同的情况下，只执行一次sql
			Student student2 = mapper.selectStudent(111);
			System.out.println(student2.toString());
		} finally {
			session1.close();
		}
		
		//不同的session会被隔离，所以会执行sql
		SqlSession session2 = sqlSessionFactory.openSession();
		try {
			StudentMapper mapper = session2.getMapper(StudentMapper.class);
			Student student = mapper.selectStudent(111);
			System.out.println(student.toString());
		} finally {
			session2.close();
		}

	}
}
