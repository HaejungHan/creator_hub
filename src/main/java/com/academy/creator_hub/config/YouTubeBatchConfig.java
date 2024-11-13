package com.academy.creator_hub.config;

import com.academy.creator_hub.model.Videos;
import com.academy.creator_hub.repository.VideoRepository;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class YouTubeBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final YouTube youTube;
    private final VideoRepository videoRepository;
    private final JobLauncher jobLauncher;

    @Value("${youtube.api.key}")
    private String API_KEY;

    @Autowired
    public YouTubeBatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                              YouTube youtube, VideoRepository videoRepository, JobLauncher jobLauncher) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.youTube = youtube;
        this.videoRepository = videoRepository;
        this.jobLauncher = jobLauncher;
    }

    @Bean
    @Lazy
    public Job youtubeJob() {
        return jobBuilderFactory.get("youtubeJob")
                .incrementer(new RunIdIncrementer())
                .flow(youtubeStep())
                .end()
                .build();
    }

    @Bean
    public Step youtubeStep() {
        return stepBuilderFactory.get("youtubeStep")
                .<Video, Videos>chunk(10)
                .reader(youtubeReader())
                .processor(youtubeProcessor())
                .writer(youtubeWriter())
                .build();
    }

    @Bean
    public ListItemReader<Video> youtubeReader() {
        return new ListItemReader<>(fetchAllVideos());
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (allVideos.isEmpty()) {
            throw new RuntimeException("No videos found.");
        }
        return allVideos;
    }

    @Bean
    public Function<Video, Videos> youtubeProcessor() {
        return video -> {
            if (video == null) {
                // null일 경우 로그를 찍고 넘어가도록 처리
                System.out.println("동영상이 Null 입니다.");
                return null;  // Null 값을 반환해서, 이후 처리에서 무시하도록 할 수 있습니다.
            }

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

            List<String> tags = snippet.getTags() != null ? snippet.getTags() : new ArrayList<>();

            return new Videos(
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
                    contentDetails.getDuration(),
                    tags // 태그 추가
            );
        };
    }

    @Bean
    public ItemWriter<Videos> youtubeWriter() {
        return items -> {
            List<Videos> newVideos = new ArrayList<>();
            for (Videos video : items) {
                if (video != null) {
                    try {
                        Optional<Videos> existingVideo = videoRepository.findByVideoId(video.getVideoId());
                        if (!existingVideo.isPresent()) {
                            newVideos.add(video);
                        } else {
                            System.out.println("------이미 존재하는 비디오: " + video.getVideoId() + "-----");
                        }
                    } catch (Exception e) {
                        System.out.println("MongoDB 처리 중 오류 발생: " + e.getMessage());
                    }
                }
            }

            // 새 비디오가 있으면 저장
            if (!newVideos.isEmpty()) {
                try {
                    videoRepository.saveAll(newVideos); // MongoRepository의 saveAll을 사용하여 비디오 저장
                    System.out.println("------mongoDB 저장완료-----");
                } catch (Exception e) {
                    System.out.println("MongoDB 저장 중 오류 발생: " + e.getMessage());
                }
            }
        };
    }

    @Scheduled(fixedRate = 10800000)
    public void scheduleYoutubeJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            System.out.println("API 3분마다 호출 시작:" + LocalDateTime.now());
            jobLauncher.run(youtubeJob(), jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
