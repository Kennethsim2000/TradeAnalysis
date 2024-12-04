package com.example.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
