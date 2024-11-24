package com.academy.creator_hub.config.spark;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("Creator_hub")
                .master("local[*]")  // 로컬 모드에서 실행
                .getOrCreate();
    }
}
