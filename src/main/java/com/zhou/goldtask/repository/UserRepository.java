package com.zhou.goldtask.repository;


import com.zhou.goldtask.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserEntity, String> {
}