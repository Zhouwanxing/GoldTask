package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.Mp4Entity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface Mp4Repository extends MongoRepository<Mp4Entity, String> {
    @Query("{'name':'?0'}")
    Mp4Entity findItemByName(String name);

    @Query(value = "{'category':'?0'}", fields = "{'name' : 1, 'quantity' : 1}")
    List<Mp4Entity> findAll(String category);

}