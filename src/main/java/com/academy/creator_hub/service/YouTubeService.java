package com.academy.creator_hub.service;

import com.academy.creator_hub.dto.VideoAnalyzeDto;
import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.dto.VideoSearchDto;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YouTubeService {

    private final YouTube youtube;

    @Value("${youtube.api.key}")
    private String API_KEY;

    @Autowired
    public YouTubeService(YouTube youtube) {
        this.youtube = youtube;
    }

    public List<VideoDto> getPopularVideos() throws IOException {
        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setChart("mostPopular")
                .setRegionCode("KR")
                .setMaxResults(6L)
                .setKey(API_KEY);

        VideoListResponse response = request.execute();
        return response.getItems().stream()
                .map(this::mapToVideoDto)
                .collect(Collectors.toList());
    }



    public List<VideoDto> searchVideos(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어 입력이 필요합니다.");
        }

        YouTube.Search.List request = youtube.search()
                .list(Collections.singletonList("snippet"))
                .setQ(query)
                .setType(Collections.singletonList("video"))
                .setMaxResults(6L)
                .setKey(API_KEY);

        SearchListResponse response = request.execute();

        List<VideoDto> videoDtos = new ArrayList<>();

        for (SearchResult searchResult : response.getItems()) {
            String videoId = searchResult.getId().getVideoId();

            VideoDto videoDto = getVideoDetails(videoId);
            videoDtos.add(videoDto);
        }
        return videoDtos;
    }

    private VideoDto getVideoDetails(String videoId) throws IOException {
        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setId(Collections.singletonList(videoId))
                .setKey(API_KEY);

        VideoListResponse response = request.execute();

        if (!response.getItems().isEmpty()) {
            Video video = response.getItems().get(0);
            return mapToVideoDto(video);
        }

        return null;
    }


    public List<VideoAnalyzeDto> fetchTrendingVideos(String regionCode) throws IOException {
        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setChart("mostPopular")
                .setRegionCode(regionCode)
                .setMaxResults(10L);

        VideoListResponse response = request.execute();
        return response.getItems().stream()
                .map(this:: mapToVideoAnalyzeDto)
                .collect(Collectors.toList());
    }

    private VideoAnalyzeDto mapToVideoAnalyzeDto(Video video) {
        String id = video.getId();
        String title = video.getSnippet().getTitle();
        BigInteger viewCount = video.getStatistics().getViewCount();
        BigInteger likeCount = video.getStatistics().getLikeCount();

        return new VideoAnalyzeDto(id, title, viewCount, likeCount);
    }

    private VideoDto mapToVideoDto(Video video) {
        return new VideoDto(
                video.getId(),
                video.getSnippet().getTitle(),
                video.getSnippet().getThumbnails().getDefault().getUrl(),
                video.getStatistics().getViewCount(),
                video.getSnippet().getPublishedAt(),
                video.getContentDetails().getDuration()
        );
    }

    private VideoSearchDto mapToVideoSearchDto(SearchResult searchResult) {
        return new VideoSearchDto(
                searchResult.getId().getVideoId(),
                searchResult.getSnippet().getTitle(),
                searchResult.getSnippet().getThumbnails().getDefault().getUrl()
        );
    }
}

