package com.academy.creator_hub.repository;

import com.academy.creator_hub.model.VideoRecommendation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecommendationRepository extends MongoRepository<VideoRecommendation, String> {
    Optional<VideoRecommendation> findByUsername(String username);
}
