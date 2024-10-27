package com.academy.creator_hub.controller;



import com.academy.creator_hub.dto.VideoAnalyzeDto;
import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.service.YouTubeService;
import com.academy.creator_hub.service.YouTubeTrendAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class YoutubeController {

    private final YouTubeService youTubeService;
    private final YouTubeTrendAnalyzer youTubeTrendAnalyzer;


    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("name", "Your Name");
        model.addAttribute("list", List.of("Item 1", "Item 2", "Item 3"));
        return "index";
    }

    @GetMapping("/popular")
    public ResponseEntity<List<VideoDto>> getPopularVideos() {
        try {
            List<VideoDto> videos = youTubeService.getPopularVideos();
            return ResponseEntity.ok(videos);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null); // 내부 서버 오류
        }
    }

    @GetMapping("/trend")
    public ResponseEntity<List<VideoAnalyzeDto>> fetchTrendingVideos(@RequestParam String regionCode) {
        try {
            List<VideoAnalyzeDto> trendingVideos = youTubeService.fetchTrendingVideos(regionCode);
            youTubeTrendAnalyzer.analyzeTrends(trendingVideos);
            return ResponseEntity.ok(trendingVideos);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}

