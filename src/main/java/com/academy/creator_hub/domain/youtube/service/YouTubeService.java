package com.academy.creator_hub.domain.youtube.service;

import com.academy.creator_hub.domain.youtube.dto.TrendKeywordDto;
import com.academy.creator_hub.domain.youtube.dto.VideoDto;
import com.academy.creator_hub.domain.youtube.model.TrendKeyword;
import com.academy.creator_hub.domain.youtube.model.Videos;
import com.academy.creator_hub.domain.youtube.repository.TrendKeywordsRepository;
import com.academy.creator_hub.domain.youtube.repository.VideoRepository;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
    private final TrendKeywordsRepository trendKeywordsRepository;
    private final VideoRepository videoRepository;
    private final YouTube youtube;

    @Value("${youtube.api.key}")
    private String API_KEY;

    public List<VideoDto> getPopularVideos() {
        List<Videos> videos = videoRepository.findTop10ByOrderByPublishedAtDesc();
        return videos.stream().map(this::convertToVideoDto).collect(Collectors.toList());
    }

    public List<VideoDto> searchVideos(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어 입력이 필요합니다.");
        }

        YouTube.Search.List request = youtube.search()
                .list(Collections.singletonList("snippet"))
                .setQ(query)
                .setType(Collections.singletonList("video"))
                .setMaxResults(50L)
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

    public Video getVideoId(String id) throws IOException {

        YouTube.Videos.List request = youtube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"))
                .setId(Collections.singletonList(id))
                .setKey(API_KEY);

        VideoListResponse response = request.execute();
        if (response.getItems().isEmpty()) {
            throw new IllegalArgumentException("동영상 정보를 찾을 수 없습니다.");
        }
        return response.getItems().get(0);
    }

    public Channel getChannelInfo(String channelId) throws IOException {
        YouTube.Channels.List request = youtube.channels()
                .list(Collections.singletonList("snippet,statistics,brandingSettings"));  // 필요한 데이터 항목
        request.setId(Collections.singletonList(channelId));
        request.setKey(API_KEY);
        ChannelListResponse response = request.execute();
        if (response.getItems().isEmpty()) {
            throw new RuntimeException("채널 정보를 찾을 수 없습니다.");
        }

        return response.getItems().get(0);
    }

    public List<TrendKeywordDto> getKeywordTrendsForChart() {
        Sort sort = Sort.by(Sort.Order.desc("count"));

        List<TrendKeyword> trendKeywords = trendKeywordsRepository.findAll(sort);

        List<TrendKeyword> top10Keywords = trendKeywords.stream()
                .limit(10)
                .collect(Collectors.toList());

        return top10Keywords.stream()
                .map(keyword -> new TrendKeywordDto(keyword.getKeyword(), keyword.getCount()))
                .collect(Collectors.toList());
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

    private VideoDto convertToVideoDto(Videos video) {
        return new VideoDto(
                video.getVideoId(),
                video.getTitle(),
                video.getDescription(),
                video.getThumbnailUrl(),
                video.getViewCount(),
                video.getLikeCount(),
                video.getCommentCount(),
                video.getPublishedAt(),
                video.getChannelId(),
                video.getChannelTitle(),
                video.getCategoryId(),
                video.getDuration()
        );
    }
}

