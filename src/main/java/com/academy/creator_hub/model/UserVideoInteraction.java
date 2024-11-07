package com.academy.creator_hub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_video_interactions")
public class UserVideoInteraction {
    @Id
    private String id;
    private String userId;
    private String videoId;
}
