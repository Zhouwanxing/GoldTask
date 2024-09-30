package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.Mp4Entity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Mp4Repository extends MongoRepository<Mp4Entity, String> {
}