package com.library.library_api.exception;

public class AlreadyReturnedException extends RuntimeException {
    public AlreadyReturnedException(Long id) {
        super("The Book in the Borrow record "+ id+" had already been returned");
    }
}
