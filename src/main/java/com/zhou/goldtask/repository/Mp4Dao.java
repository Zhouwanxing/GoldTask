package com.zhou.goldtask.repository;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zhou.goldtask.entity.*;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Mp4Dao {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private MongoTemplate secondMongoTemplate;
    @Resource
    private EnvConfig envConfig;
    @Resource
    private UrlRepository urlRepository;

    private Query findBaseQuery(boolean isShowLike, String path) {
        Query query = new Query();
        if (isShowLike) {
            query.addCriteria(Criteria.where("like").is(true));
        } else {
            query.addCriteria(Criteria.where("like").isNull());
        }
        if (!(path == null || "all".equals(path))) {
            query.addCriteria(Criteria.where("path").is(path));
        }
        return query;
    }

    public List<Mp4Entity> findByPage(int page, boolean isShowLike, String path) {
        Query query = findBaseQuery(isShowLike, path);
        query.with(Sort.by(Sort.Direction.DESC, "date", "path", "_id"));
        query.skip((page - 1) * 10L);
        query.limit(10);
        if (Utils.localhost.equals(envConfig.getHostName())) {
            query.fields().exclude("name", "img");
        }
        return mongoTemplate.find(query, Mp4Entity.class);
    }

    public long count(boolean isShowLike, String path) {
        return mongoTemplate.count(findBaseQuery(isShowLike, path), Mp4Entity.class);
    }

    public void updateLike(String id, String flag) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        boolean isLike = !"delete".equals(flag);
        Update like = new Update().set("like", isLike);
        if (!isLike) {
            like.unset("url");
            like.unset("date");
            like.unset("insertTime");
            like.unset("name");
        } else {
            like.set("lut", DateUtil.now());
            like.set("flag", flag);
        }
        mongoTemplate.updateFirst(query, like, Mp4Entity.class);
    }

    public List<String> getAllPath() {
        return mongoTemplate.findDistinct("path", Mp4Entity.class, String.class);
    }

    public List<Mp4Entity> findByDto(Mp4LikeDto dto) {
        Query query = searchLikeQuery(dto);
        query.with(Sort.by(Sort.Direction.DESC, "lut", "date", "path", "_id"));
        query.skip((dto.getPage() - 1) * 10L);
        query.limit(10);
        if (Utils.localhost.equals(envConfig.getHostName())) {
            query.fields().exclude("name", "img");
        }
        List<Mp4Entity> list = mongoTemplate.find(query, Mp4Entity.class);
        log.info("{}\n{}", query, list.size());
        return list;
    }

    private Query searchLikeQuery(Mp4LikeDto dto) {
        Query query = findBaseQuery(true, null);
        if (!StrUtil.isEmptyIfStr(dto.getShowBest())) {
            query.addCriteria(Criteria.where("flag").is(dto.getShowBest()));
        }
        if (!StrUtil.isEmptyIfStr(dto.getBeforeMonth())) {
            query.addCriteria(Criteria.where("date").lt(dto.getBeforeMonth()));
        }
        if (!(dto.getPath() == null || "all".equals(dto.getPath()))) {
            query.addCriteria(Criteria.where("path").is(dto.getPath()));
        }
        return query;
    }

    public long searchLikeCount(Mp4LikeDto dto) {
        Query query = searchLikeQuery(dto);
        return mongoTemplate.count(query, Mp4Entity.class);
    }

    public List<String> distinctPath() {
        return mongoTemplate.findDistinct("path", Mp4Entity.class, String.class);
    }

    public List<String> getUrls() {
        return urlRepository.findAllSort(Pageable.ofSize(20))
                .getContent().stream().map(UrlEntity::get_id).collect(Collectors.toList());
//        return urlRepository.findAllSort().stream().map(UrlEntity::get_id).collect(Collectors.toList());
    }

    public List<PathCountEntity> getAllCountAndPath() {
        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("like").isNull()),
                    Aggregation.group("path").count().as("count"),
                    Aggregation.sort(Sort.Direction.DESC, "count")
            );
            return mongoTemplate.aggregate(
                    aggregation,
                    Mp4Entity.class,
                    PathCountEntity.class
            ).getMappedResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Mp4ConfigEntity getMp4Config() {
        return mongoTemplate.findOne(
                new Query().addCriteria(Criteria.where("_id").is("mp4_config")),
                Mp4ConfigEntity.class, "system_config");
    }
}
