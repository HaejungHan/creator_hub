package com.academy.creator_hub.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    private static SparkSession sparkSession;

    @Bean
    public SparkSession sparkSession() {
        if (sparkSession == null) {
            sparkSession = SparkSession.builder()
                    .appName("YourAppName")
                    .master("local[*]")
                    .getOrCreate();
        }
        return sparkSession;
    }
}
