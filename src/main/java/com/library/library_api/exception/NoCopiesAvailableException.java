package com.library.library_api.exception;

// Thrown when someone tries to borrow a book that has 0 available copies.
// Caught by GlobalExceptionHandler and mapped to 409 Conflict (the request is valid, but
// it conflicts with the current state of the data - all copies are out).
public class NoCopiesAvailableException extends RuntimeException {
    public NoCopiesAvailableException(Long bookId) {
        super("No copies available for book with id: " + bookId);
    }
}
