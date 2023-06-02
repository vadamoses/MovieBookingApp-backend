package com.moviebookingapp.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.moviebookingapp.models.Role;

public interface RoleRepository extends MongoRepository<Role, Long> {
	Optional<Role> findByRoleName(String roleName);
}
