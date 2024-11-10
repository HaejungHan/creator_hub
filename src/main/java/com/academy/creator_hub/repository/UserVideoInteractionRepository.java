package com.academy.creator_hub.repository;

import com.academy.creator_hub.model.UserVideoInteraction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserVideoInteractionRepository extends MongoRepository<UserVideoInteraction, String> {
}
