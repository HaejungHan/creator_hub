package com.academy.creator_hub.dto;

import com.academy.creator_hub.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto implements Serializable {
    private String videoId;
    private String title;
    private BigInteger viewCount;
    private String url;

    public Video toEntity() {
        return new Video(videoId, title, viewCount, url);
    }
}
