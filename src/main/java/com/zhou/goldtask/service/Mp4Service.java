package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.Mp4Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class Mp4Service {
    @Resource
    private MongoService mongoService;

    public Mp4Entity findOne() {
        return null;
//        return mongoTemplate.findOne(new Query(), Mp4Entity.class);
    }

    public boolean save(Mp4Entity mp4Entity) {
        return mongoService.saveOne("my_mp4", mp4Entity, Mp4Entity.class);
    }
}
