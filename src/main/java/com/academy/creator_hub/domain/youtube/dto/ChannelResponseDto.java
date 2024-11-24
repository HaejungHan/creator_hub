package com.academy.creator_hub.domain.youtube.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChannelResponseDto {

    private String channelName;
    private String description;
    private String customUrl;
    private String publishedAt;
    private String viewCount;
    private String subscriberCount;
    private String videoCount;
    private String keywords;

    public ChannelResponseDto(String description, String channelName, String customUrl, String publishedAt,
                              String viewCount, String subscriberCount, String videoCount, String keywords) {
        this.channelName = channelName;
        this.description = description;
        this.customUrl = customUrl;
        this.publishedAt = publishedAt;
        this.viewCount = viewCount;
        this.subscriberCount = subscriberCount;
        this.videoCount = videoCount;
        this.keywords = keywords;
    }
}