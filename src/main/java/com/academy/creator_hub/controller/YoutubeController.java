package com.academy.creator_hub.controller;


import com.academy.creator_hub.dto.ChannelResponseDto;
import com.academy.creator_hub.dto.TrendKeywordDto;
import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.model.VideoRecommendation;
import com.academy.creator_hub.repository.RecommendationRepository;
import com.academy.creator_hub.security.UserDetailsImpl;
import com.academy.creator_hub.service.KeywordAnalysisService;
import com.academy.creator_hub.service.SparkRecommendationService;
import com.academy.creator_hub.service.YouTubeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class YoutubeController {

    private final YouTubeService youTubeService;
    private final SparkRecommendationService sparkRecommendationService;
    private final KeywordAnalysisService keywordAnalysisService;
    private final RecommendationRepository  recommendationRepository;

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String showHomePage(Model model) throws IOException {
        List<TrendKeywordDto> trendKeywords = youTubeService.getKeywordTrendsForChart();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        model.addAttribute("trendKeywordsJson", objectMapper.writeValueAsString(trendKeywords));

        return "home";
    }

    @RequestMapping(value = "/searchVideos", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> searchVideos(@RequestParam String query,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize) throws IOException {
        List<VideoDto> allVideos = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            allVideos = youTubeService.searchVideos(query);
        }

        // 페이지네이션 처리
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allVideos.size());
        List<VideoDto> videosForCurrentPage = allVideos.subList(startIndex, endIndex);

        int totalVideos = allVideos.size();  // 전체 비디오 개수
        int totalPages = (int) Math.ceil((double) totalVideos / pageSize);  // 전체 페이지 수

        Map<String, Object> response = new HashMap<>();
        response.put("videos", videosForCurrentPage);  // 현재 페이지 비디오 목록
        response.put("total", totalVideos);  // 전체 비디오 개수
        response.put("page", page);  // 현재 페이지 번호
        response.put("totalPages", totalPages);  // 전체 페이지 수

        return response;
    }

    @GetMapping("/popular")
    public List<VideoDto> getPopularVideos() throws IOException {
        return youTubeService.getPopularVideos();
    }

    @GetMapping("/video/{id}")
    public Video getVideoById(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return youTubeService.getVideoId(id);
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

