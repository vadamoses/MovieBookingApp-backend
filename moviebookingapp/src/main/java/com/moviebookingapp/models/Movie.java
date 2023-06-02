package com.moviebookingapp.models;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
@IdClass(MoviePK.class)
public class Movie implements Serializable {

	private static final long serialVersionUID = 15L;

	@Id
    private String id;

    @Id
    @Field("movie_name")
    private String movieName;

    @Field("total_number_of_tickets_alloted")
    private int totalNumberOfTickets;

    @Id
    @Field("theater_name")
    private String theaterName;

	/**
	 * @param movieName
	 * @param totalNumberOfTickets
	 * @param theaterName
	 */
	public Movie(String movieName, int totalNumberOfTickets, String theaterName) {
		super();
		this.movieName = movieName;
		this.totalNumberOfTickets = totalNumberOfTickets;
		this.theaterName = theaterName;
	}
}