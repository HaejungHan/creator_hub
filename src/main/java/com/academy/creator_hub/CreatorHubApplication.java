package com.academy.creator_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableMongoRepositories(basePackages =
        { "com.academy.creator_hub.domain.auth.repository", "com.academy.creator_hub.domain.youtube.repository"})
@EnableMongoAuditing
@EnableScheduling
@SpringBootApplication
public class CreatorHubApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(CreatorHubApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CreatorHubApplication.class);
    }
}

