package com.moviebookingapp.configuration;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.moviebookingapp.interfaces.impl.UserServiceImpl;
import com.moviebookingapp.tools.AuthEntryPointJwt;
import com.moviebookingapp.tools.AuthTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Autowired
	UserServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Bean
	public AuthTokenFilter authTokenFilter() {
		return new AuthTokenFilter();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);
		http.cors(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
		
				.requestMatchers("/v3/**").permitAll()
				.requestMatchers("/swagger-ui/**").permitAll()
				.requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/v1.0/moviebooking/init").permitAll()
                .requestMatchers("/api/v1.0/moviebooking/produce").permitAll()
                .requestMatchers("/api/v1.0/moviebooking/{username}/forgot").permitAll()
                .requestMatchers("/api/v1.0/moviebooking/login").permitAll()
                .requestMatchers("/api/v1.0/moviebooking/register").permitAll()
                .requestMatchers("/resources/**").permitAll()
                .anyRequest().authenticated()) 
		
				.exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
				.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)
				.formLogin(AbstractAuthenticationFilterConfigurer::permitAll).logout(lOut -> lOut.logoutUrl("/logout"))
				.sessionManagement(sessionManagement -> sessionManagement
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(Collections.singletonList("*"));
		config.setExposedHeaders(Collections.singletonList("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public CorsFilter corsFilter() {
		return new CorsFilter(corsConfigurationSource());
	}

}
