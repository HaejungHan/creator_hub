package com.academy.creator_hub.controller;


import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.service.YouTubeService;
import com.academy.creator_hub.service.YouTubeTrendAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}

