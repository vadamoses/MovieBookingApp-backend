package com.moviebookingapp.tools;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtils implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);
	private static final Marker MARKER = MarkerFactory.getMarker(LOGGER.getName());

	@Value("${jwt.signing.key}")
	private String signingKey;

	@Value("${jwt.authorities.key}")
	public String authoritiesKey;

	@Value("${jwt.token.validity}")
	private int jwtValidity;

	@Value("${jwt.cookieName}")
	private String jwtCookie;

	@Value("${jwt.cookieRefreshName}")
	private String jwtRefreshCookie;

	private Key key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
	}

	public String getUserNameFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token).getBody().getSubject();
	}

	public Date getExpirationDateFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token).getBody().getExpiration();
	}

	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		LOGGER.info(MARKER, "Start - validateToken");
		final String username = getUserNameFromToken(token);
		LOGGER.info(MARKER, "End - validateToken");
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public ResponseCookie generateJwtCookie(String token) {
		LOGGER.info(MARKER, "Generating cookie");
		return ResponseCookie.from(jwtCookie, token).path("/").maxAge(jwtValidity * 100L)
				.httpOnly(true).build();
	}

	public ResponseCookie generateRefreshJwtCookie(String token) {
		LOGGER.info(MARKER, "Generating refresh cookie");
		return ResponseCookie.from(jwtRefreshCookie, token).path("/").maxAge(jwtValidity * 400L)
				.httpOnly(true).build();		
	}

	public ResponseCookie getCleanJwtCookie() {
		return ResponseCookie.from(jwtCookie, null).path("/").build();
	}

	public String generateToken(Authentication authentication) {
		LOGGER.info(MARKER, "Start - generateToken");
		String authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
		LOGGER.info(MARKER, "End - generateToken");
		return Jwts.builder()
				.setSubject(authentication.getName())
				.claim(authoritiesKey, authorities)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtValidity * 1000L))
				.signWith(this.key, SignatureAlgorithm.HS512)
				.compact();
	}

	UsernamePasswordAuthenticationToken getAuthenticationToken(final String token, final Authentication existingAuth, final UserDetails userDetails) {
		final Claims claims = Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token).getBody();

		final Collection<? extends GrantedAuthority> authorities =
				Arrays.stream(claims.get(authoritiesKey).toString().split(","))
						.map(SimpleGrantedAuthority::new).toList();

		return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
	}
	
}
