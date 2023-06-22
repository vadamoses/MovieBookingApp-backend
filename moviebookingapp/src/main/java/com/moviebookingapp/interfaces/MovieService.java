package com.moviebookingapp.interfaces;

import java.util.List;

import com.moviebookingapp.models.Movie;
import com.moviebookingapp.models.Ticket;

public interface MovieService {
	List<Movie> getAllMovies();
	
	List<Movie> getAllMovies(Integer page, Integer size);

	Ticket bookTicket(Ticket ticket);

	List<Movie> saveMovies(List<Movie> movieList);

	Movie findMovie(String movieName);

	void deleteMovie(String movieName, String id);

	void deleteAllMovies();

	List<Ticket> saveTickets(List<Ticket> tickets);

	long getBookedTicketsCount(String movieName);

	String updateTicketStatus(String movieName, Ticket ticket);

	void deleteAllTickets();

}
