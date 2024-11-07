package com.academy.creator_hub.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "videos")
@Getter
@NoArgsConstructor
public class Videos {
    @Id
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
    private List<String> tags;

    public Videos(String videoId, String title, String description, String thumbnailUrl,
                  BigInteger viewCount, BigInteger likeCount, BigInteger commentCount, LocalDateTime publishedAt,String channelId, String channelTitle,
                  String categoryId, String duration, List<String> tags) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.publishedAt = publishedAt;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.categoryId = categoryId;
        this.duration = duration;
        this.tags = tags;
    }
}
