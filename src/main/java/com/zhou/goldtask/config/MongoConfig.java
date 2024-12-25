package com.zhou.goldtask.config;

import cn.hutool.core.util.StrUtil;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Objects;

@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.secondary-uri:#{null}}")
    private String secondMongoUri;

    @Value("${spring.data.mongodb.uri:#{null}}")
    private String mongoUri;

    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MongoMappingContext mongoMappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter mappingMongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDatabaseFactory, mappingMongoConverter);
    }


    @Bean(name = "secondMongoTemplate")
    public MongoTemplate secondMongoTemplate(MongoMappingContext context) throws Exception {
        ConnectionString connectionString = new ConnectionString(StrUtil.isNotBlank(secondMongoUri) ? secondMongoUri : mongoUri);
        SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(MongoClients.create(connectionString), Objects.requireNonNull(connectionString.getDatabase()));
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), context);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(factory, converter);
    }
}