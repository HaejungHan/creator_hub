package com.academy.creator_hub.dto;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private String videoId;
    private String title;
    private String thumbnailUrl;
    private BigInteger viewCount;
    private DateTime publishedAt;
    private String duration;
}
