package com.library.library_api.dto;

import lombok.Getter;
import lombok.Setter;

// What we send back to a client. Mirrors Book, but keeping the DTO/entity split means
// if Book ever grows fields we don't want to expose publicly, we only change this class -
// every consumer of the API keeps working against the same response shape.
@Getter
@Setter
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private int totalCopies;
    private int availableCopies;
}
