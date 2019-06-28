package com.springboot.dao;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.entity.Blog;

@Repository
@CacheConfig(cacheNames = "blogs")
public interface BlogDao extends JpaRepository<Blog, Long> {


    @Cacheable(value = "blog", key = "#p0")//3
	Blog findById(long id);
}
