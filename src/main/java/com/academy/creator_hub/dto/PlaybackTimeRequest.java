package com.academy.creator_hub.dto;

import lombok.Getter;

@Getter
public class PlaybackTimeRequest {
    private String videoId;
    private long playbackTime;
}
