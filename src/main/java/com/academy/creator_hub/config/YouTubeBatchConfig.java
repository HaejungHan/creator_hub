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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class YouTubeBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Lazy
    @Autowired
    private YouTube youTube;

    @Lazy
    @Autowired
    private MongoTemplate mongoTemplate;

    @Lazy
    @Autowired
    private JobLauncher jobLauncher;

    @Value("${youtube.api.key}")
    private String API_KEY;

    @Autowired
    public YouTubeBatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @PostConstruct
    public void init() {
        System.out.println("API 호출 시작" + LocalDateTime.now());
        scheduleYoutubeJob();
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
        try {
            YouTube.Videos.List request = youTube.videos()
                    .list(Collections.singletonList("snippet,contentDetails,statistics"))
                    .setChart("mostPopular")
                    .setRegionCode("KR")
                    .setMaxResults(50L)
                    .setKey(API_KEY);

            VideoListResponse response = request.execute();
            List<Video> videos = response.getItems();

            if (videos == null || videos.isEmpty()) {
                throw new RuntimeException("No videos found.");
            }

            return new ListItemReader<>(videos);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListItemReader<>(List.of()); // 빈 리스트 반환
        }
    }

    @Bean
    public ItemProcessor<Video, Videos> youtubeProcessor() {
        return video -> {
            if (video == null) {
                return null; // null인 경우 null 반환
            }
            return new Videos(
                    video.getId(),
                    video.getSnippet().getTitle(),
                    video.getSnippet().getThumbnails().getDefault().getUrl(),
                    video.getStatistics().getViewCount(),
                    video.getSnippet().getPublishedAt(),
                    video.getContentDetails().getDuration(),
                    "https://www.youtube.com/watch?v=" + video.getId()
            );
        };
    }

    @Bean
    public ItemWriter<Videos> youtubeWriter() {
        return items -> {
            for (Videos video : items) {
                if (video != null) { // null 확인
                    mongoTemplate.save(video, "videos");
                    System.out.println("------mongoDB 저장완료-----");
                }
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
