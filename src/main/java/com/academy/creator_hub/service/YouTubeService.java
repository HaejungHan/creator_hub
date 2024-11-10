package com.academy.creator_hub.service;

import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.model.User;
import com.academy.creator_hub.repository.RecommendationRepository;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeService {
    private final RecommendationRepository recommendationRepository;
    private final YouTube youtube;

    @Value("${youtube.api.key}")
    private String API_KEY;

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

    public Video getVideoId(String id, User user) throws IOException {

        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setId(Collections.singletonList(id))
                .setKey(API_KEY);

        VideoListResponse response = request.execute();
        if (response.getItems().isEmpty()) {
            return null;
        }
        return response.getItems().get(0);
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

    private VideoDto mapToVideoDto(Video video) {
        VideoSnippet snippet = video.getSnippet();
        VideoStatistics statistics = video.getStatistics();
        VideoContentDetails contentDetails = video.getContentDetails();

        LocalDateTime publishedAt = null;
        if (snippet.getPublishedAt() != null) {
            long publishedAtMillis = snippet.getPublishedAt().getValue();
            publishedAt = Instant.ofEpochMilli(publishedAtMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        return new VideoDto(
                video.getId(),
                snippet.getTitle(),
                snippet.getDescription(),
                snippet.getThumbnails().getDefault().getUrl(),
                statistics.getViewCount(),
                statistics.getLikeCount(),
                statistics.getCommentCount(),
                publishedAt,
                snippet.getChannelId(),
                snippet.getChannelTitle(),
                snippet.getCategoryId(),
                contentDetails.getDuration()
        );
    }


}

