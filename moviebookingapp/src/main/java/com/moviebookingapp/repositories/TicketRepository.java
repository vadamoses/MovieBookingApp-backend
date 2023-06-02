package com.moviebookingapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.moviebookingapp.models.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, Long> {
}
