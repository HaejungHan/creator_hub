package com.academy.creator_hub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;
    private LocalDateTime publishedAt;
    private String channelId;
    private String channelTitle;
    private String categoryId;
    private String duration;

}
