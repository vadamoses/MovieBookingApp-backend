package com.moviebookingapp.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableMongoRepositories(basePackages = "com.moviebookingapp.repositories")
public class MongoDBConfiguration {
	
	@Value("${spring.data.mongodb.uri}")
    private String uri;
	
	@Value("${spring.data.mongodb.database}")
    private String database;

	
    @Bean
    MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(uri);
        
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(20)
                        .minSize(10)
                        .maxWaitTime(2000, TimeUnit.MILLISECONDS)
                        .build())
                .applyToClusterSettings(builder -> builder
                        .serverSelectionTimeout(5000, TimeUnit.MILLISECONDS)
                        .build())
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, database);
    }
}
