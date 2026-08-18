package com.library.library_api.controller;

import com.library.library_api.dto.BorrowRequest;
import com.library.library_api.dto.BorrowResponse;
import com.library.library_api.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    // POST /api/borrows  -> a member borrows a book.
    // 201 Created because a new borrow record (a new resource) came into existence.
    // All the interesting failure cases (book/member missing -> 404, no copies or duplicate
    // -> 409) are thrown by the service and translated by GlobalExceptionHandler, so this
    // method stays clean and only describes the happy path.
    @PostMapping
    public ResponseEntity<BorrowResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        BorrowResponse response = borrowService.borrowBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/borrows/{id}/return  -> return the book from an existing loan.
    // FIX: path is "/{id}/return" not "/{id}" - "/{id}" alone is ambiguous (POST to a
    // borrow resource?), whereas "/{id}/return" reads as an action on that loan.
    // FIX: no @RequestBody - the {id} identifies the loan completely; member/book come from
    // the record itself. FIX: 200 OK (done now), not 202 ACCEPTED (queued for later).
    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long id) {
        BorrowResponse response = borrowService.returnBook(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/borrows/overdue -> all loans past their due date and not yet returned.
    // return type is List<BorrowResponse> (this returns MANY records), and there is NO cast.
    // Casting a List to a single object compiles but blows up at runtime
    // (ClassCastException) - the real fix is the correct return type.
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowResponse>> getOverdueLoans() {
        List<BorrowResponse> response = borrowService.getOverdueBorrows();
        return ResponseEntity.ok(response);
    }

    // GET /api/borrows/member/{memberId} -> that member's currently-borrowed (open) loans.
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowResponse>> getMemberBooks(@PathVariable Long memberId) {
        List<BorrowResponse> response = borrowService.getCurrentBorrowsForMember(memberId);
        return ResponseEntity.ok(response);
    }
}
