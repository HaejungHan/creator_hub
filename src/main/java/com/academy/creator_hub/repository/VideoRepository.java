package com.academy.creator_hub.repository;

import com.academy.creator_hub.model.Videos;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface VideoRepository extends MongoRepository<Videos, Long> {
}
