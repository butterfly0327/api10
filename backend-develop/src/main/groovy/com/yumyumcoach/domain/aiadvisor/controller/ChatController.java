package com.yumyumcoach.domain.aiadvisor.controller;

import com.yumyumcoach.domain.aiadvisor.dto.ChatMessageResponse;
import com.yumyumcoach.domain.aiadvisor.dto.ChatSendRequest;
import com.yumyumcoach.domain.aiadvisor.service.ChatService;
import com.yumyumcoach.global.common.CurrentUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/ai-chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send-once")
    public List<ChatMessageResponse> send(@RequestBody @Valid ChatSendRequest request) {
        String email = CurrentUser.email();
        return chatService.sendMessage(email, request);
    }

    @GetMapping("/daily-history")
    public List<ChatMessageResponse> getDailyHistory(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = CurrentUser.email();
        return chatService.getHistory(email, date);
    }
}
