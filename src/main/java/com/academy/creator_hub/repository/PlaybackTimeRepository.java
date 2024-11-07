package com.academy.creator_hub.repository;

import com.academy.creator_hub.model.PlaybackTime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaybackTimeRepository extends MongoRepository<PlaybackTime, String> {
}
