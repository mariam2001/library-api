package com.library.library_api.dto;

// FIX: removed `import jakarta.persistence.Column;` - that's a JPA annotation for entities,
// and it was never used here. DTOs are plain data holders with no persistence annotations.

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberResponse {

    // FIX: your comment said returning the id is "a security risk" - that's a misconception,
    // so I added the id back in. Two reasons:
    //   1. The id is NOT sensitive. It's just an auto-generated row number, not a password
    //      or secret. BookResponse returns its id for the same reason.
    //   2. The client NEEDS it. After POST /api/members, the response is how the caller
    //      learns the new member's id - without it they could never call GET/PUT/DELETE
    //      /api/members/{id}. Omitting it breaks the whole REST flow.
    // (Good security instinct, though! The real defense against people *guessing* other
    //  ids is authorization checks or using UUIDs instead of sequential numbers - not
    //  hiding the id from its rightful owner.)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private LocalDate membershipDate;
}
