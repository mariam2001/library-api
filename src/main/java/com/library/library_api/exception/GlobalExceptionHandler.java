package com.library.library_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice makes this ONE class the central error handler for EVERY
// controller. When a controller (or the service it calls) throws an exception, Spring
// looks here for a matching @ExceptionHandler method instead of letting the exception
// bubble up into a generic 500. This keeps error-to-HTTP mapping in a single place
// rather than sprinkling try/catch through every controller.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // One method handles BOTH not-found exceptions (they map to the same status).
    // Whenever a BookNotFoundException OR MemberNotFoundException is thrown anywhere,
    // this turns it into a clean 404 Not Found instead of a 500.
    @ExceptionHandler({BookNotFoundException.class, MemberNotFoundException.class, BorrowRecordNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        // ProblemDetail is Spring's built-in, standardized error body (RFC 9457). Returning
        // one from an @ExceptionHandler makes Spring set the HTTP status to match (404 here)
        // and serialize a tidy JSON body like:
        //   { "type":"about:blank", "title":"Not Found", "status":404, "detail":"Book not found with id: 99" }
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Business-rule violations from the borrow flow. 409 Conflict = "your request is well
    // formed, but it conflicts with the current state of the data" (no copies left, or the
    // member already holds this book). That's different from a 400 (malformed input).
    @ExceptionHandler({NoCopiesAvailableException.class, DuplicateBorrowException.class, AlreadyReturnedException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Spring throws MethodArgumentNotValidException when a @Valid @RequestBody fails its
    // bean-validation rules (e.g. blank name, bad email). We override the default handling
    // to return a 400 with a clear field-by-field list of what was wrong.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more fields are invalid");

        // Collect each failed field and its message into a map, then attach it as an extra
        // property on the response so the client knows exactly which fields to fix.
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", fieldErrors);

        return problem;
    }
}
