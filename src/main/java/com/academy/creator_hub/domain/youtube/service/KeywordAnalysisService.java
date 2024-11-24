package com.academy.creator_hub.domain.youtube.service;

import com.academy.creator_hub.domain.youtube.model.TrendKeyword;
import com.academy.creator_hub.domain.youtube.repository.TrendKeywordsRepository;
import lombok.RequiredArgsConstructor;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeywordAnalysisService {

    private final SparkSession spark;
    private final TrendKeywordsRepository trendKeywordsRepository;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    public Dataset<Row> getVideoData() {
        Dataset<Row> videos = spark.read()
                .format("mongo")
                .option("uri", mongoUri)
                .option("database", "creator_hub")
                .option("collection", "videos")
                .load()
                .select("videoId", "tags");

        videos = videos.withColumn("tags", functions.concat_ws(",", functions.col("tags")));

        return videos;
    }

    public void analyzeKeywordTrendsAndSave() {
        Dataset<Row> videos = getVideoData();

        Dataset<String> tags = videos.select("tags")
                .as(Encoders.STRING()) // tags 컬럼을 문자열로 변환
                .flatMap((FlatMapFunction<String, String>) tagsLine -> {
                    String[] tagArray = tagsLine.split(",");
                    return Arrays.asList(tagArray).iterator();
                }, Encoders.STRING());


        Dataset<Row> keywordCounts = tags.groupBy("value")
                .count()
                .orderBy(functions.desc("count"));

        List<TrendKeyword> trendKeywords = keywordCounts.collectAsList().stream()
                .map(row -> {
                    String keyword = row.getString(row.fieldIndex("value"));
                    long count = row.getLong(row.fieldIndex("count"));

                    // 빈 문자열인 키워드는 제외하고, count가 5 이상인 키워드만 반환
                    if (keyword.isEmpty() || count < 5) {
                        return null;
                    }

                    return new TrendKeyword(keyword, count);
                })
                .filter(keyword -> keyword != null)
                .collect(Collectors.toList());

        trendKeywordsRepository.saveAll(trendKeywords);
    }


    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduleKeywordTrendsAnalysis() {
        analyzeKeywordTrendsAndSave();
    }
}
