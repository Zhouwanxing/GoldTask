package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.GameScoreEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GameScoreRepository extends MongoRepository<GameScoreEntity, String> {
    @Query("{'game':'?0','sid':'?1'}")
    GameScoreEntity findByGameAndSid(String game, String sid);

    /** 分数降序、同分先到的排前;条数由 Pageable 限制(排行榜取前 2)。 */
    @Query(value = "{'game':'?0'}", sort = "{'score':-1,'updatedAt':1}")
    List<GameScoreEntity> findTop(String game, Pageable pageable);
}
