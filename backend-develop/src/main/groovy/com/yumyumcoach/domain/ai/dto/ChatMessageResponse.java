package com.yumyumcoach.domain.ai.dto;

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
    private String role;
    private String content;
    private LocalDate conversationDate;
    private LocalDateTime createdAt;
}
