package com.academy.creator_hub.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Bean
    public SparkSession sparkSession() {
        // SparkSession을 설정하는 부분, MongoDB 관련 설정은 직접 메서드에서 처리되므로 이 부분은 간단히 설정합니다.
        return SparkSession.builder()
                .appName("YouTube Analysis")  // 애플리케이션 이름
                .master("local[*]")  // 로컬 모드에서 실행 (전체 CPU 사용)
                .getOrCreate();
    }
}
