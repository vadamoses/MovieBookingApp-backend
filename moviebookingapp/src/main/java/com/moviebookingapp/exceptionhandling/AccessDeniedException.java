package com.moviebookingapp.exceptionhandling;

import java.io.Serial;

import org.springframework.security.core.AuthenticationException;

public class AccessDeniedException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}