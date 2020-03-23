package com.example.demo.logic;

import com.example.demo.data.repository.UserRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    public void saveUserInfo(User userForm) {
        userRepository.save(userForm);
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public User getUserByName(String name){ return userRepository.findByUserName(name); }

    public void removeUser(int id) {
        userRepository.deleteById(id);
    }
}