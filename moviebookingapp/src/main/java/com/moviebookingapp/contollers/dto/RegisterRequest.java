package com.moviebookingapp.contollers.dto;

import java.util.Set;

import lombok.Data;

@Data
public class RegisterRequest {
	String firstName;
	String lastName;
	String username;
	String email;
	String password;
	String confirmPassword;
	Long contactNumber;
	Set<String> roles;
}
