package com.zhou.goldtask.service;

import cn.hutool.json.JSONUtil;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zhou.goldtask.entity.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Service
@Slf4j
public class MongoService {
    @Resource
    private EnvConfig envConfig;
    private String dbName = null;

    private MongoClient getMongoClient() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        ConnectionString connectionString = new ConnectionString(envConfig.getMongoUri());
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(connectionString).build();
        dbName = connectionString.getDatabase();
        try {
            return MongoClients.create(settings);
        } catch (MongoException me) {
            log.warn("", me);
            return null;
        }
    }

    public <T> boolean saveOne(String tableName, Object entity, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return false;
        }
        try {
            T obj = JSONUtil.parse(entity).toBean(tClass);
            client.getDatabase(dbName).getCollection(tableName, tClass).insertOne(obj);
        } catch (Exception e) {
            log.warn("", e);
            return false;
        } finally {
            client.close();
        }
        return true;
    }
}