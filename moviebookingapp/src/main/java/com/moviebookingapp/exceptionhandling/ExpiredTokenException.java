package com.moviebookingapp.exceptionhandling;

import java.io.Serial;

public class ExpiredTokenException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExpiredTokenException() {
        super();
    }

    public ExpiredTokenException(String message) {
        super(message);
    }
}