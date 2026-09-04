package com.library.library_api.service;

import com.library.library_api.TestcontainersConfiguration;
import com.library.library_api.dto.BorrowRequest;
import com.library.library_api.dto.BorrowResponse;
import com.library.library_api.entity.Book;
import com.library.library_api.entity.Member;
import com.library.library_api.exception.DuplicateBorrowException;
import com.library.library_api.exception.NoCopiesAvailableException;
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

// @SpringBootTest loads the FULL application context (all beans: services, repositories,
// config) - a step up from the repository test's @DataJpaTest, which wired up only the JPA
// layer. We need the whole context here because we're testing BorrowService's real business
// logic end-to-end.
// @Import(TestcontainersConfiguration.class) plugs in a throwaway Postgres container, so
// these tests run against REAL Postgres - the same database engine as production.
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BorrowServiceIntegrationTest {

    @Autowired private BorrowService borrowService;
    @Autowired private BookRepository bookRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BorrowRecordRepository borrowRecordRepository;

    // Each test gets unique isbn/email values so rows from one test never collide with
    // another's (these tests share one database and don't roll back between them).
    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Test
    void borrowingDecrementsCopiesAndCreatesAnOpenRecord() {
        // Arrange: a book with 2 copies, and a member.
        Book book = createBook(2);
        Member member = createMember();

        // Act: borrow the book.
        BorrowResponse response = borrowService.borrowBook(request(member.getId(), book.getId()));

        // Assert: the response describes an OPEN loan (not returned, due in 14 days)...
        assertThat(response.getBookId()).isEqualTo(book.getId());
        assertThat(response.getMemberId()).isEqualTo(member.getId());
        assertThat(response.getReturnDate()).isNull();
        assertThat(response.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));

        // ...the book now has one fewer available copy...
        Book reloaded = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(reloaded.getAvailableCopies()).isEqualTo(1);

        // ...and the borrow record was actually persisted.
        assertThat(borrowRecordRepository.findById(response.getId())).isPresent();
    }

    @Test
    void borrowingWithNoAvailableCopiesThrows() {
        // Arrange: a 1-copy book, already borrowed by one member (so 0 copies remain).
        Book book = createBook(1);
        borrowService.borrowBook(request(createMember().getId(), book.getId()));

        Member second = createMember();

        // Act + Assert: assertThatThrownBy runs the code and checks it throws the expected
        // exception. No copies left -> the "no copies" rule must reject this borrow.
        assertThatThrownBy(() -> borrowService.borrowBook(request(second.getId(), book.getId())))
                .isInstanceOf(NoCopiesAvailableException.class);
    }

    @Test
    void borrowingSameBookTwiceBySameMemberThrows() {
        // Arrange: 2 copies, so "no copies" is NOT the blocker here - the duplicate rule is.
        Book book = createBook(2);
        Member member = createMember();
        borrowService.borrowBook(request(member.getId(), book.getId()));

        // Act + Assert: the same member can't hold two copies of the same book at once.
        assertThatThrownBy(() -> borrowService.borrowBook(request(member.getId(), book.getId())))
                .isInstanceOf(DuplicateBorrowException.class);
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
