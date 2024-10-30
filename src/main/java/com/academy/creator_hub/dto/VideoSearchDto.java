package com.academy.creator_hub.dto;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoSearchDto {
    private String videoId;
    private String title;
    private String thumbnailUrl;
}
