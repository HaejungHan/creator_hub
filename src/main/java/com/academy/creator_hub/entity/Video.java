package com.academy.creator_hub.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;

@Table(name = "videos")
@Entity
@Getter
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String videoId;
    private String title;
    private BigInteger viewCount;
    private String url;

    public Video(String videoId, String title, BigInteger viewCount, String url) {
        this.videoId = videoId;
        this.title = title;
        this.viewCount = viewCount;
        this.url = url;
    }
}
