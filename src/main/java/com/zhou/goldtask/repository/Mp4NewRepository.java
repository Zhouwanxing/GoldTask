package com.zhou.goldtask.repository;


import com.zhou.goldtask.entity.Mp4NewEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Mp4NewRepository extends MongoRepository<Mp4NewEntity, String> {
    long countByClassid(int classid);
}
