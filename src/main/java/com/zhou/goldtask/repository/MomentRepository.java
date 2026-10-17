package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.MomentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MomentRepository extends MongoRepository<MomentEntity, String> {
    List<MomentEntity> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    long countByUsername(String username);
}
