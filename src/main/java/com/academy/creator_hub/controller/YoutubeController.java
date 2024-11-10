package com.academy.creator_hub.controller;


import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.dto.VideoPlaybackRequest;
import com.academy.creator_hub.model.UserVideoInteraction;
import com.academy.creator_hub.model.VideoRecommendation;
import com.academy.creator_hub.repository.RecommendationRepository;
import com.academy.creator_hub.repository.UserVideoInteractionRepository;
import com.academy.creator_hub.security.UserDetailsImpl;
import com.academy.creator_hub.service.SparkALSModelService;
import com.academy.creator_hub.service.YouTubeService;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
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
    private final SparkALSModelService sparkALSModelService;
    private final RecommendationRepository  recommendationRepository;
    private final UserVideoInteractionRepository userVideoInteractionRepository;

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
        // 특정 사용자에 대한 추천 결과 조회
        Optional<VideoRecommendation> recommendation = recommendationRepository.findByUsername(userDetails.getUsername());
        return recommendation.orElse(null);  // 추천 결과가 없으면 null 반환
    }

    @GetMapping("/generate")
    public void generateRecommendations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
           sparkALSModelService.generateRecommendations(userDetails.getUsername());
    }


}

