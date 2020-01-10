package com.example.demo.logic;

import com.example.demo.data.repository.UserRepository;

import org.springframework.stereotype.Component;

import java.util.List;

import com.example.demo.data.model.User;

@Component
public class UserManager {
    private UserRepository userRepository;

    public UserManager(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}