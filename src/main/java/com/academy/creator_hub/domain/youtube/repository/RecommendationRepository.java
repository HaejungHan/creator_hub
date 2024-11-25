package com.academy.creator_hub.domain.youtube.repository;

import com.academy.creator_hub.domain.youtube.model.VideoRecommendation;
import com.google.api.services.youtube.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends MongoRepository<VideoRecommendation, String> {
    Optional<VideoRecommendation> findByUsername(String username);
    boolean existsByUsername(String username);
}
