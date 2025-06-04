package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.UrlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UrlRepository extends MongoRepository<UrlEntity, String> {
    @Query(value = "{}", sort = "{'date':-1}", fields = "{'_id':1}")
    List<UrlEntity> findAllSort();

    @Query(value = "{}", sort = "{'date':-1}", fields = "{'_id':1}")
    Page<UrlEntity> findSort(Pageable pageable);

    // 直接在Repository中添加分页查询方法
    @Query(value = "{}", sort = "{'date':-1}", fields = "{'_id':1}")
    Page<UrlEntity> findAllSort(Pageable pageable);
}