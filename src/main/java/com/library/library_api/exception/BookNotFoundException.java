package com.library.library_api.exception;

// Thrown when a requested book id doesn't exist.
// Caught by GlobalExceptionHandler, which maps it to a clean 404 Not Found response.
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }
}
