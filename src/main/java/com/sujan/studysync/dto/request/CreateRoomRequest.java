package com.sujan.studysync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRoomRequest(
        @NotBlank(message = "Room name is required")
        @Size(min = 3, max = 100, message = "Name must be 3–100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotBlank(message = "Topic is required")
        @Size(max = 100)
        String topic,

        List<String> tags,        // optional, e.g. ["java", "spring"]

        Boolean isPrivate,        // default false

        Integer maxMembers        // default 50
) {}
