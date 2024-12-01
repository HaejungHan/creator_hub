package com.academy.creator_hub.config.spark;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Bean(destroyMethod = "close")  // `close` 메서드를 자동으로 호출
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("Creator_hub")
                .master("local[*]")
                .getOrCreate();
    }
}
