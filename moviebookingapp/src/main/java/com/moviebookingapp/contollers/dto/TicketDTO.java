package com.moviebookingapp.contollers.dto;

import java.io.Serializable;

import com.moviebookingapp.models.Movie;
import com.moviebookingapp.models.Ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO implements Serializable {

	private static final long serialVersionUID = 12L;

	private String id;

	private Movie movie;
	
	private String movieName;
	
	private String theaterName;

	private int numberOfTickets;

	private int seatNumber;

	private String ticketStatus = "BOOK ASAP";

	/**
	 * @param movieName
	 * @param theaterName
	 * @param numberOfTickets
	 * @param seatNumber
	 * @param ticketStatus
	 */
	public TicketDTO(String movieName, String theaterName, int numberOfTickets, int seatNumber, String ticketStatus) {
		super();
		this.movieName = movieName;
		this.theaterName = theaterName;
		this.numberOfTickets = numberOfTickets;
		this.seatNumber = seatNumber;
		this.ticketStatus = ticketStatus;
	}
	
	public Ticket getTicket() {
		Ticket ticket = new Ticket(this.movieName, this.theaterName, this.numberOfTickets,this.seatNumber, this.ticketStatus);
		return ticket;
	}

}
