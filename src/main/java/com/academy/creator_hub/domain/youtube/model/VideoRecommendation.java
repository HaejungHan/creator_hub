package com.academy.creator_hub.domain.youtube.model;

import com.academy.creator_hub.domain.youtube.dto.VideoRecommandationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@Getter
@Document(collection = "user_video_recommendations")
public class VideoRecommendation {

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private List<VideoRecommandationDto> recommendations;

    public VideoRecommendation(String username, List<VideoRecommandationDto> recommendations) {
        this.username = username;
        this.recommendations = recommendations;
    }
}
