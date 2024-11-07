package com.academy.creator_hub.service;

import org.apache.hadoop.shaded.org.eclipse.jetty.websocket.common.frames.DataFrame;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

@Service
public class MongoDBService {

    private final SparkSession sparkSession;

    // 생성자에서 SparkSession을 주입받음
    public MongoDBService(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    // MongoDB에서 데이터를 읽기
    public void readMongoData() {
        // MongoDB에서 데이터를 읽어 Dataset<Row> 형태로 처리
        Dataset<Row> dataset = sparkSession.read()
                .format("mongo")  // MongoDB에서 데이터를 읽기 위한 포맷
                .load();  // MongoDB에서 데이터를 로드

        dataset.show();  // Dataset을 출력 (디버깅 용도)
    }

    // MongoDB에 데이터 쓰기
    public void writeToMongoDB(Dataset<Row> dataset) {
        dataset.write()
                .format("mongo")  // MongoDB 포맷
                .option("uri", "mongodb://localhost:27017/creator_hub.outputCollection")  // MongoDB URI
                .mode("append")
                .save();  // MongoDB에 데이터 저장
    }
}
