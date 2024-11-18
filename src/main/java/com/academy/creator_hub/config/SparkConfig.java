package com.academy.creator_hub.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("Creator_hub")  // 애플리케이션 이름
                .master("local[*]")  // 로컬 모드에서 실행 (전체 CPU 사용)
                .getOrCreate();
    }
}
