package com.example.Demo.service;

import java.util.List;

import com.example.Demo.model.User;

public interface UserService {

    User saveUser(User user);
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll();
}
