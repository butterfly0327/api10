package com.yumyumcoach.domain.aiadvisor.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private LocalDate conversationDate;
    private String role;
    private String message;
    private LocalDateTime createdAt;
}
