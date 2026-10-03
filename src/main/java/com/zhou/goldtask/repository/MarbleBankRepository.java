package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.MarbleBankEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MarbleBankRepository extends MongoRepository<MarbleBankEntity, String> {
}
