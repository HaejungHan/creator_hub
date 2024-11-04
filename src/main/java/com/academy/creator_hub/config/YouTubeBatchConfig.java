package com.academy.creator_hub.config;

import com.academy.creator_hub.entity.Videos;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
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

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class YouTubeBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final YouTube youTube;
    private final MongoTemplate mongoTemplate;
    private final JobLauncher jobLauncher;

    @Value("${youtube.api.key}")
    private String API_KEY;

    @Autowired
    public YouTubeBatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                              YouTube youtube, MongoTemplate mongoTemplate, JobLauncher jobLauncher) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.youTube = youtube;
        this.mongoTemplate = mongoTemplate;
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
            do { // 먼저 실행
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
    public ItemProcessor<Video, Videos> youtubeProcessor() {
        return video -> {
            if (video == null) {
                return null; // null인 경우 null 반환
            }

            com.google.api.client.util.DateTime publishedAtDateTime = video.getSnippet().getPublishedAt();
            LocalDateTime publishedAt = null;

            if (publishedAtDateTime != null) {
                long publishedAtMillis = publishedAtDateTime.getValue(); // long 타입으로 가져오기
                publishedAt = Instant.ofEpochMilli(publishedAtMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(); // LocalDateTime으로 변환
            }


            return new Videos(
                    video.getId(),
                    video.getSnippet().getTitle(),
                    video.getSnippet().getThumbnails().getDefault().getUrl(),
                    video.getStatistics().getViewCount(),
                    publishedAt, // 변환된 LocalDateTime 사용
                    video.getContentDetails().getDuration(),
                    "https://www.youtube.com/watch?v=" + video.getId()
            );
        };
    }

    @Bean
    public ItemWriter<Videos> youtubeWriter() {
        return items -> {
            List<Videos> newVideos = new ArrayList<>();
            for (Videos video : items) {
                if (video != null) {
                    // 비디오가 이미 존재하는지 확인
                    if (mongoTemplate.findById(video.getVideoId(), Videos.class, "videos") == null) {
                        newVideos.add(video);
                    } else {
                        System.out.println("------이미 존재하는 비디오: " + video.getVideoId() + "-----");
                    }
                }
            }
            if (!newVideos.isEmpty()) {
                mongoTemplate.insertAll(newVideos); // 한꺼번에 저장
                System.out.println("------mongoDB 저장완료-----");
            }
        };
    }

    @Scheduled(fixedRate = 120000) // 3분마다 실행
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
