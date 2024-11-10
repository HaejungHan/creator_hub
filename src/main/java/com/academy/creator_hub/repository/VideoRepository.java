package com.academy.creator_hub.repository;

import com.academy.creator_hub.model.Videos;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface VideoRepository extends MongoRepository<Videos, Long> {

    List<Videos> findByVideoId(String videoId);

}
