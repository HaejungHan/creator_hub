package com.academy.creator_hub.service;


import com.academy.creator_hub.dto.VideoAnalyzeDto;
import com.google.api.services.youtube.model.Video;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YouTubeTrendAnalyzer {

    private final SparkSession spark;

    public YouTubeTrendAnalyzer(SparkSession spark) {
        this.spark = spark;
    }

    public void analyzeTrends(List<VideoAnalyzeDto> videos) {
        Dataset<Row> videoDF = spark.createDataFrame(videos, VideoAnalyzeDto.class);

        videoDF.groupBy("title") // title을 기준으로 그룹화
                .agg(
                        org.apache.spark.sql.functions.sum("viewCount").alias("total_views"),
                        org.apache.spark.sql.functions.avg("likeCount").alias("average_likes")
                )
                .show();
    }
}
