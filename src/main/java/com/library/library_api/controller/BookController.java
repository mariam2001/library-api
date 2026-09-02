package com.library.library_api.controller;

import com.library.library_api.dto.BookRequest;
import com.library.library_api.dto.BookResponse;
import com.library.library_api.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

// @RestController = @Controller + @ResponseBody: every method's return value gets
// written straight into the HTTP response body as JSON, instead of being treated as
// the name of a view template to render.
@RestController
// Every method below is relative to this path, so "" means exactly "/api/books" and
// "/{id}" means "/api/books/{id}".
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    // Switched to constructor injection to match the pattern in BookService - Spring
    // auto-detects the single constructor and supplies a BookService bean, no
    // @Autowired needed.
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    // @Valid tells Spring to run Bean Validation (the @NotBlank/@Min annotations on
    // BookRequest) BEFORE this method body even runs. If validation fails, Spring
    // automatically returns a 400 Bad Request and this code never executes.
    // @RequestBody tells Spring to parse the incoming JSON into a BookRequest object.
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.createBook(request);
        // 201 Created is the correct status for "a new resource was made" - plain 200
        // would technically work but doesn't communicate that as clearly to API clients.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    // @PathVariable pulls the {id} segment out of the URL and passes it in as a method
    // argument - Spring matches it by parameter name.
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        BookResponse response = bookService.getBook(id);
        return ResponseEntity.ok(response);
    }

    // Spring builds the Pageable automatically from the query params:
    //   GET /api/books?page=0&size=20&sort=title,asc
    // @PageableDefault supplies fallbacks when the client omits them: 20 per page, sorted
    // by title. The return type is Page<BookResponse>, which Spring serializes to JSON as
    // a "content" array plus paging metadata (totalElements, totalPages, number, first,
    // last, ...) - so the client knows how to fetch the next page.
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    // GET /api/books/search?title=harry&page=0&size=20
    // FIX: search uses a QUERY PARAM (@RequestParam), not a path variable. A path like
    // "/{title}" would collide with getBook's "/{id}" - both are GET /api/books/{x}, so
    // Spring couldn't tell them apart and the app would fail to start.
    // @RequestParam String title reads ?title=... from the URL; Pageable brings the same
    // page/size/sort support as the main listing.
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooksByTitle(
            @RequestParam String title,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findAllBooksByTitle(title, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        // 204 No Content: the delete succeeded, and there's nothing meaningful to send
        // back in the response body.
        return ResponseEntity.noContent().build();
    }
}
