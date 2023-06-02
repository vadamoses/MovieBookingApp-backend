package com.moviebookingapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.moviebookingapp.models.Movie;

public interface MovieRepository extends MongoRepository<Movie, Long> {
	Movie findByMovieName(String movieName);

	Movie findByMovieNameAndId(String movieName, long id);
}
