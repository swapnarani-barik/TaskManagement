package com.example.taskflow.service;

import com.example.taskflow.domain.User;

public interface UserService {
    User register(String fullName, String email, String rawPassword);
    User findByEmail(String email);
}