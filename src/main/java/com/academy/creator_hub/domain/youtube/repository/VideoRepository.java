package com.academy.creator_hub.domain.youtube.repository;

import com.academy.creator_hub.domain.youtube.model.Videos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface VideoRepository extends MongoRepository<Videos, Long> {

    Optional<Videos> findByVideoId(String videoId);
    List<Videos> findTop10ByOrderByPublishedAtDesc();

}
