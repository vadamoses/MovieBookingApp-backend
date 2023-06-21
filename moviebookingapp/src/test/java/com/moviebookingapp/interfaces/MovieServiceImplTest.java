package com.moviebookingapp.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.moviebookingapp.interfaces.impl.MovieServiceImpl;
import com.moviebookingapp.models.Movie;
import com.moviebookingapp.models.Ticket;
import com.moviebookingapp.repositories.MovieRepository;
import com.moviebookingapp.repositories.TicketRepository;

@DataMongoTest
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService test")
class MovieServiceImplTest {

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private MovieServiceImpl movieService;

	@Mock
	TicketRepository ticketRepository;

	@Mock
	MovieRepository movieRepository;

	List<Movie> movies = new ArrayList<>();

	List<Ticket> tickets = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		movies = Arrays.asList(new Movie("Independence Day", 43, "Royal Albert"), new Movie("Jumanji", 91, "Southside"),
				new Movie("Jaws", 103, "Reduxx"));
		tickets = Arrays.asList(
				new Ticket(movies.get(0).getMovieName(), movies.get(0).getTheaterName(), 2, 69, "BOOK ASAP"),
				new Ticket(movies.get(1).getMovieName(), movies.get(1).getTheaterName(), 5, 109, "BOOK ASAP"));
	}

	@AfterEach
	public void tearDown() {
		tickets = null;
		movies = null;
	}

	@Test
	@DisplayName("Get all movies service test")
	void testGetAllMovies() {
		when(mongoTemplate.findAll(Movie.class)).thenReturn(movies);

		List<Movie> allMoviesFetched = movieService.getAllMovies();

		assertEquals(3, allMoviesFetched.size());
		assertEquals(movies, allMoviesFetched);
		verify(mongoTemplate, times(1)).findAll(Movie.class);
	}

	@Test
	@DisplayName("Get all movies paginated service test")
	void testGetAllMoviesPaginated() {
		Integer page = 1;
		Integer size = 10;
		Sort sort = Sort.by("movieName").ascending();
		Pageable pageable = PageRequest.of(page - 1, size, sort);

		Query query = new Query().with(pageable);

		Page<Movie> pagedResult = new PageImpl<>(movies, pageable, movies.size());

		when(mongoTemplate.find(query, Movie.class)).thenReturn(pagedResult.getContent());
		when(mongoTemplate.count(query, Movie.class)).thenReturn((long) movies.size());

		List<Movie> result = movieService.getAllMovies(page, size);

		assertEquals(movies, result);
		assertEquals(movies.size(), result.size());
		assertEquals(movies.size(), mongoTemplate.count(query, Movie.class));
		verify(mongoTemplate, times(1)).find(query, Movie.class);
	}

	@Test
	@DisplayName("Creating new ticket and storing it in the database test")
	void testBookTicket() {
		when(ticketRepository.save(any(Ticket.class))).thenReturn(tickets.get(1));

		Ticket result = movieService.bookTicket(tickets.get(1));

		assertNotNull(result);
		assertEquals(tickets.get(1).getMovieName(), result.getMovieName());
		assertEquals(tickets.get(1).getTheaterName(), result.getTheaterName());

	}

	@Test
	@DisplayName("save a list of movies service test")
	void testSaveMovies() {
		when(movieRepository.saveAll(anyList())).thenReturn(movies);

		List<Movie> result = movieService.saveMovies(movies);

		assertNotNull(result);
		assertEquals(movies, result);
	}

	@Test
	@DisplayName("find a movie by given name service test")
	void testFindMovie() {
		when(movieRepository.findByMovieName(anyString())).thenReturn(movies.get(1));

		Movie result = movieService.findMovie(movies.get(1).getMovieName());

		assertNotNull(result);
		assertEquals(movies.get(1), result);
	}

	@Test
	@DisplayName("find a movie by given name service test")
	void testDeleteMovie() {
		movies.get(1).setId("12");

		when(movieRepository.findByMovieNameAndId(anyString(), anyLong())).thenReturn(movies.get(1));

		movieService.deleteMovie(movies.get(1).getMovieName(), Long.valueOf(movies.get(1).getId()));

		verify(movieRepository).delete(movies.get(1));
	}

	@Test
	@DisplayName("delete all movies service test")
	void testDeleteAllMovies() {
		movieService.deleteAllMovies();

		verify(movieRepository).deleteAll();
	}

	@Test
	@DisplayName("delete all tickets service test")
	void testDeleteAllTickets() {
		movieService.deleteAllTickets();

		verify(ticketRepository).deleteAll();
	}

	@Test
	@DisplayName("save a list of tickets service test")
	void testSaveTickets() {
		when(ticketRepository.saveAll(anyList())).thenReturn(tickets);

		List<Ticket> result = movieService.saveTickets(tickets);

		assertNotNull(result);
		assertEquals(tickets, result);
	}

	@Test
	@DisplayName("get number of tickets booked for a movie service test")
	void testGetBookedTicketsCount() {
		when(mongoTemplate.find(any(Query.class), eq(Ticket.class))).thenReturn(tickets);

		long bookedTicketsCount = movieService.getBookedTicketsCount(tickets.get(1).getMovieName());

		long expectedCount = tickets.stream().mapToLong(Ticket::getNumberOfTickets).sum();

		assertEquals(expectedCount, bookedTicketsCount);
		verify(mongoTemplate, times(1)).find(any(Query.class), eq(Ticket.class));
	}
	
	@Test
	@DisplayName("update tickets status to SOLD OUT service test")
    void testUpdateTicketStatusSoldOut() {
        long numberOfBookedTickets = 10;
        
        Movie movie = movies.get(1);
        Ticket ticket = tickets.get(1);
        movie.setTotalNumberOfTickets(10);
        
        MovieService movieServiceSpy = spy(movieService);

        when(movieRepository.findByMovieName(movie.getMovieName())).thenReturn(movie);

        doReturn(numberOfBookedTickets).when(movieServiceSpy).getBookedTicketsCount(movie.getMovieName());
        
        String ticketStatus = movieServiceSpy.updateTicketStatus(movie.getMovieName(), ticket);

        assertEquals("SOLD OUT", ticketStatus);
        assertEquals("SOLD OUT", ticket.getTicketStatus());
        verify(ticketRepository).save(ticket);
        verify(movieRepository, times(1)).findByMovieName(movie.getMovieName());
    }

    @Test
    @DisplayName("update tickets status to BOOK ASAP service test")
    void testUpdateTicketStatusBookASAP() {
        long numberOfBookedTickets = 5;
        
        Movie movie = movies.get(1);
        Ticket ticket = tickets.get(1);
        movie.setTotalNumberOfTickets(10);
        
        MovieService movieServiceSpy = spy(movieService);

        when(movieRepository.findByMovieName(movie.getMovieName())).thenReturn(movie);
      
        when(movieServiceSpy.getBookedTicketsCount(movie.getMovieName())).thenReturn(numberOfBookedTickets);

        String ticketStatus = movieServiceSpy.updateTicketStatus(movie.getMovieName(), ticket);

        assertNotNull(ticketStatus);
        assertEquals("BOOK ASAP", ticketStatus);
        assertEquals("BOOK ASAP", ticket.getTicketStatus());
        verify(ticketRepository).save(ticket);
        verify(movieRepository, times(1)).findByMovieName(movie.getMovieName());
    }
}
