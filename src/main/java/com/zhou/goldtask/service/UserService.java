package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.UserEntity;
import com.zhou.goldtask.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {
    @Resource
    private UserRepository userRepository;


    public boolean isExistUser(String userName, String password) {
        UserEntity user = userRepository.findById(userName).orElse(null);
        return user != null && user.getPassword().equals(password);
    }
}