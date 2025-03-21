package com.example.user.service;

import com.example.common.model.Result;
import com.example.user.pojo.User;

import java.util.List;

public interface UserService {
    Result<User> createUser(User user);
    Result<Void> deleteUser(Long id);
    Result<User> updateUser(User user);
    Result<User> getUserById(Long id);
    Result<User> getUserByUsername(String username);
    Result<User> getUserByEmail(String email);
    Result<List<User>> getAllUsers();
}
