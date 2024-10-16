package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.GoldEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GoldRepository extends MongoRepository<GoldEntity, String> {
    @Query(value = "{}", sort = "{'_id':1}")
    List<GoldEntity> findAll();

    @Query("{'_id':'?0'}")
    GoldEntity findItemById(String _id);
}