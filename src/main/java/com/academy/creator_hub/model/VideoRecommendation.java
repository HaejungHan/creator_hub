package com.academy.creator_hub.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Getter
@Document(collection = "user_video_recommendations")
public class VideoRecommendation {

    @Id
    private String id;
    private String username;
    private String videoId;
    private double similarity;

    public VideoRecommendation(String username, String videoId, double similarity) {
        this.username = username;
        this.videoId = videoId;
        this.similarity = similarity;
    }
}
