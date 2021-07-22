package com.itorix.apiwiz.test.config;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.test.security.MultiTenantMongoDbFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

@Configuration
public class MultiTenantMongoDbConfiguration {

    @Autowired(required = false)
    private MongoClientOptions options;

    @Autowired
    private Environment environment;

    @Autowired
    private MongoProperties properties;

    @Bean
    public MongoClient createMongoClient() throws UnknownHostException {
        return properties.createMongoClient(options, environment);
    }

    @Primary
    @Bean
    public MongoDbFactory multitenantFactory() throws UnknownHostException {
        String dbName = properties.getDatabase();
        if (!StringUtils.hasText(dbName)) {
            dbName = properties.getUri().substring(properties.getUri().lastIndexOf("/") + 1,
                    properties.getUri().length());
        }
        return new MultiTenantMongoDbFactory(createMongoClient(), dbName);
    }

    @Primary
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(multitenantFactory());
    }

    @Bean(name = "masterMongoTemplate")
    public MongoTemplate secondaryMongoTemplate() throws Exception {
        return new MongoTemplate(masterFactory());
    }

    @Bean
    public MongoDbFactory masterFactory() throws Exception {
        String dbName = properties.getDatabase();
        if (!StringUtils.hasText(dbName)) {
            dbName = properties.getUri().substring(properties.getUri().lastIndexOf("/") + 1,
                    properties.getUri().length());
        }
        return new SimpleMongoDbFactory(createMongoClient(), dbName);
    }

}