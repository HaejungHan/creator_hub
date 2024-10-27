package com.academy.creator_hub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@NoArgsConstructor
@Getter
public class VideoAnalyzeDto {
    private String id;
    private String title;
    private BigInteger viewCount;
    private BigInteger likeCount;

    public VideoAnalyzeDto(String id, String title, BigInteger viewCount, BigInteger likeCount) {
        this.id = id;
        this.title = title;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
    }
}
