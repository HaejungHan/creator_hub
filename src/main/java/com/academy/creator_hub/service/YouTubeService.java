package com.academy.creator_hub.service;

import com.academy.creator_hub.dto.VideoAnalyzeDto;
import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.model.User;
import com.academy.creator_hub.repository.PlaybackTimeRepository;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
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
    private final PlaybackTimeRepository playbackTimeRepository;
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

//    public String savePlaybackTime(String username, PlaybackTimeRequest request) {
//        // 사용자 인증이 안 된 경우 처리
//        if (username == null) {
//            return "User not authenticated";
//        }
//
//        // PlaybackTime 객체 생성
//        PlaybackTime playbackTime = new PlaybackTime();
//        playbackTime.setVideoId(request.getVideoId());      // 클라이언트에서 보낸 비디오 ID
//        playbackTime.setPlaybackTime(request.getPlaybackTime()); // 클라이언트에서 보낸 재생 시간
//        playbackTime.setUserId(username);  // 인증된 사용자의 정보
//
//        // MongoDB에 저장
//        playbackTimeRepository.save(playbackTime);
//
//        return "Playback time saved successfully!";
//    }

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

    private VideoAnalyzeDto mapToVideoAnalyzeDto(Video video) {
        String id = video.getId();
        String title = video.getSnippet().getTitle();
        BigInteger viewCount = video.getStatistics().getViewCount();
        BigInteger likeCount = video.getStatistics().getLikeCount();

        return new VideoAnalyzeDto(id, title, viewCount, likeCount);
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

}

