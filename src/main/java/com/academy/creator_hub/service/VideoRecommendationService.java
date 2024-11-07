package com.academy.creator_hub.service;

import com.academy.creator_hub.dto.VideoDto;
import com.academy.creator_hub.model.User;
import com.academy.creator_hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.*;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class VideoRecommendationService {
    private final SparkSession spark;
    private final UserRepository userRepository;

    public List<VideoDto> getRecommendedVideos(User user) {
        User foundUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new NotFoundException(user.getUsername()));

        List<String> userInterests = foundUser.getInterests();

        // MongoDB에서 videos 컬렉션 읽기
        Dataset<Row> videosDataset = spark.read()
                .format("mongo")
                .option("database", "creator_hub")
                .option("collection", "videos")
                .load();

        // 사용자 관심사 벡터화 및 비디오 태그 벡터화
        Dataset<Row> userVectorized = vectorizeUserInterests(userInterests);
        Dataset<Row> videoVectorized = vectorizeVideoTags(videosDataset);

        // Cosine Similarity 계산 후 추천 비디오 목록 반환
        return calculateCosineSimilarity(userVectorized, videoVectorized, videosDataset);
    }

    // 사용자 관심사 벡터화 (TF-IDF 또는 HashingTF 방식으로)
    private Dataset<Row> vectorizeUserInterests(List<String> userInterests) {

        Dataset<Row> userDataset = spark.createDataset(userInterests, Encoders.STRING())
                .toDF("value");  // 기본 컬럼명은 "value"로 생성되므로 이를 "interests"로 변경

        userDataset = userDataset.withColumnRenamed("value", "interests");

        // "interests" 컬럼을 배열로 변환 (Word2Vec은 배열 형식이 필요)
        userDataset = userDataset.withColumn("interests", functions.array(userDataset.col("interests")));

        // 3. Word2Vec 벡터화 처리
        Word2Vec word2Vec = new Word2Vec()
                .setInputCol("interests")  // interests 컬럼을 입력으로 사용
                .setOutputCol("features")  // features 컬럼에 벡터화된 결과를 저장
                .setVectorSize(100)        // 벡터 차원 설정 (예: 100차원)
                .setMinCount(0);           // 등장 횟수가 적은 단어는 무시

        Word2VecModel model = word2Vec.fit(userDataset);
        Dataset<Row> featurizedData = model.transform(userDataset);

        // 결과: "features" 컬럼에 벡터화된 관심사 정보가 포함됩니다.
        return featurizedData;
    }

    private Dataset<Row> vectorizeVideoTags(Dataset<Row> videosDataset) {
        // "tags"가 배열로 되어 있기 때문에 그대로 사용
        Word2Vec word2Vec = new Word2Vec()
                .setInputCol("tags")         // "tags" 배열을 입력으로 사용
                .setOutputCol("features")    // 벡터화된 결과를 "features" 컬럼에 저장
                .setVectorSize(100)          // 벡터 차원 설정 (100차원)
                .setMinCount(0);             // 최소 등장 횟수 (0으로 설정하면 모든 단어를 고려)

        // Word2Vec 모델을 학습
        Word2VecModel model = word2Vec.fit(videosDataset);
        Dataset<Row> featurizedData = model.transform(videosDataset);

        return featurizedData;
    }

    private List<VideoDto> calculateCosineSimilarity(Dataset<Row> userDataset, Dataset<Row> videoDataset, Dataset<Row> originalVideosDataset) {
        // 첫 번째 사용자 벡터
        Row userRow = userDataset.head();
        Vector userVector = userRow.getAs("features");  // ml Vector (SparseVector 또는 DenseVector)

        // 비디오 벡터와 Cosine Similarity 계산
        List<Row> videoRows = videoDataset.collectAsList();
        List<VideoDto> recommendedVideos = new ArrayList<>();

        for (Row videoRow : videoRows) {
            Vector videoVector = videoRow.getAs("features");  // ml Vector (SparseVector 또는 DenseVector)

            // 코사인 유사도 계산
            double similarity = calculateCosineSimilarity(userVector, videoVector);

            // 유사도가 0.5 이상인 비디오만 추천
            if (similarity >= 0.5) {
                // 비디오 상세 정보를 `originalVideosDataset`에서 가져옴
                String videoId = videoRow.getAs("videoId");
                String title = videoRow.getAs("title");
                String description = videoRow.getAs("description");
                String thumbnailUrl = videoRow.getAs("thumbnailUrl");
                BigInteger viewCount = videoRow.getAs("viewCount");
                BigInteger likeCount = videoRow.getAs("likeCount");
                BigInteger commentCount = videoRow.getAs("commentCount");
                LocalDateTime publishedAt = videoRow.getAs("publishedAt");
                String channelId = videoRow.getAs("channelId");
                String channelTitle = videoRow.getAs("channelTitle");
                String categoryId = videoRow.getAs("categoryId");
                String duration = videoRow.getAs("duration");

                // VideoDto 객체 생성 후 추천 리스트에 추가
                recommendedVideos.add(new VideoDto(videoId, title, description, thumbnailUrl, viewCount,
                        likeCount, commentCount, publishedAt, channelId, channelTitle, categoryId, duration));
            }
        }

        return recommendedVideos;
    }

    private double calculateCosineSimilarity(Vector vector1, Vector vector2) {
        // 벡터가 SparseVector일 때 처리
        if (vector1 instanceof SparseVector && vector2 instanceof SparseVector) {
            SparseVector sparseVector1 = (SparseVector) vector1;
            SparseVector sparseVector2 = (SparseVector) vector2;

            // Cosine similarity 계산 (SparseVector에서 직접 계산)
            double dotProduct = sparseVector1.dot(sparseVector2);

            // L2 노름 계산
            double norm1 = calculateL2Norm(sparseVector1);
            double norm2 = calculateL2Norm(sparseVector2);

            return dotProduct / (norm1 * norm2);
        }

        // 벡터가 DenseVector일 때 처리
        double dotProduct = vector1.dot(vector2);

        // 각 벡터의 크기 (Magnitude)
        double magnitude1 = Math.sqrt(Arrays.stream(vector1.toArray()).map(x -> x * x).sum());
        double magnitude2 = Math.sqrt(Arrays.stream(vector2.toArray()).map(x -> x * x).sum());

        // Cosine Similarity 계산
        return dotProduct / (magnitude1 * magnitude2);
    }

    // SparseVector에 대한 L2 노름 계산
    private double calculateL2Norm(SparseVector vector) {
        double sum = 0.0;

        // SparseVector의 모든 인덱스와 값을 가져오기
        int[] indices = vector.indices();  // SparseVector의 모든 인덱스를 가져옴
        double[] values = vector.values();  // SparseVector의 모든 값들을 가져옴

        // 모든 인덱스의 값에 대해 제곱값 합산
        for (int i = 0; i < indices.length; i++) {
            double value = values[i];
            sum += value * value;  // 제곱한 값을 더함
        }

        return Math.sqrt(sum);  // L2 norm (제곱근)
    }
}

