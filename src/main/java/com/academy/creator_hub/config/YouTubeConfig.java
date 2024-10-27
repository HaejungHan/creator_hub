package com.academy.creator_hub.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class YouTubeConfig {

    private static final String APPLICATION_NAME = "creator_hub";

    @Bean
    public YouTube youtube(@Value("${youtube.api.key}") String apiKey) throws GeneralSecurityException, IOException {
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null)
                .setApplicationName(APPLICATION_NAME)
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey)) // API 키 설정
                .build();
    }
}
