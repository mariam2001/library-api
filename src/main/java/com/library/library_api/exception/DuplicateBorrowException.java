package com.library.library_api.exception;

// Thrown when a member tries to borrow a book they ALREADY have an unreturned copy of.
// The rule: one member shouldn't hold two copies of the same title at once.
// Caught by GlobalExceptionHandler and mapped to 409 Conflict.
public class DuplicateBorrowException extends RuntimeException {
    public DuplicateBorrowException(Long memberId, Long bookId) {
        super("Member " + memberId + " already has an unreturned copy of book " + bookId);
    }
}
