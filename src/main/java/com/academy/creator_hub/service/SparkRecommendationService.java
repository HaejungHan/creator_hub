package com.academy.creator_hub.service;


import com.academy.creator_hub.model.VideoRecommendation;
import com.academy.creator_hub.repository.RecommendationRepository;
import com.academy.creator_hub.repository.VideoRepository;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SparkRecommendationService {

    private final SparkSession spark;
    private final RecommendationRepository recommendationRepository;
    private final VideoRepository videoRepository;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Autowired
    public SparkRecommendationService(SparkSession spark, RecommendationRepository recommendationRepository, VideoRepository videoRepository) {
        this.spark = spark;
        this.recommendationRepository = recommendationRepository;
        this.videoRepository = videoRepository;
    }

    public void generateRecommendations(String username) {
        Dataset<Row> userInterests = getUserInterests(username);
        Dataset<Row> videos = getVideos();
        Dataset<Row> userKeywords = extractKeywords(userInterests, "interestsString"); // 사용자 관심사에서 키워드 추출
        Dataset<Row> videoKeywords = extractKeywords(videos, "tagsString");  // 동영상 설명에서 키워드 추출

        Dataset<Row> similarityScores = calculateKeywordMatch(userKeywords, videoKeywords);

        Dataset<Row> recommendedVideos = similarityScores
                .withColumn("username", functions.lit(username))
                .orderBy(functions.desc("similarity"))
                .limit(10);

        JavaRDD<Row> recommendedVideosRDD = recommendedVideos.javaRDD();
        saveRecommendationsToMongoDB(recommendedVideosRDD, username);
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
                .select("videoId", "tags");

        // 'tags' 배열을 explode하여 개별 키워드로 분리
        videos = videos.withColumn("tagsString", functions.explode(functions.col("tags")))
                .select("videoId", "tagsString");

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

    public void saveRecommendationsToMongoDB(JavaRDD<Row> recommendedVideos, String username) {
        List<VideoRecommendation> recommendationList = recommendedVideos.collect()
                .stream()
                .map(row -> {
                    String videoId = row.getAs("videoId");
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

                    return new VideoRecommendation(username, videoId, similarity);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!recommendationList.isEmpty()) {
            recommendationRepository.saveAll(recommendationList);
            System.out.println("추천 리스트가 MongoDB에 저장되었습니다.");
        } else {
            System.out.println("추천할 동영상이 없습니다.");
        }
    }

}


