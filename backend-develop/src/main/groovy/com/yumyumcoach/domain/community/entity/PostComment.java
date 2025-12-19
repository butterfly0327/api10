package com.yumyumcoach.domain.community.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {
    private Long id;            // PK
    private Long postId;        // FK(posts.id)
    private String authorEmail; // FK(accounts.email)
    private String content;
    private LocalDateTime createdAt;

    public void update(String content) {
        this.content = content;
    }

    public static PostComment newComment(Long postId, String authorEmail, String content) {
        return PostComment.builder()
                .postId(postId)
                .authorEmail(authorEmail)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
