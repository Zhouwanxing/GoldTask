package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.UserEntity;
import com.zhou.goldtask.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserService {
    @Resource
    private UserRepository userRepository;

    public void mongoTest() {
        long count = userRepository.count();
//        log.info("mongo 检测:" + count);
    }


    public boolean isExistUser(String userName, String password) {
        UserEntity user = userRepository.findById(userName).orElse(null);
        return user != null && user.getPassword().equals(password);
    }
}