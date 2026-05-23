package com.sujan.studysync.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// All fields optional — only update what's provided
// null = keep existing value
public record UpdateProfileRequest(

        @Size(min = 2, max = 100,
                message = "Name must be 2–100 characters")
        String name,

        @Size(min = 3, max = 50,
                message = "Username must be 3–50 characters")
        @Pattern(
                regexp  = "^[a-zA-Z0-9_]+$",
                message = "Username: letters, numbers, underscores only"
        )
        String username,

        @Size(max = 300,
                message = "Bio cannot exceed 300 characters")
        String bio
) {}
