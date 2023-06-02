package com.moviebookingapp.models;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tickets")
public class Ticket implements Serializable {

	private static final long serialVersionUID = 12L;

	@Id
	private String id;

	@DBRef
	private Movie movie;
	
	@Id
	@Field("movie_movieName")
	private String movieName;
	
	@Id
	@Field("theater_theaterName")
	private String theaterName;

	@Field("number_of_tickets")
	private int numberOfTickets;

	@Field("seat_number")
	private int seatNumber;

	@Field("ticket_status")
	private String ticketStatus = "BOOK ASAP";

	/**
	 * @param movieName
	 * @param theaterName
	 * @param numberOfTickets
	 * @param seatNumber
	 * @param ticketStatus
	 */
	public Ticket(String movieName, String theaterName, int numberOfTickets, int seatNumber, String ticketStatus) {
		super();
		this.movieName = movieName;
		this.theaterName = theaterName;
		this.numberOfTickets = numberOfTickets;
		this.seatNumber = seatNumber;
		this.ticketStatus = ticketStatus;
	}

}
