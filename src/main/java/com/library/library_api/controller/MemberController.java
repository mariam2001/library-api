package com.library.library_api.controller;

// FIX: removed unused `import com.library.library_api.dto.BookResponse;` (copy-paste leftover).
import com.library.library_api.dto.MemberRequest;
import com.library.library_api.dto.MemberResponse;
import com.library.library_api.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest request){
        MemberResponse memberResponse = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id){
        // (minor) fixed the typo in the variable name: membrResponse -> memberResponse.
        MemberResponse memberResponse = memberService.getMember(id);
        return ResponseEntity.ok(memberResponse);
    }

    // FIX: this endpoint was missing entirely. MemberService already had getAllMembers(),
    // but with no controller method mapped to GET /api/members it was unreachable over HTTP -
    // the logic existed but no URL exposed it. This wires it up.
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers(){
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request){
        MemberResponse memberResponse = memberService.updateMember(id, request);
        return ResponseEntity.ok(memberResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id){
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

}
