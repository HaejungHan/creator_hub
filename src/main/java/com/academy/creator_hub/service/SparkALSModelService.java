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
        Dataset<Row> videoKeywords = extractKeywords(videos, "tagsString");  // 동영상 설명에서 키워드 추출

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

        // 'Interests' 배열을 explode하여 개별 관심사로 분리
        userInterests = userInterests.withColumn("interestsString", functions.explode(functions.col("Interests")))  // 배열을 개별 행으로 분리
                .select("username", "interestsString");  // 'username'과 'interest'만 선택

        userInterests.show();  // 분리된 데이터를 출력

        return userInterests;
    }

    // 동영상 데이터를 가져오는 함수 (MongoDB에서 가져오는 부분)
    private Dataset<Row> getVideos() {
        // MongoDB에서 모든 동영상의 tags 가져오기
        Dataset<Row> videos = spark.read().format("mongo")
                .option("uri", mongoUri)
                .option("database", "creator_hub")
                .option("collection", "videos")
                .load()
                .select("videoId", "tags");  // 'tags' 컬럼을 포함시킴

        // 'tags' 배열을 explode하여 개별 키워드로 분리
        videos = videos.withColumn("tagsString", functions.explode(functions.col("tags")))  // 배열을 개별 행으로 분리
                .select("videoId", "tagsString");  // 'videoId'와 'keywords'만 선택

        return videos;
    }

    private Dataset<Row> extractKeywords(Dataset<Row> dataset, String column) {
        // 'tags'는 배열 형태일 수도 있기 때문에, 이를 다루는 방법이 중요함
        return dataset.withColumn("keywords", functions.split(functions.col(column), ", ")) // tags가 ','로 구분된 경우 처리
                .withColumn("keywords", functions.explode(functions.col("keywords")));  // 단어들을 개별 행으로 분리
    }

    // 키워드 일치 수를 기반으로 유사도 계산 함수
    private Dataset<Row> calculateKeywordMatch(Dataset<Row> userKeywords, Dataset<Row> videoKeywords) {
        // userKeywords의 'keywords' 컬럼을 'user_keywords'로 변경하고 중복 제거
        userKeywords = userKeywords.withColumnRenamed("keywords", "user_keywords").distinct();

        // videoKeywords의 'keywords' 컬럼을 'video_keywords'로 변경하고 중복 제거
        videoKeywords = videoKeywords.withColumnRenamed("keywords", "video_keywords").distinct();

        // 'user_keywords'와 'video_keywords'를 비교하여 일치하는 키워드를 계산
        Dataset<Row> joined = userKeywords.join(videoKeywords, userKeywords.col("user_keywords").equalTo(videoKeywords.col("video_keywords")));

        // 로그로 일치하는 키워드 출력
        joined.show(false);  // show() 메서드로 데이터프레임 내용을 출력하여 어떤 키워드들이 일치하는지 확인

        // 일치하는 고유한 키워드 수를 계산하여 similarity를 구하고, videoId 별로 그룹화하여 반환
        return joined.groupBy("videoId")
                .agg(functions.countDistinct("user_keywords").alias("similarity"));
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


