package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.MomentMediaClaim;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MomentMediaClaimRepository extends MongoRepository<MomentMediaClaim, String> {
    void deleteByFileIdIn(List<String> fileIds);
}
