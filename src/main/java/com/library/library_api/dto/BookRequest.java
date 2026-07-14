package com.library.library_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// What a client sends us to create or update a book.
// Kept separate from the Book entity on purpose: a client should never be able to set
// internal fields like `id` or `availableCopies` directly - the server controls those.
@Getter
@Setter
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    private String genre;

    // int can't be null, but @Min still rejects 0 or negative values.
    @Min(value = 1, message = "Total copies must be at least 1")
    private int totalCopies;
}
