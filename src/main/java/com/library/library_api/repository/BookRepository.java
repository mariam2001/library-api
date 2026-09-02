package com.library.library_api.repository;

import com.library.library_api.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Search derived query: "TitleContaining" -> WHERE title LIKE %?%, and "IgnoreCase"
    // makes it case-insensitive. Returns a Page (not a List) so search results carry the
    // same paging metadata as the main listing.
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
