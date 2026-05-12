package com.sujan.studysync.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private String role;
    private String plan;
}
