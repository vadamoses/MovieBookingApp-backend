package com.moviebookingapp.contollers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebookingapp.contollers.dto.LoginRequest;
import com.moviebookingapp.contollers.dto.MessageResponse;
import com.moviebookingapp.contollers.dto.RegisterRequest;
import com.moviebookingapp.contollers.dto.TicketDTO;
import com.moviebookingapp.interfaces.MovieService;
import com.moviebookingapp.interfaces.impl.UserServiceImpl;
import com.moviebookingapp.models.Movie;
import com.moviebookingapp.models.Role;
import com.moviebookingapp.models.Ticket;
import com.moviebookingapp.models.User;
import com.moviebookingapp.repositories.MovieRepository;
import com.moviebookingapp.repositories.TicketRepository;
import com.moviebookingapp.tools.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@DisplayName("Booking Controller Integration Test")
class BookingControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	PasswordEncoder encoder;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private JwtUtils jwtTokenUtil;

	@Autowired
	private BookingController bookingController;

	@Autowired
	MovieRepository movieRepository;

	@Autowired
	TicketRepository ticketRepository;

	@Mock
	private MovieService movieService;

	@MockBean
	private UserServiceImpl userService;

	List<Movie> movies;

	List<Ticket> tickets;

	RegisterRequest signUpRequest;

	LoginRequest loginRequest;

	User user;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@AfterEach
	public void cleanup() {
		movies = null;
		tickets = null;
	}

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

		movies = Arrays.asList(new Movie("Independence Day", 43, "Royal Albert"), new Movie("Jumanji", 91, "Southside"),
				new Movie("Jaws", 103, "Reduxx"));
		tickets = Arrays.asList(
				new Ticket(movies.get(0).getMovieName(), movies.get(0).getTheaterName(), 2, 69, "BOOK ASAP"),
				new Ticket(movies.get(1).getMovieName(), movies.get(1).getTheaterName(), 5, 109, "BOOK ASAP"));

		signUpRequest = new RegisterRequest();
		signUpRequest.setFirstName("John1");
		signUpRequest.setLastName("Doe");
		signUpRequest.setUsername("johndoe");
		signUpRequest.setEmail("johndoe@example.com");
		signUpRequest.setPassword("password");
		signUpRequest.setConfirmPassword("password");
		signUpRequest.setContactNumber(1234567890L);
		Set<String> roles = new HashSet<>();
		Role r1 = new Role("user");
		roles.add(r1.getRoleName());
		signUpRequest.setRoles(roles);

		user = new User();
		user.setId("userId");
		user.setUsername(signUpRequest.getUsername());
		user.setFirstName(signUpRequest.getFirstName());
		user.setLastName(signUpRequest.getLastName());
		user.setEmail(signUpRequest.getEmail());
		user.setPassword(signUpRequest.getPassword());
		user.setContactNumber(signUpRequest.getContactNumber());
		Set<Role> rolesx = new HashSet<>();
		Role rx = new Role("user");
		rolesx.add(rx);
		user.setRoles(rolesx);

		loginRequest = new LoginRequest();
		loginRequest.setUsername("testuser");
		loginRequest.setPassword("password");
	}
	
    @Test
    void adminCanCreateOrganization() throws Exception {

    	String movieName = "Jumanji";
		int numberOfTickets = 5;
		int seatNumber = 49;

		mockMvc.perform(post("/api/v1.0/moviebooking/{movieName}/add", movieName)
				.param("numberOfTickets", String.valueOf(numberOfTickets))
				.param("seatNumber", String.valueOf(seatNumber))
                .with(user("admin1").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

	@Test
	@DisplayName("Test register new user with credentials")
	void testRegisterUser() throws Exception {

		when(userService.existsByUsername(signUpRequest.getUsername())).thenReturn(false);

		when(userService.existsByEmail(signUpRequest.getEmail())).thenReturn(false);

		when(userService.findByRoleName(anyString())).thenReturn(Optional.of(new Role("user")));

		ResponseEntity<?> response = bookingController.registerUser(signUpRequest);

		assertNotNull(response.getBody());
		assertNotNull(signUpRequest.getRoles());
		assertEquals(signUpRequest.getPassword(), signUpRequest.getConfirmPassword());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof MessageResponse);
		assertEquals("User registered successfully!", ((MessageResponse) response.getBody()).getMessage());

		verify(userService).existsByUsername(signUpRequest.getUsername());
		verify(userService).existsByEmail(signUpRequest.getEmail());
		verify(userService, times(1)).findByRoleName(anyString());
		verify(userService).saveUser(any(User.class));

	}

	@Test
	@DisplayName("Test sign in of user with good credentials")
	void testLoginUser() throws Exception {
		Authentication authentication = Mockito.mock(Authentication.class);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		String token = "test-token";
		when(jwtTokenUtil.generateToken(authentication)).thenReturn(token);
		ResponseCookie cookie = ResponseCookie.from("test-cookie", token).path("/api").maxAge(100 * 1000L)
				.httpOnly(true).build();
		when(jwtTokenUtil.generateJwtCookie(token)).thenReturn(cookie);
		when(jwtTokenUtil.generateRefreshJwtCookie(token)).thenReturn(cookie);

		mockMvc.perform(post("/api/v1.0/moviebooking/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
				.andExpect(header().exists(HttpHeaders.SET_COOKIE)).andExpect(jsonPath("$.token").value(token));
	}

	@Test
	@DisplayName("Test sign in of user with bad credentials")
	void testLoginUserWithBadCredentials() throws Exception {

		AuthenticationProvider authenticationProvider = new AuthenticationProvider() {
			@Override
			public boolean supports(Class<?> authentication) {
				return true;
			}

			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				throw new AuthenticationException("Bad Credentials") {
					private static final long serialVersionUID = 1L;
				};
			}
		};
		ProviderManager providerManager = new ProviderManager(Arrays.asList(authenticationProvider));

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenAnswer(invocation -> providerManager.authenticate(invocation.getArgument(0)));
		mockMvc.perform(post("/api/v1.0/moviebooking/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Test sign out user")
	@WithMockUser(username = "admin", password = "password", roles = { "ADMIN" })
	void testLogOutUser() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1.0/moviebooking/logout")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("You've been signed out!"));
	}

	@Test
	@DisplayName("sign in user with fogotten password test")
	@WithMockUser(username = "admin", password = "password", roles = { "ADMIN" })
	void testForgotPassword() throws Exception {
		String username = "admin";
		String newPassword = "newpassword";
		String token = "refresh_token_value";

		Authentication authentication = Mockito.mock(Authentication.class);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtTokenUtil.generateToken(authentication)).thenReturn(token);
		ResponseCookie cookie = ResponseCookie.from("refresh_token", token).path("/api").maxAge(100 * 1000L)
				.httpOnly(true).build();
		when(jwtTokenUtil.generateJwtCookie(token)).thenReturn(cookie);
		when(jwtTokenUtil.generateRefreshJwtCookie(token)).thenReturn(cookie);

		UserDetails userDetails = Mockito.mock(UserDetails.class);
		when(userService.loadUserByUsername(username)).thenReturn(userDetails);
		when(jwtTokenUtil.validateToken(token, userDetails)).thenReturn(true);

		when(userService.findByUsername(username)).thenReturn(user);

		MvcResult result = mockMvc.perform(get("/api/v1.0/moviebooking/{username}/forgot", username)
				.param("newPassword", newPassword).cookie(new Cookie("refresh_token", token))
				.header(HttpHeaders.SET_COOKIE, cookie).header("refresh_token", cookie)).andExpect(status().isOk())
				.andReturn();

		HttpServletRequest request = result.getRequest();
		String jwtRefreshCookieValue = getRefreshCookieValue(request);

		assertNotNull(jwtRefreshCookieValue);

		verify(userService).findByUsername(username);
		verify(userService).loadUserByUsername(username);
		verify(jwtTokenUtil).validateToken(token, userDetails);
	}

	private String getRefreshCookieValue(HttpServletRequest request) {
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

	@Test
	@DisplayName("Get all movies controller test")
	@WithMockUser(username = "admin", password = "password", roles = { "ADMIN" })
	void testViewAllMovies() throws Exception {
		ResponseEntity<?> response = bookingController.viewAllMovies();

		assertNotNull(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	@DisplayName("Get all movies per page controller test")
	@WithMockUser(username = "admin", password = "password", roles = { "USER" })
	void testViewAllMoviesPerPage() throws Exception {
		int page = 1;
		int size = 10;

		ResponseEntity<?> result = bookingController.viewAllMoviesPerPage(page, size);

		assertNotNull(result.getBody());
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	@DisplayName("retrieve the number of tickets booked for a movie with the given movie title test")
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void testFindCountOfTicketsForMovie() throws Exception {
		Movie movie = new Movie("Jumanji", 91, "Southside");
		long numberOfTicketsForMovie = 5;

		/*
		 * mockMvc.perform( get("/api/v1.0/moviebooking/search/{movieName}",
		 * movieName).contentType(MediaType.APPLICATION_JSON)).andReturn();
		 */
		/*
		 * .andExpect(status().isOk()).andExpect(content().contentType(MediaType.
		 * APPLICATION_JSON)) .andExpect(jsonPath("$").isNumber());
		 */

		when(movieService.findMovie(anyString())).thenReturn(movie);

		when(movieService.getBookedTicketsCount(anyString())).thenReturn(numberOfTicketsForMovie);

		ResponseEntity<?> result = bookingController.findCountOfTicketsForMovie(movie.getMovieName());

		assertNotNull(result.getBody());
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	@DisplayName("retrieve a movie with the given title test")
	@WithMockUser(username = "admin", password = "password", roles = { "USER" })
	void testFindMovie() throws Exception {
		Movie movie = new Movie("Jumanji", 91, "Southside");

		ResponseEntity<?> result = bookingController.findMovie(movie.getMovieName());

		assertNotNull(result.getBody());
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	@DisplayName("book a ticket with the given details test")
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void testBookTicket() throws Exception {
		String movieName = "Jumanji";
		int numberOfTickets = 5;
		int seatNumber = 49;

		when(movieService.bookTicket(any(Ticket.class))).thenReturn(tickets.get(1));

		mockMvc.perform(post("/api/v1.0/moviebooking/{movieName}/add", movieName)
				.param("numberOfTickets", String.valueOf(numberOfTickets))
				.param("seatNumber", String.valueOf(seatNumber)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.movieName").value(movieName))
				.andExpect(jsonPath("$.numberOfTickets").value(numberOfTickets))
				.andExpect(jsonPath("$.seatNumber").value(seatNumber));
	}

	@Test
	@DisplayName("update ticket status based on the given details test")
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void testUpdateTicketStatus() throws Exception {
		String movieName = "Jumanji";
		String ticketId = "ticket-123";
		String updatedTicketStatus = "BOOK ASAP";

		TicketDTO ticketDto = new TicketDTO(movies.get(1).getMovieName(), movies.get(1).getTheaterName(), 5, 109,
				"BOOK ASAP");

		mockMvc.perform(put("/api/v1.0/moviebooking/{movieName}/update/{ticketId}", movieName, ticketId)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ticketDto)))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(updatedTicketStatus));
	}

	@Test
	@DisplayName("delete a movie with given details test")
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void testDeleteMovie() throws Exception {
		String movieName = "Jumanji";
		Long movieId = 123L;

		mockMvc.perform(delete("/api/v1.0/moviebooking/{movieName}/delete/{movieId}", movieName, movieId)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("Movie has been deleted!"));
	}

}
