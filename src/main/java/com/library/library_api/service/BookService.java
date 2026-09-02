package com.library.library_api.service;

import com.library.library_api.dto.BookRequest;
import com.library.library_api.dto.BookResponse;
import com.library.library_api.entity.Book;
import com.library.library_api.exception.BookNotFoundException;
import com.library.library_api.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// The service layer sits between the controller (HTTP) and the repository (database).
// This is where business logic lives - right now that's just mapping between DTOs and
// entities, but this is also where rules like "can't borrow with 0 copies available"
// will go once we build the borrow/return flow.
@Service
public class BookService {

    private final BookRepository bookRepository;

    // Constructor injection: Spring sees this is the only constructor and automatically
    // supplies a BookRepository bean - no @Autowired needed. Preferred over field
    // injection because dependencies are explicit and the class is easy to unit test
    // (you can just call `new BookService(fakeRepository)`).
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setGenre(request.getGenre());
        book.setTotalCopies(request.getTotalCopies());
        // A newly added book starts fully available - nothing has been borrowed yet.
        book.setAvailableCopies(request.getTotalCopies());

        return toResponse(bookRepository.save(book));
    }

    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return toResponse(book);
    }

    // Pageable is the "which slice do I want" request (page number, size, sort). It arrives
    // from the controller, which lets Spring build it from ?page=&size=&sort= query params.
    // findAll(Pageable) is provided for free by JpaRepository and returns a Page<Book> - the
    // slice of rows PLUS metadata (total count, total pages, ...). Page has its own .map(),
    // so we turn Page<Book> into Page<BookResponse> in one call, keeping all that metadata
    // (no .stream()/.toList() needed here).
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setGenre(request.getGenre());
        book.setTotalCopies(request.getTotalCopies());
        // Simplified for now: borrow/return doesn't exist yet, so no copies are out on
        // loan, meaning totalCopies and availableCopies stay equal at this stage.
        book.setAvailableCopies(request.getTotalCopies());

        return toResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    // Now that the repository returns a Page<Book>, we call .map() directly on it (Page has
    // its own map) to get a Page<BookResponse> - the same clean one-liner as getAllBooks,
    // and it preserves the paging metadata. No .stream()/.toList() needed.
    public Page<BookResponse> findAllBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::toResponse);
    }

    // Converts an entity into the shape we expose over the API.
    private BookResponse toResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setGenre(book.getGenre());
        response.setTotalCopies(book.getTotalCopies());
        response.setAvailableCopies(book.getAvailableCopies());
        return response;
    }
}
