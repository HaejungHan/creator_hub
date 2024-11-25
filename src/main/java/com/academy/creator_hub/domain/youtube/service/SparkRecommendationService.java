package com.academy.creator_hub.domain.youtube.service;


import com.academy.creator_hub.domain.youtube.dto.VideoRecommandationDto;
import com.academy.creator_hub.domain.youtube.model.VideoRecommendation;
import com.academy.creator_hub.domain.youtube.repository.RecommendationRepository;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SparkRecommendationService {

    private final SparkSession spark;
    private final RecommendationRepository recommendationRepository;
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Autowired
    public SparkRecommendationService(SparkSession spark, RecommendationRepository recommendationRepository) {
        this.spark = spark;
        this.recommendationRepository = recommendationRepository;
    }

    @Async
    public void generateRecommendations(String username) {
        if (recommendationRepository.existsByUsername(username)) {
            // 이미 추천 데이터가 존재하면 메서드를 종료하고 추가 작업을 하지 않음
            System.out.println("추천 데이터가 이미 존재합니다. 생성하지 않습니다.");
            return;
        }
        Dataset<Row> userInterests = getUserInterests(username);
        Dataset<Row> videos = getVideos();
        Dataset<Row> userKeywords = extractKeywords(userInterests, "interestsString");
        Dataset<Row> videoKeywords = extractKeywords(videos, "tagsString");

        Dataset<Row> similarityScores = calculateKeywordMatch(userKeywords, videoKeywords);

        Dataset<Row> recommendedVideos = similarityScores
                .withColumn("username", functions.lit(username))
                .orderBy(functions.desc("similarity"))
                .limit(10);

        JavaRDD<Row> recommendedVideosRDD = recommendedVideos.javaRDD();
        saveRecommendationsToMongoDB(recommendedVideosRDD, username, videos);
    }

    private Dataset<Row> getUserInterests(String username) {
        Dataset<Row> userInterests = spark.read().format("mongo")
                .option("uri", mongoUri)
                .option("database", "creator_hub")
                .option("collection", "users")
                .option("filter", "{\"username\":\"" + username + "\"}")
                .load();

        // 'Interests' 배열을 explode하여 개별 관심사로 분리
        userInterests = userInterests.withColumn("interestsString", functions.explode(functions.col("Interests")))
                .select("username", "interestsString");

        return userInterests;
    }

    private Dataset<Row> getVideos() {
        Dataset<Row> videos = spark.read().format("mongo")
                .option("uri", mongoUri)
                .option("database", "creator_hub")
                .option("collection", "videos")
                .load()
                .select("videoId", "title", "description", "thumbnailUrl", "viewCount", "likeCount", "commentCount",
                        "publishedAt", "channelId", "channelTitle", "categoryId", "duration", "tags");

        // 'tags' 배열을 explode하여 개별 키워드로 분리
        videos = videos.withColumn("tagsString", functions.explode(functions.col("tags"))).select("videoId", "tagsString",
                "title", "description", "thumbnailUrl", "viewCount", "likeCount", "commentCount", "publishedAt",
                "channelId", "channelTitle", "categoryId", "duration");

        return videos;
    }


    private Dataset<Row> extractKeywords(Dataset<Row> dataset, String column) {
        return dataset.withColumn("keywords", functions.split(functions.col(column), ", "))
                .withColumn("keywords", functions.explode(functions.col("keywords")));
    }

    public static Dataset<Row> calculateKeywordMatch(Dataset<Row> userKeywords, Dataset<Row> videoKeywords) {
        userKeywords = userKeywords.withColumnRenamed("keywords", "user_keywords").distinct();
        videoKeywords = videoKeywords.withColumnRenamed("keywords", "video_keywords").distinct();

        // 데이터 재파티셔닝 - 조인 컬럼 기준으로 파티셔닝
        userKeywords = userKeywords.repartition(200, userKeywords.col("user_keywords"));
        videoKeywords = videoKeywords.repartition(200, videoKeywords.col("video_keywords"));

        Dataset<Row> joined = userKeywords.join(videoKeywords,
                userKeywords.col("user_keywords").equalTo(videoKeywords.col("video_keywords")));

        return joined.groupBy("videoId")
                .agg(functions.countDistinct("user_keywords").alias("similarity"));
    }

    private void saveRecommendationsToMongoDB(JavaRDD<Row> recommendedVideos, String username, Dataset<Row> videos) {
        // 추천 리스트를 채우기
        List<VideoRecommendation> recommendationList = recommendedVideos.collect()
                .stream()
                .map(row -> {
                    // 유사도 값 가져오기
                    Object similarityObj = row.getAs("similarity");

                    if (similarityObj == null) {
                        return null;
                    }

                    Double similarity = null;
                    if (similarityObj instanceof Long) {
                        similarity = ((Long) similarityObj).doubleValue();
                    } else if (similarityObj instanceof Double) {
                        similarity = (Double) similarityObj;
                    }

                    if (similarity == null || similarity.isNaN()) {
                        return null;
                    }

                    Row videoRow = getVideoDetails(row.getAs("videoId"), videos);

                    if (videoRow == null) {
                        return null;
                    }

                    Timestamp timestamp = videoRow.getAs("publishedAt");
                    LocalDateTime publishedAt = timestamp != null ? timestamp.toLocalDateTime() : null;

                    VideoRecommandationDto videoDto = new VideoRecommandationDto(
                            videoRow.getAs("videoId"),
                            videoRow.getAs("title"),
                            videoRow.getAs("description"),
                            videoRow.getAs("thumbnailUrl"),
                            new BigInteger(videoRow.getAs("viewCount").toString()),
                            new BigInteger(videoRow.getAs("likeCount").toString()),
                            new BigInteger(videoRow.getAs("commentCount").toString()),
                            publishedAt,
                            videoRow.getAs("channelId"),
                            videoRow.getAs("channelTitle"),
                            videoRow.getAs("categoryId"),
                            videoRow.getAs("duration"),
                            similarity
                    );

                    return new VideoRecommendation(
                            username,
                            List.of(videoDto)
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 추천 목록을 하나의 username으로 묶어서 저장
        if (!recommendationList.isEmpty()) {
            Map<String, List<VideoRecommendation>> groupedByUsername = recommendationList.stream()
                    .collect(Collectors.groupingBy(VideoRecommendation::getUsername));

            for (Map.Entry<String, List<VideoRecommendation>> entry : groupedByUsername.entrySet()) {
                String user = entry.getKey();
                List<VideoRecommendation> userRecommendations = entry.getValue();

                List<VideoRecommandationDto> allRecommendations = userRecommendations.stream()
                        .flatMap(r -> r.getRecommendations().stream())
                        .collect(Collectors.toList());

                VideoRecommendation videoRecommendation = new VideoRecommendation(
                        user,
                        allRecommendations
                );
                recommendationRepository.save(videoRecommendation);
            }

            System.out.println("추천 리스트가 MongoDB에 저장되었습니다.");
        } else {
            System.out.println("추천할 동영상이 없습니다.");
        }
    }

    private Row getVideoDetails(String videoId, Dataset<Row> videos) {
        return videos.filter(functions.col("videoId").equalTo(videoId))
                .head();
    }
}


