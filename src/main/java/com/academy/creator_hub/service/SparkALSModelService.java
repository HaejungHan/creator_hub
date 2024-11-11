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
public class SparkALSModelService {

    private final SparkSession spark;
    private final RecommendationRepository recommendationRepository;
    private final VideoRepository videoRepository;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Autowired
    public SparkALSModelService(SparkSession spark, RecommendationRepository recommendationRepository, VideoRepository videoRepository) {
        this.spark = spark;
        this.recommendationRepository = recommendationRepository;
        this.videoRepository = videoRepository;
    }

    public void generateRecommendations(String username) {
        // Step 1: MongoDB에서 사용자 관심사(Interests) 가져오기
        Dataset<Row> userInterests = getUserInterests(username); // 예시: username을 받아서 관심사 가져오기

        // Step 2: MongoDB에서 모든 동영상의 description 가져오기
        Dataset<Row> videos = getVideos(); // 동영상의 description 데이터를 가져옵니다
        // Step 3: 사용자 관심사에서 interestsString 컬럼 생성 (array_join으로 Interests 배열을 문자열로 결합)

        // Step 3: 관심사와 동영상 설명에서 키워드 추출 (단어 일치 기준)
        Dataset<Row> userKeywords = extractKeywords(userInterests, "interestsString"); // 사용자 관심사에서 키워드 추출
        Dataset<Row> videoKeywords = extractKeywords(videos, "description"); // 동영상 설명에서 키워드 추출

        // Step 4: 관심사와 동영상 설명 키워드 간의 일치 수 계산
        Dataset<Row> similarityScores = calculateKeywordMatch(userKeywords, videoKeywords);

        // Step 5: 가장 유사한 동영상 찾기 (username을 추가)
        Dataset<Row> recommendedVideos = similarityScores
                .withColumn("username", functions.lit(username))  // 각 추천 결과에 username 추가
                .orderBy(functions.desc("similarity"))
                .limit(10);  // 상위 10개 추천

        // Step 6: 추천 결과 저장
        JavaRDD<Row> recommendedVideosRDD = recommendedVideos.javaRDD();
        saveRecommendationsToMongoDB(recommendedVideosRDD, username);  // username을 전달
    }

    private Dataset<Row> getUserInterests(String username) {
        // MongoDB에서 사용자 관심사(Interests) 가져오기
        Dataset<Row> userInterests = spark.read().format("mongo")
                .option("uri", mongoUri)  // Mongo URI
                .option("database", "creator_hub")  // DB 이름
                .option("collection", "users")  // users 컬렉션
                .option("filter", "{\"username\":\"" + username + "\"}")  // 사용자 필터링
                .load();

        // 'Interests' 배열을 하나의 문자열로 합침 (공백으로 구분) => array_join 사용
        // array_join으로 Interests 배열을 하나의 문자열로 합친 후 interestsString 컬럼을 생성합니다.
        userInterests = userInterests.withColumn("interestsString", functions.array_join(functions.col("Interests"), " "));
        userInterests.show();
        // 최종 결과 반환 (username과 keywords만 선택)
        return userInterests.select("username", "interestsString");
    }

    // 동영상 데이터를 가져오는 함수 (MongoDB에서 가져오는 부분)
    private Dataset<Row> getVideos() {
        Dataset<Row> videos = spark.read().format("mongo")
                .option("uri", mongoUri)
                .option("database", "creator_hub")
                .option("collection", "videos")
                .load()
                .select("videoId", "description");

        return videos.select("videoId", "description");
    }

    private Dataset<Row> extractKeywords(Dataset<Row> dataset, String column) {
        // split으로 문자열을 단어로 분리 후, explode로 각 단어를 개별 행으로 변환
        return dataset.withColumn("keywords", functions.split(functions.col(column), " "))
                .withColumn("keywords", functions.explode(functions.col("keywords")));  // 단어들을 개별 행으로 분리
    }

    // 키워드 일치 수를 기반으로 유사도 계산 함수
    private Dataset<Row> calculateKeywordMatch(Dataset<Row> userKeywords, Dataset<Row> videoKeywords) {
        // userKeywords의 'keywords' 컬럼을 'user_keywords'로 변경
        userKeywords = userKeywords.withColumnRenamed("keywords", "user_keywords");

        // videoKeywords의 'keywords' 컬럼을 'video_keywords'로 변경
        videoKeywords = videoKeywords.withColumnRenamed("keywords", "video_keywords");

        // 'user_keywords'와 'video_keywords'를 비교하여 일치하는 키워드를 계산
        return userKeywords.join(videoKeywords, userKeywords.col("user_keywords").equalTo(videoKeywords.col("video_keywords")))
                .groupBy("videoId")
                .agg(functions.count("user_keywords").alias("similarity"));  // 일치하는 키워드 수 계산
    }

    // 추천 결과를 MongoDB에 저장하는 함수
    public void saveRecommendationsToMongoDB(JavaRDD<Row> recommendedVideos, String username) {
        List<VideoRecommendation> recommendationList = recommendedVideos.collect()
                .stream()
                .map(row -> {
                    String videoId = row.getAs("videoId");
                    Object similarityObj = row.getAs("similarity");

                    // similarity가 null일 경우 처리
                    if (similarityObj == null) {
                        return null;
                    }

                    // similarity가 Long 타입일 경우 Double로 변환
                    Double similarity = null;
                    if (similarityObj instanceof Long) {
                        similarity = ((Long) similarityObj).doubleValue();  // Long을 Double로 변환
                    } else if (similarityObj instanceof Double) {
                        similarity = (Double) similarityObj;  // 이미 Double이라면 그대로 사용
                    }

                    // similarity가 NaN일 경우 처리
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


