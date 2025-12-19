package com.yumyumcoach.domain.aiadvisor.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ChatSendRequest {
    private LocalDate conversationDate;
    @NotBlank
    private String message;
}
