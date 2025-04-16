package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.TangYueEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class TangYueDao {
    @Resource
    private MongoTemplate secondMongoTemplate;

    public void countTest(){
        secondMongoTemplate.count(new Query(), TangYueEntity.class);
    }

    public List<TangYueEntity> findList(TangYueEntity con) {
        Query query = new Query();
        if (StringUtils.isNotBlank(con.getBuilding())) {
            query.addCriteria(Criteria.where("building").is(con.getBuilding()));
        } else {
            query.addCriteria(Criteria.where("building").is("A7"));
        }
        if (StringUtils.isNotBlank(con.getUnit())) {
            query.addCriteria(Criteria.where("unit").is(con.getUnit()));
        } else {
            query.addCriteria(Criteria.where("unit").is("1"));
        }
        if (StringUtils.isNotBlank(con.getXq())) {
            query.addCriteria(Criteria.where("xq").is(con.getXq()));
        } else {
            query.addCriteria(Criteria.where("xq").is("ty"));
        }
        query.with(Sort.by(Sort.Direction.ASC, "floor", "room"));
        query.fields().exclude("_id", "areaPredict", "xq");
        return secondMongoTemplate.find(query, TangYueEntity.class);
    }
}