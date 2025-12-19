package com.yumyumcoach.domain.aiadvisor.entity;

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
public class AiChatMessage {
    private Long id;
    private String email;
    private LocalDate conversationDate;
    private String role;
    private String message;
    private LocalDateTime createdAt;
}
