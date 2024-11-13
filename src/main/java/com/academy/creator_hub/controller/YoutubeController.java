package com.academy.creator_hub.controller;


import com.academy.creator_hub.dto.ChannelResponseDto;
import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.model.VideoRecommendation;
import com.academy.creator_hub.repository.RecommendationRepository;
import com.academy.creator_hub.security.UserDetailsImpl;
import com.academy.creator_hub.service.KeywordAnalysisService;
import com.academy.creator_hub.service.SparkRecommendationService;
import com.academy.creator_hub.service.YouTubeService;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class YoutubeController {

    private final YouTubeService youTubeService;
    private final SparkRecommendationService sparkRecommendationService;
    private final KeywordAnalysisService keywordAnalysisService;
    private final RecommendationRepository  recommendationRepository;

    @GetMapping("/search")
    public List<VideoDto> searchVideos(@RequestParam String query) throws IOException {
        return youTubeService.searchVideos(query);
    }

    @GetMapping("/popular")
    public List<VideoDto> getPopularVideos() throws IOException {
        return youTubeService.getPopularVideos();
    }

    @GetMapping("/video/{id}")
    public Video getVideoById(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return youTubeService.getVideoId(id, userDetails.getUser());
    }

    @GetMapping("/recommendations")
    public VideoRecommendation getRecommendations(@AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        Optional<VideoRecommendation> recommendation = recommendationRepository.findByUsername(userDetails.getUsername());
        return recommendation.orElse(null);
    }

    @PostMapping("/generate")
    public void generateRecommendations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
           sparkRecommendationService.generateRecommendations(userDetails.getUsername());
    }

//    @GetMapping("/keywords/trend")
//    public void getKeywordTrends() {
//        keywordAnalysisService.analyzeKeywordTrendsAndSave();
//    }

    @GetMapping("/channel/{channelId}")
    public ChannelResponseDto getChannelInfo(@PathVariable String channelId) {
        try {
            Channel channel = youTubeService.getChannelInfo(channelId);

            return new ChannelResponseDto(
                    channel.getSnippet().getTitle(),
                    channel.getSnippet().getDescription(),
                    channel.getSnippet().getCustomUrl(),
                    channel.getSnippet().getPublishedAt().toString(),
                    channel.getStatistics().getViewCount().toString(),
                    channel.getStatistics().getSubscriberCount().toString(),
                    channel.getStatistics().getVideoCount().toString(),
                    channel.getBrandingSettings().getChannel().getKeywords()
            );
        } catch (IOException e) {
            throw new RuntimeException("채널 정보를 가져오는 데 실패했습니다.", e);
        }
    }

}

