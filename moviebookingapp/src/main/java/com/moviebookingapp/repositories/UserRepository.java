package com.moviebookingapp.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.moviebookingapp.models.User;

public interface UserRepository extends MongoRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);
}