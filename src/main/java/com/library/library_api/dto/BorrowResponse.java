package com.library.library_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// What we return after a borrow (or later, a return). We flatten the linked Member and
// Book down to just the useful bits (id + a human-readable label) rather than nesting
// whole objects - the caller rarely needs the full entity here.
@Getter
@Setter
public class BorrowResponse {
    private Long id;

    private Long memberId;
    private String memberName;

    private Long bookId;
    private String bookTitle;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    // null while the book is still out on loan; set once it's returned.
    private LocalDate returnDate;
}
