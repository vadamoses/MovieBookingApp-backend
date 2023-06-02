package com.moviebookingapp.interfaces;

import java.util.List;
import java.util.Optional;

import com.moviebookingapp.models.Role;
import com.moviebookingapp.models.User;

public interface UserService {
    
    User findUserById(Long id);
    
    List<User> findAllUsers();
    
    User saveUser(User user);

	User findByUsername(String username);
	
	Role saveRole(Role role);

	/* returning an optional here allows for the use of .orElseThrow(()) in any calling controller */
	Optional<Role> findByRoleName(String roleName);
	
	void addRoleToUser(String roleName, String username);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);




    
}
