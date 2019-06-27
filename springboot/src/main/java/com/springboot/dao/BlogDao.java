package com.springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.entity.Blog;

@Repository
public interface BlogDao extends JpaRepository<Blog, Long> {

	
}
