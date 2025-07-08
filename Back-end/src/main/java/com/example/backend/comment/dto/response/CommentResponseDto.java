package com.example.backend.comment.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

import java.time.LocalDateTime;

@Builder
@Getter
public class CommentResponseDto {
    private UUID commentId;
    private String userNickname;
    private String content;
    private LocalDateTime createdAt;
}

