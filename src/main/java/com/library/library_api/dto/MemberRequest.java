package com.library.library_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemberRequest {

    // FIX: was missing validation. name must not be blank (same rule as BookRequest.title).
    @NotBlank(message = "Name is required")
    private String name;

    // FIX: email needs TWO checks - @NotBlank (must be present) AND @Email (must actually
    // look like an address, e.g. "ada@example.com"). @Email alone would let an empty string
    // through, so we pair them.
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    // phone stays optional - no annotation, so null/blank is allowed.
    private String phone;

    // FIX: removed the `membershipDate` field entirely.
    // A client should NOT get to choose when they joined - that's server-controlled state
    // (exactly like availableCopies on BookRequest). The service sets it to today's date.
}
