package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.Mp4Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class Mp4Service {
    @Resource
    private MongoTemplate mongoTemplate;

    public Mp4Entity findOne() {
        return mongoTemplate.findOne(new Query(), Mp4Entity.class);
    }

    public void save(Mp4Entity mp4Entity) {
        mongoTemplate.save(mp4Entity);
    }
}
