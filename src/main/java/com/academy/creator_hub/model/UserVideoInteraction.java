package com.academy.creator_hub.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@NoArgsConstructor
@Getter
@Document(collection = "user_video_interactions")
public class UserVideoInteraction {
    @Id
    private String id;        // MongoDB 자동 생성된 ID (필요시 사용할 수 있습니다)
    @Field("username")
    private String username;    // 사용자 ID
    private String videoId;   // 동영상 ID
    private int rating;       // 평점 또는 시청 시간 (예: 1~5)

    public UserVideoInteraction(String username, String videoId, int rating) {
        this.username = username;
        this.videoId = videoId;
        this.rating = rating;
    }
}
