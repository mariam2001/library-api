package com.library.library_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// To borrow a book, a client only tells us WHO wants WHICH book. Everything else
// (borrow date, due date) is decided by the server - the client doesn't get to set them.
@Getter
@Setter
public class BorrowRequest {

    // @NotNull (not @NotBlank) because these are Long ids, not Strings. @NotBlank only
    // works on text; for an object like Long we just require it to be present.
    @NotNull(message = "memberId is required")
    private Long memberId;

    @NotNull(message = "bookId is required")
    private Long bookId;
}
