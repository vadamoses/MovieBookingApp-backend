package com.moviebookingapp.contollers.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
