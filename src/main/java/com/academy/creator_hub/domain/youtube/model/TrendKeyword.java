package com.academy.creator_hub.domain.youtube.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Document(collection = "trend_keywords")
public class TrendKeyword {

    @Id
    private String keyword;
    private long count;
    @CreatedDate
    private LocalDate createdAt;

    public TrendKeyword(String keyword, long count) {
        this.keyword = keyword;
        this.count = count;
        this.createdAt = LocalDate.now();
    }

}
