package com.academy.creator_hub.repository;

import com.academy.creator_hub.entity.Video;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface VideoRepository extends MongoRepository<Video, Long> {
}
