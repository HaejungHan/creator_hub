package com.academy.creator_hub.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@NoArgsConstructor
@Getter
@Document(collection = "playback_times")
public class PlaybackTime {
    private String userId;
    private String videoId;
    private long playbackTime;

    public PlaybackTime(String videoId, long playbackTime, String userId) {
        this.videoId = videoId;
        this.playbackTime = playbackTime;
        this.userId = userId;
    }
}
