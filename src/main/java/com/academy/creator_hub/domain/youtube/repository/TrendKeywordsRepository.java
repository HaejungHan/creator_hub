package com.academy.creator_hub.domain.youtube.repository;

import com.academy.creator_hub.domain.youtube.model.TrendKeyword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrendKeywordsRepository extends MongoRepository<TrendKeyword, String> {


}
