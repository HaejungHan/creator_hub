package com.academy.creator_hub.entity;

import com.google.api.client.util.DateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.math.BigInteger;

@Document(collation = "videos")
@Getter
@NoArgsConstructor
public class Videos {
    @Id
    private String videoId;
    private String title;
    private String thumbnailUrl;
    private BigInteger viewCount;
    private DateTime publishedAt;
    private String duration;
    private String videoUrl;

    public Videos(String videoId, String title, String thumbnailUrl, BigInteger viewCount,
                 DateTime publishedAt, String duration, String videoUrl) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.viewCount = viewCount;
        this.publishedAt = publishedAt;
        this.duration = duration;
        this.videoUrl = videoUrl;
    }
}
