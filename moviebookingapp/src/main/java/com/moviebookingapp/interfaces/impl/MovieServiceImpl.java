package com.moviebookingapp.interfaces.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.moviebookingapp.interfaces.MovieService;
import com.moviebookingapp.models.Movie;
import com.moviebookingapp.models.Ticket;
import com.moviebookingapp.repositories.MovieRepository;
import com.moviebookingapp.repositories.TicketRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Service
@Tag(name = "Movie Service Implementation for Movie Booking Application", description = "Used for Booking Controller endpoints actions handling")
public class MovieServiceImpl implements MovieService {

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Operation(description = "Used for retrieving all available movies in the database.")
    @Override
    public List<Movie> getAllMovies() {
        return mongoTemplate.findAll(Movie.class);
    }

    @Operation(description = "Used for retrieving all available movies in the database page by page.")
    @Override
    public List<Movie> getAllMovies(Integer page, Integer size) {
        Sort sort = Sort.by("movieName").ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Query query = new Query().with(pageable);

        Page<Movie> pagedResult = PageableExecutionUtils.getPage(mongoTemplate.find(query, Movie.class), pageable,
                () -> mongoTemplate.count(query, Movie.class));

        return pagedResult.getContent();
    }

    @Operation(description = "Used for creating new ticket and storing it in the database.")
    @Override
    public Ticket bookTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
    
    @Operation(description = "Used for storing a list of movies in the database.")
    @Override
    public List<Movie> saveMovies(List<Movie> movieList) {
        return movieRepository.saveAll(movieList);
    }
    
    @Operation(description = "Used for storing a list of tickets in the database.")
    @Override
    public List<Ticket> saveTickets(List<Ticket> tickets) {
        return ticketRepository.saveAll(tickets);
    }
    
    @Operation(description = "Used for retrieving a particular movie from the database.")
    @Override
    public Movie findMovie(String movieName) {
        return movieRepository.findByMovieName(movieName);
    }

    @Operation(description = "Used to delete a particular movie from the database.")
    @Override
    public void deleteMovie(String movieName, long id) {
        Movie movie = movieRepository.findByMovieNameAndId(movieName, id);
        if (movie != null) {
            movieRepository.delete(movie);
        }
    }

    @Operation(description = "Used to delete all movies from the database.")
    @Override
    public void deleteAllMovies() {
        movieRepository.deleteAll();
    }

    @Operation(description = "Used to find the number of tickets booked for a particular movie.")
    @Override
    public long getBookedTicketsCount(String movieName) {
        Query query = new Query(Criteria.where("movieName").is(movieName));
        query.fields().include("numberOfTickets");
        List<Ticket> tickets = mongoTemplate.find(query, Ticket.class);
        return tickets.stream().mapToLong(Ticket::getNumberOfTickets).sum();
    }

    @Operation(description = "Used to update the status of a particular ticket based on number of tickets sold.")
    @Override
    public Ticket updateTicketStatus(String movieName, Ticket ticket) {
        Movie movie = movieRepository.findByMovieName(movieName);
        if (movie == null) {
            return null;
        }
        long numberOfBookedTickets = this.getBookedTicketsCount(movieName);

        long ticketsAvailable = movie.getTotalNumberOfTickets() - numberOfBookedTickets;

        if (ticketsAvailable == 0) {
            ticket.setTicketStatus("SOLD OUT");
        } else {
            ticket.setTicketStatus("BOOK ASAP");
        }

        return ticketRepository.save(ticket);
    }
}
