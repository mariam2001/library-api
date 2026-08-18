package com.library.library_api.exception;

public class BorrowRecordNotFoundException extends RuntimeException {
    public BorrowRecordNotFoundException(Long id) {

        super("Borrow record with id "+ id+ " not Found");
    }
}
