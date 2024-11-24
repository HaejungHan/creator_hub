package com.academy.creator_hub.config.youtube;

import com.academy.creator_hub.domain.youtube.model.Videos;
import com.academy.creator_hub.domain.youtube.repository.VideoRepository;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableScheduling
public class YouTubeRealTimeConfig {

    private final YouTube youTube;
    private final VideoRepository videoRepository;

    @Value("${youtube.api.key}")
    private String API_KEY;

    @Autowired
    public YouTubeRealTimeConfig(YouTube youtube, VideoRepository videoRepository) {
        this.youTube = youtube;
        this.videoRepository = videoRepository;
    }

    private List<Video> fetchAllVideos() {
        List<Video> allVideos = new ArrayList<>();
        String nextPageToken = null;

        try {
            do {
                YouTube.Videos.List request = youTube.videos()
                        .list(Collections.singletonList("snippet,contentDetails,statistics"))
                        .setChart("mostPopular")
                        .setRegionCode("KR")
                        .setMaxResults(50L)
                        .setKey(API_KEY);

                if (nextPageToken != null) {
                    request.setPageToken(nextPageToken);
                }

                VideoListResponse response = request.execute();
                List<Video> videos = response.getItems();

                if (videos != null && !videos.isEmpty()) {
                    System.out.println("Fetched " + videos.size() + " videos.");
                    allVideos.addAll(videos);
                } else {
                    System.out.println("No new videos fetched.");
                }
                nextPageToken = response.getNextPageToken();

            } while (nextPageToken != null);

        } catch (GoogleJsonResponseException e) {
            System.err.println("YouTube API error: " + e.getDetails());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException while calling YouTube API: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error while fetching videos: " + e.getMessage());
            e.printStackTrace();
        }
        if (allVideos.isEmpty()) {
            throw new RuntimeException("No videos found.");
        }
        return allVideos;
    }

    public void processVideosRealTime() {
        List<Video> videos = fetchAllVideos();

        for (Video video : videos) {
            if (video != null) {
                VideoSnippet snippet = video.getSnippet();
                VideoStatistics statistics = video.getStatistics();
                VideoContentDetails contentDetails = video.getContentDetails();

                // 유효성 검사 및 데이터 처리
                if (snippet == null || statistics == null || contentDetails == null) {
                    System.out.println("유효하지 않은 video 데이터 발견, 일부 데이터가 누락되었습니다.");
                    continue;
                }

                Videos videoEntity = new Videos(
                        video.getId(),
                        snippet.getTitle(),
                        snippet.getDescription(),
                        snippet.getThumbnails().getDefault().getUrl(),
                        statistics.getViewCount(),
                        statistics.getLikeCount(),
                        statistics.getCommentCount(),
                        Instant.ofEpochMilli(snippet.getPublishedAt().getValue())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime(),
                        snippet.getChannelId(),
                        snippet.getChannelTitle(),
                        snippet.getCategoryId(),
                        contentDetails.getDuration(),
                        snippet.getTags()
                );

                try {
                    // 비디오가 존재하지 않으면 새로 저장
                    Optional<Videos> existingVideo = videoRepository.findByVideoId(videoEntity.getVideoId());
                    if (!existingVideo.isPresent()) {
                        videoRepository.save(videoEntity);
                        System.out.println("새 비디오 저장 완료: " + videoEntity.getVideoId());
                    } else {
                        System.out.println("이미 존재하는 비디오: " + videoEntity.getVideoId());
                    }
                } catch (Exception e) {
                    // 예외 처리: 중복된 데이터 발생 시
                    System.out.println("비디오 저장 중 오류 발생: " + e.getMessage());
                }
            }
        }
    }

    @Scheduled(fixedRate = 10800000)  // 3시간마다 호출
    public void scheduleRealTimeJob() {
        System.out.println("실시간 비디오 데이터 처리 시작: " + LocalDateTime.now());
        processVideosRealTime();  // 실시간 데이터 처리 메소드 호출
    }

}
