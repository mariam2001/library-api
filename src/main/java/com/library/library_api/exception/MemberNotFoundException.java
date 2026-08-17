package com.library.library_api.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long id) {
        // FIX: copy-paste leftover - the message said "Book not found" in a Member
        // exception. A wrong error message like this is nasty because the code works fine,
        // but when it fires in production the log points you at the wrong entity entirely.
        super("Member not found with id: " + id);
    }
}
