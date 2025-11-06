package com.zhou.goldtask.config;

import cn.hutool.extra.spring.SpringUtil;
import com.mongodb.client.result.UpdateResult;
import com.zhou.goldtask.entity.MagicConfigEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.resource.KeyValueResource;
import org.ssssssss.magicapi.core.resource.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@Component
public class CustomResource extends KeyValueResource {
    private final MongoTemplate mongoTemplate;

    private Map<String, String> cachedContent = new ConcurrentHashMap<>();

    public CustomResource() {
        this(SpringUtil.getBean("mongoTemplate", MongoTemplate.class), "", false, null);
    }

    public CustomResource(MongoTemplate mongoTemplate, String path, boolean readonly, KeyValueResource parent) {
        super(":", path, readonly, parent);
        this.mongoTemplate = mongoTemplate;
    }

    public CustomResource(MongoTemplate mongoTemplate, String path, boolean readonly, Map<String, String> cachedContent, KeyValueResource parent) {
        this(mongoTemplate, path, readonly, parent);
        this.cachedContent = cachedContent;
    }

    /**
     * 需要做修改的key，原key: 新key
     *
     * @param renameKeys 需重命名的key
     * @return 是否修改成功
     */
    @Override
    protected boolean renameTo(Map<String, String> renameKeys) {
        Query query;
        Update update;
        for (String key : renameKeys.keySet()) {
            query = new Query();
            update = new Update();
            query.addCriteria(Criteria.where("file_path").is(key));
            update.set("file_path", renameKeys.get(key));
            mongoTemplate.updateFirst(query, update, MagicConfigEntity.class);
            this.cachedContent.put(renameKeys.get(key), this.cachedContent.remove(key));
        }
        return true;
    }


    /**
     * mapped函数，用于根据路径创建资源对象
     *
     * @return mapped函数
     */
    @Override
    protected Function<String, Resource> mappedFunction() {
        return it -> new CustomResource(mongoTemplate, it, readonly, this.cachedContent, this);
    }

    /**
     * 该资源下的keys
     *
     * @return 返回该资源下的keys
     */
    @Override
    protected Set<String> keys() {
        String prefix = isDirectory() ? this.path : (this.path + separator);
        if (!cachedContent.isEmpty()) {
            return cachedContent.keySet().stream().filter(it -> it.startsWith(prefix)).collect(Collectors.toSet());
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("file_path").regex(prefix));
        return new HashSet<>(mongoTemplate.findDistinct(query, "file_path", MagicConfigEntity.class, String.class));
    }

    /**
     * 判断是否存在
     *
     * @return 返回资源是否存在
     */
    @Override
    public boolean exists() {
        if (this.cachedContent.get(this.path) != null) {
            return true;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("file_path").is(this.path));
        return mongoTemplate.exists(query, MagicConfigEntity.class);
    }

    /**
     * 写入
     *
     * @param content 写入的内容
     * @return 是否写入成功
     */
    @Override
    public boolean write(String content) {
        if (exists()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("file_path").is(this.path));
            Update update = new Update().set("file_content", content);
            UpdateResult result = mongoTemplate.updateFirst(query, update, MagicConfigEntity.class);
            if (result.getMatchedCount() > 0) {
                this.cachedContent.put(this.path, content);
                return true;
            }
        }
        MagicConfigEntity entity = new MagicConfigEntity(this.path, content);
        try {
            mongoTemplate.insert(entity);
            this.cachedContent.put(this.path, content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 处理导出
     *
     * @param zos       zip 输出流
     * @param path      路径
     * @param directory 目录资源对象
     * @param resources 资源集合
     * @param excludes  排除的目录
     * @throws IOException 处理过程中抛出的异常
     */
    @Override
    public void processExport(ZipOutputStream zos, String path, Resource directory, List<Resource> resources, List<String> excludes) throws IOException {
        super.processExport(zos, path, directory, resources, excludes);
    }

    /**
     * 处理导出
     *
     * @param os       输出流
     * @param excludes 排除的目录
     * @throws IOException 处理过程中抛出的异常
     */
    @Override
    public void export(OutputStream os, String... excludes) throws IOException {
        super.export(os, excludes);
    }

    /**
     * 读取
     *
     * @return 读取的资源内容
     */
    @Override
    public byte[] read() {
        String value = this.cachedContent.get(path);
        if (value == null) {
            Query query = new Query();
            query.addCriteria(Criteria.where("file_path").is(this.path));
            MagicConfigEntity entity = mongoTemplate.findOne(query, MagicConfigEntity.class);
            if (entity != null) {
                value = entity.getFile_content();
                this.cachedContent.put(path, value);
            }
        }
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 读取当前资源下的所有内容,主要是缓存作用。
     */
    @Override
    public void readAll() {
        this.cachedContent.entrySet().removeIf(entry -> entry.getKey().startsWith(path));
        Query query = new Query();
        query.addCriteria(Criteria.where("file_path").regex(this.path));
        List<MagicConfigEntity> list = mongoTemplate.find(query, MagicConfigEntity.class);
        for (MagicConfigEntity entity : list) {
            this.cachedContent.put(entity.getFile_path(), entity.getFile_content());
        }
    }
}