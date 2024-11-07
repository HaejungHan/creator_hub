package com.academy.creator_hub.controller;


import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.security.UserDetailsImpl;
import com.academy.creator_hub.service.VideoRecommendationService;
import com.academy.creator_hub.service.YouTubeService;
import com.academy.creator_hub.service.YouTubeTrendAnalyzer;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class YoutubeController {

    private final YouTubeService youTubeService;
    private final VideoRecommendationService videoRecommendationService;

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

    @GetMapping("/recommend-videos")
    public List<VideoDto> getRecommendedVideos(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 사용자 정보(userDetails)와 관심사(userInterests)를 서비스로 전달하여 추천 비디오를 받아옴
        return videoRecommendationService.getRecommendedVideos(userDetails.getUser());
    }

//    @PostMapping("/save-playback-time")
//    public String savePlaybackTime(@RequestBody PlaybackTimeRequest request) {
//        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        if (username == null) {
//            return "User not authenticated";
//        }
//        youTubeService.savePlaybackTime(username, request);
//
//        return "Playback time saved successfully!";
//    }

}

