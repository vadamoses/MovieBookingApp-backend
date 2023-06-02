package com.moviebookingapp.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviePK implements Serializable {

	private static final long serialVersionUID = 451L;

	private String id;
	private String movieName;
	private String theaterName;

}
