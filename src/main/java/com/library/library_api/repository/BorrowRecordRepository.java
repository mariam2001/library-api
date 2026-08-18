package com.library.library_api.repository;

import com.library.library_api.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    // A Spring Data "derived query": we declare the method, Spring writes the query from
    // the method NAME. It splits into conditions ANDed together:
    //   MemberId          -> WHERE member_id = ?1
    //   And BookId        -> AND book_id = ?2
    //   And ReturnDateIsNull -> AND return_date IS NULL
    // "existsBy..." makes it return a boolean (true if any matching row exists). We use it
    // to enforce "a member can't borrow a second unreturned copy of the same book".
    boolean existsByMemberIdAndBookIdAndReturnDateIsNull(Long memberId, Long bookId);
    List<BorrowRecord> findByMemberIdAndReturnDateIsNull(Long memberId);
    List<BorrowRecord> findByReturnDateIsNullAndDueDateBefore(LocalDate date);

}
