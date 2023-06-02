package com.moviebookingapp.tools;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.moviebookingapp.interfaces.impl.UserServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilter extends OncePerRequestFilter {
	
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserServiceImpl userDetailsService;

  @Value("${jwt.header.string}")
  public String HEADER_STRING;

  @Value("${jwt.token.prefix}")
  public String TOKEN_PREFIX;

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenFilter.class);
  private static final Marker MARKER = MarkerFactory.getMarker(LOGGER.getName());
  
  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    String header = req.getHeader(HEADER_STRING);
    String username = null;
    String authToken = null;
    if (header != null && header.startsWith(TOKEN_PREFIX)) {
      authToken = header.substring(TOKEN_PREFIX.length());
      try {
        username = jwtUtils.getUserNameFromToken(authToken);
      } catch (IllegalArgumentException e) {
        LOGGER.error(MARKER, "An error occurred while fetching Username from Token", e);
      } catch (ExpiredJwtException e) {
        LOGGER.warn("MARKER, The token has expired, {}", e.getLocalizedMessage());
      } catch(SignatureException e){
        LOGGER.warn("MARKER, Authentication Failed. Username or Password not valid.");
      }
    } else {
      LOGGER.warn("MARKER, Couldn't find bearer string, header will be ignored");
    }
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (Boolean.TRUE.equals(jwtUtils.validateToken(authToken, userDetails))) {
        UsernamePasswordAuthenticationToken authentication = jwtUtils.getAuthenticationToken(authToken, SecurityContextHolder.getContext().getAuthentication(), userDetails);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        LOGGER.info(MARKER, "authenticated user: {}, now setting appropriate security context.", username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    chain.doFilter(req, res);
  }
}