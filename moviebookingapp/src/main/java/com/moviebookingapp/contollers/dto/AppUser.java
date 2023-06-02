package com.moviebookingapp.contollers.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
	private String firstName;
	private String lastName; 
	private String username; 
	private String email;
	private Long contactNumber;
	private List<String> roles = new ArrayList<>();
}
