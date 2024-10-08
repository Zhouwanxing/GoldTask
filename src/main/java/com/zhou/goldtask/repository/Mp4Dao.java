package com.zhou.goldtask.repository;

import com.zhou.goldtask.entity.DevEntity;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.entity.Mp4Entity;
import com.zhou.goldtask.utils.Utils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class Mp4Dao {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private EnvConfig envConfig;

    private Query findBaseQuery(boolean isShowLike) {
        Query query = new Query();
        if (isShowLike) {
            query.addCriteria(Criteria.where("like").is(true));
        } else {
            query.addCriteria(Criteria.where("like").isNull());
        }
        return query;
    }

    public List<Mp4Entity> findByPage(int page, boolean isShowLike) {
        Query query = findBaseQuery(isShowLike);
        query.with(Sort.by(Sort.Direction.DESC, "date", "path"));
        query.skip((page - 1) * 10L);
        query.limit(10);
        if (Utils.localhost.equals(envConfig.getHostName())) {
            query.fields().exclude("name", "img");
        }
        return mongoTemplate.find(query, Mp4Entity.class);
    }

    public boolean isMyDev(String devId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(devId));
        return mongoTemplate.count(query, DevEntity.class) > 0;
    }

    public long count(boolean isShowLike) {
        return mongoTemplate.count(findBaseQuery(isShowLike), Mp4Entity.class);
    }

    public void updateLike(String id, boolean isLike) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        mongoTemplate.updateFirst(query, new Update().set("like", isLike), Mp4Entity.class);
    }
}
