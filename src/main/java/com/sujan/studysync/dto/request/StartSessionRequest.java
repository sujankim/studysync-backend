package com.sujan.studysync.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartSessionRequest {
    private Long roomId;       // optional
    private String roomName;   // optional
}
