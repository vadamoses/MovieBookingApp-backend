package com.moviebookingapp.models;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Role implements Serializable {

	private static final long serialVersionUID = 14L;

	@Id
    private String id;

    private String roleName;

	/**
	 * @param roleName
	 */
	public Role(String roleName) {
		super();
		this.roleName = roleName;
	}
}