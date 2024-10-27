package com.academy.creator_hub.service;

import com.academy.creator_hub.dto.VideoAnalyzeDto;
import com.academy.creator_hub.dto.VideoDto;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YouTubeService {

    private final YouTube youtube;

    @Autowired
    public YouTubeService(YouTube youtube) {
        this.youtube = youtube;
    }

    public List<VideoDto> getPopularVideos() throws IOException {
        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setChart("mostPopular")
                .setRegionCode("KR") // 한국의 인기 동영상
                .setMaxResults(10L);

        VideoListResponse response = request.execute();
        return response.getItems().stream()
                .map(this::mapToVideoDto)
                .collect(Collectors.toList());
    }

    public List<VideoAnalyzeDto> fetchTrendingVideos(String regionCode) throws IOException {
        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setChart("mostPopular")
                .setRegionCode(regionCode) // 지역 코드를 매개변수로 받음
                .setMaxResults(10L); // 최대 10개의 비디오

        VideoListResponse response = request.execute();
        return response.getItems().stream()
                .map(this::mapToVideoAnalyzeDto)
                .collect(Collectors.toList());
    }

    private VideoAnalyzeDto mapToVideoAnalyzeDto(Video video) {
        String id = video.getId();
        String title = video.getSnippet().getTitle();
        BigInteger viewCount = video.getStatistics().getViewCount(); // BigInteger 타입으로 가져오기
        BigInteger likeCount = video.getStatistics().getLikeCount(); // BigInteger 타입으로 가져오기

        return new VideoAnalyzeDto(id, title, viewCount, likeCount);
    }

    private VideoDto mapToVideoDto(Video video) {
        return new VideoDto(
                video.getId(),
                video.getSnippet().getTitle(),
                video.getStatistics().getViewCount(),
                "https://www.youtube.com/watch?v=" + video.getId()
        );
    }
}

