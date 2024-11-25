package com.academy.creator_hub.domain.youtube.controller;
import com.academy.creator_hub.domain.youtube.dto.ChannelResponseDto;
import com.academy.creator_hub.domain.youtube.dto.TrendKeywordDto;
import com.academy.creator_hub.domain.youtube.dto.VideoDto;
import com.academy.creator_hub.domain.youtube.dto.VideoRecommandationDto;
import com.academy.creator_hub.domain.youtube.model.VideoRecommendation;
import com.academy.creator_hub.domain.youtube.repository.RecommendationRepository;
import com.academy.creator_hub.security.UserDetailsImpl;
import com.academy.creator_hub.domain.youtube.service.KeywordAnalysisService;
import com.academy.creator_hub.domain.youtube.service.SparkRecommendationService;
import com.academy.creator_hub.domain.youtube.service.YouTubeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        int totalVideos = allVideos.size();
        int totalPages = (int) Math.ceil((double) totalVideos / pageSize);  // 전체 페이지 수

        Map<String, Object> response = new HashMap<>();
        response.put("videos", videosForCurrentPage);
        response.put("total", totalVideos);
        response.put("page", page);
        response.put("totalPages", totalPages);

        return response;
    }

    @RequestMapping(value ="/video/{videoId}", method = RequestMethod.GET)
    @ResponseBody
    public Video getVideoById(
            @PathVariable(value = "videoId") String videoId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        System.out.println("Fetching video for ID: " + videoId);
        return youTubeService.getVideoId(videoId);
    }

    @RequestMapping(value = "/popular", method = RequestMethod.GET)
    public String getPopularVideos (Model model) throws JsonProcessingException {
        List<VideoDto> videoDto = youTubeService.getPopularVideos();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonVideoDtos = objectMapper.writeValueAsString(videoDto);

        model.addAttribute("videoDto", jsonVideoDtos);
        return "popularVideo";
    }

    @RequestMapping(value = "/recommendations", method = RequestMethod.GET)
    public String recommendationPage() {
        return "recommendationVideo";
    }

    @RequestMapping(value = "/recommendation", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getRecommendations(@AuthenticationPrincipal UserDetailsImpl userDetails) throws JsonProcessingException {

        if (userDetails == null) {
            throw new IllegalArgumentException("로그인 필요");
        }

        Map<String, Object> response = new HashMap<>();

        String username = userDetails.getUsername();
        List<VideoRecommandationDto> recommendations = youTubeService.getRecommendationsForUser(username);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String recommendationsJson = recommendations.isEmpty() ? "[]" : objectMapper.writeValueAsString(recommendations);

        response.put("recommendations", recommendationsJson);
        return response;
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

