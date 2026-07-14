package com.library.library_api.exception;

// Thrown when a requested book id doesn't exist.
// Nothing catches this yet, so right now it'll surface as a generic 500 error - we'll
// fix that properly when we add a global exception handler (@ControllerAdvice) in a
// later step, which will map this to a clean 404 response instead.
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }
}
