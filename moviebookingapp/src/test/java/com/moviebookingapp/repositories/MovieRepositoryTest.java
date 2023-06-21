package com.moviebookingapp.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.moviebookingapp.interfaces.impl.MovieServiceImpl;
import com.moviebookingapp.models.Movie;

@SpringJUnitConfig
@DataMongoTest
@TestInstance(Lifecycle.PER_CLASS)
class MovieRepositoryTest {

	@Autowired
	private MovieRepository movieRepository;
	
	@Mock
	private MovieServiceImpl movieService;

	private Movie movie;

	@BeforeEach
	void setUp() throws Exception {
		movie = new Movie("The Dark Knight", 43, "Royal Albert");
		movie.setId("12");
	}

	@AfterEach
	void setDown() throws Exception {
		movieRepository.deleteAll();
		movie = null;
	}
	
	@Test
	void testFindByMovieName() {
		movieRepository.save(movie);

		Movie foundMovie = movieRepository.findByMovieName(movie.getMovieName());

		assertNotNull(foundMovie);
		assertEquals("The Dark Knight", foundMovie.getMovieName());
	}
}
