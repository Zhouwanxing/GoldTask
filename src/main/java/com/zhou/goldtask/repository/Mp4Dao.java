package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.Mp4Entity;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class Mp4Dao {
    @Resource
    private MongoTemplate mongoTemplate;

    public List<Mp4Entity> findByPage(int page) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "date", "path"));
        query.skip((page - 1) * 10L);
        query.limit(10);
        return mongoTemplate.find(query, Mp4Entity.class);
    }
}
