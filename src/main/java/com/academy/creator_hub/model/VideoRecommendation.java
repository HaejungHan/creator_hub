package com.academy.creator_hub.model;

import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.dto.VideoRecommandationDto;
import com.google.api.services.youtube.model.Video;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@Getter
@Document(collection = "user_video_recommendations")
public class VideoRecommendation {

    @Id
    private String id;
    private String username;
    private List<VideoRecommandationDto> recommendations;

    public VideoRecommendation(String username, List<VideoRecommandationDto> recommendations) {
        this.username = username;
        this.recommendations = recommendations;
    }
}
