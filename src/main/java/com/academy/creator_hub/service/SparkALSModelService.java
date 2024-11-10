package com.academy.creator_hub.service;


import com.academy.creator_hub.model.VideoRecommendation;
import com.academy.creator_hub.model.Videos;
import com.academy.creator_hub.repository.RecommendationRepository;
import com.academy.creator_hub.repository.VideoRepository;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
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
        userInterests.printSchema(); // 스키마 출력
        userInterests.show();

        // Step 2: MongoDB에서 모든 동영상의 tags 가져오기
        Dataset<Row> videos = getVideos(); // 동영상의 tags 데이터를 가져옵니다
        videos.printSchema(); // 스키마 출력
        videos.show();

        // Step 3: 관심사와 동영상의 tags를 TF-IDF 벡터화
        // 사용자 관심사 벡터화: "userFeatures"
        Dataset<Row> userInterestsVec = transformToTFIDF(userInterests, "interestsString", "userFeatures");
        userInterestsVec.show(10);
        Dataset<Row> videoTagsVec = transformToTFIDF(videos, "descriptionString", "videoFeatures");
        videoTagsVec.show(10);
        // Step 4: Cosine Similarity 계산 (사용자 관심사와 동영상 태그 간의 유사도)
        Dataset<Row> similarityScores = calculateCosineSimilarity(userInterestsVec, videoTagsVec);

        // Step 5: 가장 유사한 동영상 찾기 (username을 추가)
        Dataset<Row> recommendedVideos = similarityScores
                .withColumn("username", functions.lit(username))  // 각 추천 결과에 username 추가
                .orderBy(functions.desc("similarity"))
                .limit(10);  // 상위 10개 추천

        // Step 6: 추천 결과 저장
        JavaRDD<Row> recommendedVideosRDD = recommendedVideos.javaRDD();
        saveRecommendationsToMongoDB(recommendedVideosRDD, username);  // username을 전달
    }

    // 사용자 관심사 데이터를 가져오는 함수 (MongoDB에서 가져오는 부분)
    private Dataset<Row> getUserInterests(String username) {
        // MongoDB에서 사용자 관심사(Interests) 가져오기
        Dataset<Row> userInterests = spark.read().format("mongo")
                .option("uri", mongoUri)  // Mongo URI
                .option("database", "creator_hub")  // DB 이름
                .option("collection", "users")  // users 컬렉션
                .option("filter", "{\"username\":\"" + username + "\"}")  // 사용자 필터링
                .load()
                .select("Interests");  // MongoDB에서 필드 이름이 'Interests'로 대소문자 구분을 해야함

        // 'Interests' 배열을 하나의 문자열로 합침 (공백으로 구분)
        userInterests = userInterests.withColumn("interestsString", functions.concat_ws(" ", functions.col("Interests")));

        // interestsString 컬럼을 반환 (하나의 문자열로 합쳐진 관심사)
        return userInterests.select("interestsString");
    }

    // 동영상 데이터를 가져오는 함수 (MongoDB에서 가져오는 부분)
    private Dataset<Row> getVideos() {
        // MongoDB에서 동영상 데이터 가져오기
        Dataset<Row> videos = spark.read().format("mongo")
                .option("uri", mongoUri)  // Mongo URI
                .option("database", "creator_hub")  // DB 이름
                .option("collection", "videos")  // videos 컬렉션
                .load()
                .select("videoId", "description");  // videoId와 description 컬럼만 선택

        // 'description' 텍스트를 하나의 문자열로 사용할 것이므로, 그대로 사용
        videos = videos.withColumn("descriptionString", functions.col("description"));

        // descriptionString 컬럼을 반환 (설명 텍스트)
        return videos.select("videoId", "descriptionString");
    }

    // TF-IDF 벡터화를 위한 함수
    private Dataset<Row> transformToTFIDF(Dataset<Row> inputDataset, String inputCol, String outputCol) {
        // 텍스트를 토큰화하는 과정
        Tokenizer tokenizer = new Tokenizer().setInputCol(inputCol).setOutputCol("words");
        Dataset<Row> tokenizedData = tokenizer.transform(inputDataset);

        // HashingTF 적용
        HashingTF hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures");
        Dataset<Row> featurizedData = hashingTF.transform(tokenizedData);

        // IDF 적용
        IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol(outputCol);
        IDFModel idfModel = idf.fit(featurizedData);
        Dataset<Row> rescaledData = idfModel.transform(featurizedData);

        return rescaledData;
    }

    private Dataset<Row> calculateCosineSimilarity(Dataset<Row> userInterestsVec, Dataset<Row> videoTagsVec) {
        // 벡터 크기(Norm) 계산 함수 (UDF)
        UserDefinedFunction vectorNorm = functions.udf((Vector v) -> {
            if (v == null || v.size() == 0) {
                return 0.0; // 벡터 크기가 0이면 0을 반환
            }
            return Math.sqrt(Arrays.stream(v.toArray()).map(x -> x * x).sum());
        }, DataTypes.DoubleType);

        // 내적 계산 함수 (UDF)
        UserDefinedFunction vectorDotProduct = functions.udf((Vector v1, Vector v2) -> {
            if (v1 == null || v2 == null || v1.size() == 0 || v2.size() == 0) {
                return 0.0; // 벡터가 0일 경우 내적은 0
            }
            double[] array1 = v1.toArray();
            double[] array2 = v2.toArray();
            double dotProduct = 0.0;
            for (int i = 0; i < array1.length; i++) {
                dotProduct += array1[i] * array2[i];
            }
            return dotProduct;
        }, DataTypes.DoubleType);

        // 상위 100개 관심사와 100개 동영상을 제한하여 결합
        Dataset<Row> limitedUserInterests = userInterestsVec.limit(100);  // 상위 100개 관심사만 사용
        Dataset<Row> limitedVideoTags = videoTagsVec.limit(100);  // 상위 100개 동영상만 사용

        // 벡터 간의 내적과 크기(Norm) 계산 후 Cosine Similarity 계산
        Dataset<Row> combinedData = limitedUserInterests.crossJoin(limitedVideoTags)
                .withColumn("userNorm", vectorNorm.apply(functions.col("userFeatures")))
                .withColumn("videoNorm", vectorNorm.apply(functions.col("videoFeatures")))
                .withColumn("dotProduct", vectorDotProduct.apply(functions.col("userFeatures"), functions.col("videoFeatures")));
        combinedData.show();
        // similarity 계산 부분
        Dataset<Row> finalResult = combinedData.withColumn("similarity",
                functions.when(functions.col("userNorm").equalTo(0).or(functions.col("videoNorm").equalTo(0)),
                                functions.lit(0))
                        .otherwise(functions.col("dotProduct")
                                .divide(functions.col("userNorm").multiply(functions.col("videoNorm")))));
        finalResult.show();
        // 최종 결과 반환
        return finalResult;
    }

    public void saveRecommendationsToMongoDB(JavaRDD<Row> recommendedVideos, String username) {
        List<VideoRecommendation> recommendationList = recommendedVideos.collect()
                .stream()
                .map(row -> {
                    String videoId = row.getAs("videoId");
                    Double similarity = row.getAs("similarity");

                    // similarity가 null이나 NaN일 경우 처리
                    if (similarity == null || similarity.isNaN()) {
                        return null;
                    }

                    return new VideoRecommendation(username, videoId, similarity);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 저장하려는 리스트 출력 (디버깅용)
        System.out.println("추천 리스트 데이터: " + recommendationList);

        if (!recommendationList.isEmpty()) {
            recommendationRepository.saveAll(recommendationList);
            System.out.println("추천 리스트가 MongoDB에 저장되었습니다.");
        } else {
            System.out.println("추천할 동영상이 없습니다.");
        }
    }
}


