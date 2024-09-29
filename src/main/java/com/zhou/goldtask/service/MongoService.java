package com.zhou.goldtask.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zhou.goldtask.annotation.TableName;
import com.zhou.goldtask.entity.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

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

    public <T> boolean saveOne(T entity, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return false;
        }
        try {
            client.getDatabase(dbName).getCollection(tClass.getAnnotation(TableName.class).value())
                    .insertOne(Document.parse(JSONUtil.toJsonStr(entity)));
        } catch (Exception e) {
            log.warn("", e);
            return false;
        } finally {
            client.close();
        }
        return true;
    }

    public <T> T findOneById(String _id, Class<T> tClass) {
        return findOneByCon(idToJson(_id), tClass);
    }

    public <T> T findOneByCon(JSONObject condition, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return null;
        }
        try {
            return client.getDatabase(dbName).getCollection(tClass.getAnnotation(TableName.class).value(), tClass).find(Document.parse(condition.toString())).first();
        } catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }

    public <T> List<T> findListByCon(JSONObject condition, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return new ArrayList<>();
        }
        try {
            return client.getDatabase(dbName).getCollection(tClass.getAnnotation(TableName.class).value(), tClass).find(Document.parse(condition.toString())).into(new ArrayList<>());
        } catch (Exception e) {
            log.warn("", e);
            return new ArrayList<>();
        }
    }

    private JSONObject idToJson(Object _id) {
        return new JSONObject().putOpt("_id", _id);
    }

    public <T> void updateOneById(String _id, JSONObject setData, Class<T> tClass) {
        updateOneByCon(idToJson(_id), setData, tClass);
    }

    public <T> void updateOneByCon(JSONObject con, JSONObject setData, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return;
        }
        try {
            client.getDatabase(dbName).getCollection(tClass.getAnnotation(TableName.class).value(), tClass)
                    .updateOne(Document.parse(con.toString()), Document.parse(setData.toString()));
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public <T> void updateAllByCon(JSONObject con, JSONObject setData, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return;
        }
        try {
            client.getDatabase(dbName).getCollection(tClass.getAnnotation(TableName.class).value(), tClass)
                    .updateMany(Document.parse(con.toString()), Document.parse(setData.toString()));
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public <T> void deleteOneById(String _id, Class<T> tClass) {
        deleteOneByCon(idToJson(_id), tClass);
    }

    private <T> void deleteOneByCon(JSONObject idToJson, Class<T> tClass) {
        MongoClient client = getMongoClient();
        if (client == null) {
            return;
        }
        try {
            client.getDatabase(dbName).getCollection(tClass.getAnnotation(TableName.class).value(), tClass)
                    .deleteMany(Document.parse(idToJson.toString()));
        } catch (Exception e) {
            log.warn("", e);
        }
    }
}