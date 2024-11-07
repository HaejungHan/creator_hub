package com.academy.creator_hub.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Value("${spring.data.mongodb.uri}") // application.properties에서 Mongo URI를 읽어옵니다.
    private String mongoUri;

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("YouTube Analysis")
                .master("local[*]")
                .config("spark.mongodb.input.uri", mongoUri + ".inputCollection")
                .config("spark.mongodb.output.uri", mongoUri + ".outputCollection")
                .getOrCreate();
    }
}
