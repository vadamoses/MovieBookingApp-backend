package com.moviebookingapp.contollers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moviebookingapp.contollers.dto.AuthToken;
import com.moviebookingapp.contollers.dto.LoginRequest;
import com.moviebookingapp.contollers.dto.MessageResponse;
import com.moviebookingapp.contollers.dto.RegisterRequest;
import com.moviebookingapp.contollers.dto.TicketDTO;
import com.moviebookingapp.interfaces.impl.MovieServiceImpl;
import com.moviebookingapp.interfaces.impl.UserServiceImpl;
import com.moviebookingapp.kafka.producer.MessageProducer;
import com.moviebookingapp.models.Movie;
import com.moviebookingapp.models.Role;
import com.moviebookingapp.models.Ticket;
import com.moviebookingapp.models.User;
import com.moviebookingapp.repositories.RoleRepository;
import com.moviebookingapp.tools.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/v1.0/moviebooking", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Booking Controller for Movie Booking Application", description = "used for user authentication, ticket booking and Movie Service endpoints handling")
public class BookingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);
	private static final Marker MARKER = MarkerFactory.getMarker(LOGGER.getName());
	private static final String ROLE_NOT_FOUND = "Error: Role is not found.";

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	PasswordEncoder encoder;

	// Autowired here only for use in init() method
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private JwtUtils jwtTokenUtil;

	@Autowired
	private MessageProducer messageProducer;

	private MovieServiceImpl movieService;

	/**
	 * @param movieService
	 */
	public BookingController(MovieServiceImpl movieService) {
		super();
		this.movieService = movieService;
	}

	List<Movie> movies;

	List<Ticket> tickets;

	@Operation(description = "Used to add dummy data to the database.")
	@GetMapping("/init")
	public void initializeValues() {

		movies = Arrays.asList(new Movie("Independence Day", 43, "Royal Albert"), new Movie("Jumanji", 91, "Southside"),
				new Movie("Jaws", 103, "Reduxx"));
		tickets = Arrays.asList(
				new Ticket(movies.get(0).getMovieName(), movies.get(0).getTheaterName(), 2, 69, "BOOK ASAP"),
				new Ticket(movies.get(1).getMovieName(), movies.get(1).getTheaterName(), 5, 109, "BOOK ASAP"),
				new Ticket(movies.get(2).getMovieName(), movies.get(2).getTheaterName(), 7, 33, "BOOK ASAP"));

		User u1 = new User("vas", "asli", "aslivas", "a@li.com", "vas", 1234567890L);
		User u2 = new User("man", "saw", "sawman", "saw@li.com", "man", 7934937891L);
		User u3 = new User("tre", "bien", "bi", "bi@li.com", "bi", 1298317890L);

		Role r1 = new Role("user");
		Role r2 = new Role("admin");

		Set<Role> roles = new HashSet<>();
		roles.add(r1);
		roles.add(r2);

		u1.setRoles(roles);
		u2.setRoles(roles);

		roleRepository.save(r1);
		roleRepository.save(r2);

		userService.saveUser(u1);
		userService.saveUser(u2);
		userService.saveUser(u3);

		movieService.saveMovies(movies);
		movieService.saveTickets(tickets);
		
		
	}

	@Operation(description = "Used to sign in a user.")
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) throws AuthenticationException {

		final Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		final String token = jwtTokenUtil.generateToken(authentication);

		ResponseCookie jwtCookie = jwtTokenUtil.generateJwtCookie(token);
		ResponseCookie jwtRefreshCookie = jwtTokenUtil.generateRefreshJwtCookie(token);

		LOGGER.info(MARKER, "Sign in user");

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()).body(new AuthToken(token));
		/*return Cookies as body instead of raw token?*/
	}

	@Operation(description = "Used to register a new user.")
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
		if (Boolean.TRUE.equals(userService.existsByUsername(signUpRequest.getUsername()))) {
			LOGGER.error(MARKER, "Username is already taken!");
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (Boolean.TRUE.equals(userService.existsByEmail(signUpRequest.getEmail()))) {
			LOGGER.error(MARKER, "Email is already in use!");
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		if (Boolean.FALSE.equals(signUpRequest.getPassword().equalsIgnoreCase(signUpRequest.getConfirmPassword()))) {
			LOGGER.error(MARKER, "Both password fields must match!");
			return ResponseEntity.badRequest().body(new MessageResponse("Both password fields must match!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getFirstName(), signUpRequest.getLastName(), signUpRequest.getUsername(),
				signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()),
				signUpRequest.getContactNumber());

		Set<String> strRoles = signUpRequest.getRoles();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			roles.add(new Role("user"));
		} else {
			strRoles.forEach(role -> {
				if (role.equalsIgnoreCase("admin")) {
					Role adminRole = userService.findByRoleName("admin")
							.orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND));
					roles.add(adminRole);
				} else {
					Role userRole = userService.findByRoleName("user")
							.orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userService.saveUser(user);
		LOGGER.info(MARKER, "New user registered successfully!");
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@Operation(description = "Used to sign out a user.")
	@PostMapping("/logout")
	public ResponseEntity<?> logoutUser() {
		return ResponseEntity.ok().body(new MessageResponse("You've been signed out!"));
	}

	@Operation(description = "Used to reset user password and to sign a user back in using a refresh token.")
	@GetMapping("/{username}/forgot")
	public ResponseEntity<?> forgotPassword(@PathVariable(name = "username") String username,
			@RequestParam(name = "newPassword") String newPassword, HttpServletRequest request) {
		User user = userService.findByUsername(username);
		if (user == null) {
			LOGGER.error(MARKER, "User not found!");
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
		}

		// Check if the 'jwtRefreshCookie' is present in the request header
		String refreshCookieValue = getRefreshCookieValue(request);

		if (refreshCookieValue == null) {
			LOGGER.error(MARKER, "Refresh token not found!");
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Refresh token not found!"));
		}

		UserDetails userDetails = this.userService.loadUserByUsername(username);

		// Validate the refresh token and obtain the user's information
		Boolean tokenValid = this.jwtTokenUtil.validateToken(refreshCookieValue, userDetails);
		if (Boolean.FALSE.equals(tokenValid)) {
			LOGGER.error(MARKER, "Invalid refresh token!");
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid refresh token!"));
		}

		user.setPassword(encoder.encode(newPassword));

		userService.saveUser(user);

		LoginRequest loginRequest = new LoginRequest(user.getUsername(), newPassword);
		this.loginUser(loginRequest);

		LOGGER.error(MARKER, "User logged in with refresh token");
		this.loginUser(loginRequest);
		return ResponseEntity.ok(new MessageResponse("User logged in with refresh token"));
	}

	public String getRefreshCookieValue(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("refresh_token")) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	@Operation(description = "Used to retrieve all available movies.")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	@GetMapping("/all")
	public ResponseEntity<?> viewAllMovies() {
		LOGGER.info(MARKER, "Start - viewAllMovies");
		List<Movie> allMovies = this.movieService.getAllMovies();
		LOGGER.trace(MARKER, "End - viewAllMovies");
		return ResponseEntity.ok().body(allMovies);
	}

	@Operation(description = "Used to retrieve all available movies applying pagination.")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	@GetMapping("/all/{page}/{size}")
	public ResponseEntity<?> viewAllMoviesPerPage(@PathVariable(name = "page") Integer page,
			@PathVariable(name = "size") Integer size) {
		LOGGER.info(MARKER, "Start - viewAllMoviesPerPage");
		List<Movie> allMovies = this.movieService.getAllMovies(page, size);
		LOGGER.info(MARKER, "End - viewAllMoviesPerPage");
		return ResponseEntity.ok().body(allMovies);
	}

	@Operation(description = "Used to retrieve the number of tickets booked for a movie with the given movie title.")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	@GetMapping("/search/{name}")
	public ResponseEntity<?> findCountOfTicketsForMovie(@PathVariable(name = "name") String name) {
		LOGGER.info(MARKER, "Start - findCountOfTicketsForMovie");
		Movie movie = this.movieService.findMovie(name);
		long ticketsForMovie = this.movieService.getBookedTicketsCount(movie.getMovieName());
		messageProducer.sendMessage("number_of_tickets_booked_for_movie_topic", String.valueOf(ticketsForMovie));
		LOGGER.info(MARKER, "End - findCountOfTicketsForMovie");
		return ResponseEntity.ok().body(ticketsForMovie);
	}

	@Operation(description = "Used to retrieve a movie with the given movie title.")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	@GetMapping("/movies/search/{movieName}")
	public ResponseEntity<?> findMovie(@PathVariable(name = "movieName") String movieName) {
		LOGGER.info(MARKER, "Start - findMovie");
		Movie movie = this.movieService.findMovie(movieName);
		LOGGER.info(MARKER, "End - findMovie");
		return ResponseEntity.ok().body(movie);
	}

	@Operation(description = "Used to book a ticket with the given details.")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	@PostMapping("/{movieName}/add")
	public ResponseEntity<?> bookTicket(@PathVariable(name = "movieName") String movieName,
			@RequestParam(name = "numberOfTickets") int numberOfTickets,
			@RequestParam(name = "seatNumber") int seatNumber) {
		LOGGER.info(MARKER, "Start - bookTicket");
		Ticket ticket = new Ticket();
		Movie movie = this.movieService.findMovie(movieName);
		if (movie == null) {
			return ResponseEntity.badRequest().body(new MessageResponse("Movie not found."));
		}
		ticket.setMovie(movie);
		ticket.setMovieName(movieName);
		ticket.setTheaterName(movie.getTheaterName());
		ticket.setNumberOfTickets(numberOfTickets);
		ticket.setSeatNumber(seatNumber);
		ticket = this.movieService.bookTicket(ticket);
		LOGGER.info(MARKER, "End - bookTicket");
		return ResponseEntity.ok().body(ticket);
	}

	@Operation(description = "Used to update a movie's details with the given movie title and ticket.")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PutMapping("/{movieName}/update/{ticket}")
	public ResponseEntity<?> updateTicketStatus(@PathVariable(name = "movieName") String movieName,
			@Valid @RequestBody TicketDTO ticketDto) {
		LOGGER.info(MARKER, "Start - updateTicketStatus");
		Ticket ticket = ticketDto.getTicket();
		LOGGER.info(MARKER, "find ticket by movie name.");
		String updatedTicketStatus = this.movieService.updateTicketStatus(movieName, ticket);
		messageProducer.sendMessage("ticket_status_for_movie_topic", updatedTicketStatus);
		LOGGER.info(MARKER, "End - updateTicketStatus");
		return ResponseEntity.ok().body(updatedTicketStatus);
	}

	@Operation(description = "Used to delete a movie with the given movie title and id.")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping("/{movieName}/delete/{id}")
	public ResponseEntity<?> deleteMovie(@PathVariable(name = "movieName") String movieName,
			@PathVariable(name = "id") String id) {
		LOGGER.info(MARKER, "Start - deleteMovie");
		this.movieService.deleteMovie(movieName, id);
		LOGGER.info(MARKER, "End - deleteMovie");
		return ResponseEntity.ok().body(new MessageResponse("Movie has been deleted!"));
	}
}
