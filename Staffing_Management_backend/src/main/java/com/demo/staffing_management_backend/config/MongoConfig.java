package com.demo.staffing_management_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Enables multi-document transactions so service methods that touch several collections
 * (e.g. creating/deleting an employee together with its login and dependents) commit or
 * roll back atomically.
 *
 * <p>MongoDB transactions require the server to be a replica set. MongoDB Atlas clusters are
 * always replica sets, so this works out of the box in every deployed environment. A local
 * standalone {@code mongod} does not support transactions — run it as a single-node replica set
 * for local development if you exercise the transactional paths.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }
}
