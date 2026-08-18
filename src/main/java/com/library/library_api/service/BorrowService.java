package com.library.library_api.service;

import com.library.library_api.dto.BorrowRequest;
import com.library.library_api.dto.BorrowResponse;
import com.library.library_api.entity.Book;
import com.library.library_api.entity.BorrowRecord;
import com.library.library_api.entity.Member;
import com.library.library_api.exception.*;
import com.library.library_api.repository.BookRepository;
import com.library.library_api.repository.BorrowRecordRepository;
import com.library.library_api.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// The borrow flow touches THREE tables (members, books, borrow_records), so this service
// needs all three repositories. This is where the real business rules of a library live.
@Service
public class BorrowService {

    // How many days a member gets to keep a book before it's "due". Server policy, not
    // something the client chooses. Pulled out as a constant so it's easy to find/change.
    private static final int LOAN_PERIOD_DAYS = 14;

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowService(MemberRepository memberRepository,
                         BookRepository bookRepository,
                         BorrowRecordRepository borrowRecordRepository) {
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    // @Transactional wraps this whole method in ONE database transaction. That matters
    // here because we do TWO writes: decrement the book's available copies AND insert a
    // borrow record. If the second write failed, we do NOT want the first to stick (that
    // would "lose" a copy forever). @Transactional guarantees all-or-nothing: either both
    // succeed and commit together, or any exception rolls BOTH back.
    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {
        // 1. Both the member and the book must exist. Reusing the same not-found exceptions
        //    from before, so a bad id here also produces a clean 404.
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException(request.getMemberId()));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

        // 2. RULE: there must be a copy left to lend.
        if (book.getAvailableCopies() <= 0) {
            throw new NoCopiesAvailableException(book.getId());
        }

        // 3. RULE: this member can't already be holding an unreturned copy of THIS book.
        //    existsBy...ReturnDateIsNull is a Spring Data "derived query" - we never write
        //    SQL; Spring reads the METHOD NAME and generates the query. It translates to
        //    roughly: SELECT count(*) > 0 FROM borrow_records
        //             WHERE member_id = ? AND book_id = ? AND return_date IS NULL
        if (borrowRecordRepository.existsByMemberIdAndBookIdAndReturnDateIsNull(
                member.getId(), book.getId())) {
            throw new DuplicateBorrowException(member.getId(), book.getId());
        }

        // 4. All checks passed - lend the book. Write #1: one fewer copy available.
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 5. Write #2: record the loan. Server sets the dates; returnDate stays null
        //    because the book is going out now, not coming back.
        BorrowRecord record = new BorrowRecord();
        record.setMember(member);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        BorrowRecord saved = borrowRecordRepository.save(record);

        return toResponse(saved);
    }

    // Takes ONLY the borrow-record id. The record already knows its member and book, so we
    // don't need (and shouldn't ask for) a request body here.
    @Transactional
    public BorrowResponse returnBook(Long id) {
        // FIX: LOAD the existing loan - don't create a new one. This is the heart of the
        // fix. Returning = finding the open loan and closing it. `new BorrowRecord()` made
        // a second row and left the original loan open forever.
        // FIX: this also un-inverts your not-found check. orElseThrow throws when the record
        // is ABSENT (correct), whereas your `if (findById(id).isPresent()) throw` threw when
        // it was PRESENT (backwards).
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BorrowRecordNotFoundException(id));

        // FIX: "already returned?" is simply whether THIS record already has a return date.
        // Your derived-query version checked the member's whole history, which is both
        // unnecessary and wrong (an unrelated past return would trip it).
        if (record.getReturnDate() != null) {
            throw new AlreadyReturnedException(id);
        }

        // Close the loan: stamp today's return date on the EXISTING record. We do NOT touch
        // borrowDate/dueDate/member/book - that's the original loan's history, left intact.
        record.setReturnDate(LocalDate.now());

        // FIX: get the book straight from the record (record.getBook()), not from a request.
        // Restore one copy - the exact mirror of borrow's `- 1`.
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        BorrowRecord saved = borrowRecordRepository.save(record);
        return toResponse(saved);
    }

    public List<BorrowResponse> getCurrentBorrowsForMember(Long memberId) {
        return borrowRecordRepository.findByMemberIdAndReturnDateIsNull(memberId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BorrowResponse> getOverdueBorrows() {
        return borrowRecordRepository.findByReturnDateIsNullAndDueDateBefore(LocalDate.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // Flattens a BorrowRecord (with its linked Member and Book) into the API response shape.
    private BorrowResponse toResponse(BorrowRecord record) {
        BorrowResponse response = new BorrowResponse();
        response.setId(record.getId());
        response.setMemberId(record.getMember().getId());
        response.setMemberName(record.getMember().getName());
        response.setBookId(record.getBook().getId());
        response.setBookTitle(record.getBook().getTitle());
        response.setBorrowDate(record.getBorrowDate());
        response.setDueDate(record.getDueDate());
        response.setReturnDate(record.getReturnDate());
        return response;
    }
}
