package com.moviebookingapp.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User implements Serializable {

	private static final long serialVersionUID = 9L;

	@Id
	private String id;

	@NotBlank(message = "First name field is required")
	@Size(max = 50)
	private String firstName;

	@NotBlank(message = "Last name field is required")
	@Size(max = 50)
	private String lastName;

	@NotBlank(message = "Username field is required")
	@Size(max = 25)
	private String username;

	@NotBlank(message = "email field is required")
	@Size(max = 50)
	@Email
	private String email;

	@Transient
	@NotBlank(message = "Password field is required")
	@Size(min = 8, max = 30)
	private String password;

	@NotBlank(message = "Contact number field is required")
	@Size(max = 30)
	private Long contactNumber;

	@DBRef
	private Set<Role> roles = new HashSet<>();

	/**
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param email
	 * @param password
	 * @param contactNumber
	 * @param roles
	 */
	public User(String firstName, String lastName, String username, String email, String password, Long contactNumber) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.email = email;
		this.password = password;
		this.contactNumber = contactNumber;
	}
}
