package com.academy.creator_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.academy.creator_hub.repository")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CreatorHubApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(CreatorHubApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CreatorHubApplication.class);
    }

}

