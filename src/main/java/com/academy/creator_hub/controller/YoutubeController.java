package com.academy.creator_hub.controller;


import com.academy.creator_hub.dto.PlaybackTimeRequest;
import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.entity.PlaybackTime;
import com.academy.creator_hub.service.YouTubeService;
import com.academy.creator_hub.service.YouTubeTrendAnalyzer;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class YoutubeController {

    private final YouTubeService youTubeService;
    private final YouTubeTrendAnalyzer youTubeTrendAnalyzer;

    @GetMapping("/search")
    public List<VideoDto> searchVideos(@RequestParam String query) throws IOException {
        return youTubeService.searchVideos(query);
    }

    @GetMapping("/popular")
    public List<VideoDto> getPopularVideos() throws IOException {
        return youTubeService.getPopularVideos();
    }

    @GetMapping("/video/{id}")
    public Video getVideoById(@PathVariable String id) throws IOException {
        return youTubeService.getVideoId(id);
    }

    @PostMapping("/save-playback-time")
    public String savePlaybackTime(@RequestBody PlaybackTimeRequest request) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (username == null) {
            return "User not authenticated";
        }
        youTubeService.savePlaybackTime(username, request);

        return "Playback time saved successfully!";
    }

}

