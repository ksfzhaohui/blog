package com.zh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zh.domain.User;

public interface UserDao extends JpaRepository<User, Long> {

	User findByUsername(String username);
}
