package com.library.library_api.repository;

import com.library.library_api.TestcontainersConfiguration;
import com.library.library_api.entity.Book;
import com.library.library_api.entity.BorrowRecord;
import com.library.library_api.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class BorrowRecordRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Test
    void savesBorrowRecordLinkingAMemberAndABook() {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("9780132350884");
        book.setGenre("Programming");
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        book = bookRepository.save(book);

        Member member = new Member();
        member.setName("Ada Lovelace");
        member.setEmail("ada@example.com");
        member.setMembershipDate(LocalDate.now());
        member = memberRepository.save(member);

        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setBook(book);
        borrowRecord.setMember(member);
        borrowRecord.setBorrowDate(LocalDate.now());
        borrowRecord.setDueDate(LocalDate.now().plusDays(14));
        borrowRecord = borrowRecordRepository.save(borrowRecord);

        Optional<BorrowRecord> found = borrowRecordRepository.findById(borrowRecord.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBook().getTitle()).isEqualTo("Clean Code");
        assertThat(found.get().getMember().getEmail()).isEqualTo("ada@example.com");
        assertThat(found.get().getReturnDate()).isNull();
    }
}
