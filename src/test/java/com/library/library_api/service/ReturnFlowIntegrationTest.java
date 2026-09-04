package com.library.library_api.service;

import com.library.library_api.TestcontainersConfiguration;
import com.library.library_api.dto.BorrowRequest;
import com.library.library_api.dto.BorrowResponse;
import com.library.library_api.entity.Book;
import com.library.library_api.entity.Member;
import com.library.library_api.exception.AlreadyReturnedException;
import com.library.library_api.exception.BorrowRecordNotFoundException;
import com.library.library_api.repository.BookRepository;
import com.library.library_api.repository.BorrowRecordRepository;
import com.library.library_api.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ReturnFlowIntegrationTest {

    @Autowired private BorrowService borrowService;
    @Autowired private BookRepository bookRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BorrowRecordRepository borrowRecordRepository;

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Test
    void returningRestoresACopyAndSetsReturnDate() {
        // Arrange: make a 1-copy book + member, then BORROW it (you can only return an
        // existing loan). Grab the record id from the borrow response.
        Book book = createBook(1);
        Member member = createMember();
        BorrowResponse borrow = borrowService.borrowBook(request(member.getId(), book.getId()));
        // ^ after this, availableCopies is 0

        // Act: return the loan — returnBook needs the RECORD id (borrow.getId()), not book id.
        BorrowResponse response = borrowService.returnBook(borrow.getId());

        // Assert #1: the loan is now CLOSED — returnDate is set to today
        // (the borrow test asserted this was null; a return flips it).
        assertThat(response.getReturnDate()).isEqualTo(LocalDate.now());

        // Assert #2: the copy is RESTORED — back up to 1
        // (borrow decremented 1 → 0; return brings it 0 → 1).
        Book reloaded = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(reloaded.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void returningAnAlreadyReturnedLoanThrows() {
        // Arrange: borrow a book, then return it once - now the loan is already closed.
        Book book = createBook(1);
        Member member = createMember();
        BorrowResponse borrow = borrowService.borrowBook(request(member.getId(), book.getId()));
        borrowService.returnBook(borrow.getId());

        // Act + Assert: returning the SAME loan again must be rejected. Here the "pass"
        // condition is that the code THROWS - if returnBook ran without throwing, the test
        // fails (that would mean the guard is broken).
        assertThatThrownBy(() -> borrowService.returnBook(borrow.getId()))
                .isInstanceOf(AlreadyReturnedException.class);
    }

    @Test
    void returningAnUnknownRecordThrows() {
        // No Arrange needed - we WANT an id that doesn't exist. Returning a non-existent
        // record must fail with a not-found exception.
        assertThatThrownBy(() -> borrowService.returnBook(999999L))
                .isInstanceOf(BorrowRecordNotFoundException.class);
    }


    // ---- small helpers to keep each test short and readable ----

    private Book createBook(int copies) {
        int n = COUNTER.incrementAndGet();
        Book book = new Book();
        book.setTitle("Test Book " + n);
        book.setAuthor("Author");
        book.setIsbn("ISBN-" + n);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return bookRepository.save(book);
    }

    private Member createMember() {
        int n = COUNTER.incrementAndGet();
        Member member = new Member();
        member.setName("Member " + n);
        member.setEmail("member" + n + "@example.com");
        member.setMembershipDate(LocalDate.now());
        return memberRepository.save(member);
    }

    private BorrowRequest request(Long memberId, Long bookId) {
        BorrowRequest request = new BorrowRequest();
        request.setMemberId(memberId);
        request.setBookId(bookId);
        return request;
    }

}
